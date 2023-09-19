import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

import rankedQueue.MyQueue;
import game.Game;
import auth.Login;
import message.Message;

public class Server {



    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);
        Map<String, Socket> users = new HashMap<>();
        Map<String, Socket> queue = new HashMap<>();
        Map<String, String> states = new HashMap<>();
        List<Game> games = new ArrayList<>();
        int nThreads = 5;
        int maxPlayers;
        if (args.length == 2){
            if (Integer.parseInt(args[1]) > 2){
                maxPlayers = Integer.parseInt(args[1]);
            }
            else{
                maxPlayers = 2;
            }

        }
        else{
            maxPlayers = 2;
        }

        MyQueue[] rankedQueues = new MyQueue[nThreads];
        for (int i=0;i<nThreads;i++){
            rankedQueues[i] = new MyQueue();
        }
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);



        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            Thread socketThread = new Thread(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        Thread loginThread = new Thread(() -> {
                            try {
                                Login authenticator = new Login(socket);
                                String user = authenticator.authenticate();
                                if (user.equals("")) {
                                    socket.close();
                                }
                                synchronized (users) {
                                    users.put(user,socket);
                                    users.notifyAll();
                                }
                                synchronized (states){
                                    boolean inGame = false;
                                    for (int i=0;i<games.size();i++){
                                        if (games.get(i).getUsernames().contains(user)){
                                            games.get(i).updateUserSocket(user,socket);
                                            inGame = true;
                                        }
                                    }
                                    if (!inGame){
                                        if (!(states.containsKey(user) && states.get(user).startsWith("in-queueS"))){
                                            states.put(user,"online");
                                        }
                                        else{
                                            synchronized (queue){
                                                queue.put(user,socket);
                                            }
                                            Message messenger = new Message(socket);
                                            messenger.sendMessageString("info");
                                            messenger.sendMessageString("In Simple Queue");
                                            messenger.sendMessageString("Waiting For Players...");
                                            messenger.sendMessageString("Your position in the queue is: " + states.get(user).split("-")[2]);
                                            messenger.sendMessageString("over");
                                        }

                                    }
                                    else{
                                        states.put(user,"ingame");
                                        Message messenger = new Message(socket);
                                        messenger.sendMessageString("info");
                                        messenger.sendMessageString("Going Back to Game");
                                        messenger.sendMessageString("over");
                                    }

                                    states.notifyAll();
                                }



                            } catch (IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        });
                        loginThread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            socketThread.start();

            while (true) {
                synchronized (users) {
                    while (users.isEmpty()) {
                        users.wait();
                    }
                }


                synchronized (states){
                    while (states.isEmpty()){
                        states.wait();
                    }
                }



                for (Map.Entry<String, String> entry : states.entrySet()) {

                    if (entry.getValue().equals("online")) {
                        Socket socket = users.get(entry.getKey());
                        states.put(entry.getKey(),"standby");
                        Thread userThread = new Thread(() -> {
                            try {
                                Message messenger = new Message(socket);
                                String[] messages = {"What game mode would you like to play?", "", "1. Simple", "2. Ranked","3. Quit","over"};
                                boolean resolved = false;
                                while (!resolved){
                                    messenger.sendMultipleMessages(messages);
                                    String response = messenger.waitForMessageServer(false);
                                    switch (response){
                                        case "1":
                                            synchronized (queue) {
                                                queue.put(entry.getKey(),socket);
                                                messenger.sendMessageString("info");
                                                messenger.sendMessageString("In Simple Queue");
                                                messenger.sendMessageString("Waiting For Players...");
                                                messenger.sendMessageString("Your position in the queue is: " + queue.size());
                                                messenger.sendMessageString("over");
                                                queue.notifyAll();
                                            }
                                            synchronized (states){
                                                states.put(entry.getKey(),"in-queueS-" +queue.size());
                                            }
                                            resolved = true;
                                            break;
                                        case "2":
                                            synchronized (rankedQueues){
                                                int index = getBestQueue(rankedQueues,entry.getKey(),maxPlayers);
                                                float score = getUserScore(entry.getKey());
                                                rankedQueues[index].insertPlayer(entry.getKey(),socket,score);
                                                rankedQueues[index].updateRank(getUserScore(entry.getKey()));
                                                messenger.sendMessageString("info");
                                                messenger.sendMessageString("In Ranked Queue");
                                                messenger.sendMessageString("Waiting For Players...");
                                                messenger.sendMessageString("over");
                                                rankedQueues.notifyAll();
                                            }
                                            resolved = true;
                                            break;
                                        case "3":
                                            messenger.sendMessageString("info");
                                            messenger.sendMessageString("quit");
                                            messenger.sendMessageString("over");
                                            socket.close();
                                            synchronized (users){
                                                users.remove(entry.getKey());
                                                states.remove(entry.getKey());
                                            }
                                            resolved = true;
                                            break;
                                        default:
                                            messenger.sendMessageString("Invalid Option");
                                            messenger.sendMessageString("");
                                            break;

                                    }
                                }




                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                        });
                        userThread.start();
                    }
                }


                if (queue.size() >= maxPlayers){ //Simple Queue

                    List<Socket> socketList = new ArrayList<>(queue.values());
                    Map<String,Socket> temp = new HashMap<>(queue);

                    List<Map.Entry<String, Socket>> entries = new ArrayList<>(queue.entrySet());
                    entries.subList(0, 2).clear();

                    queue.clear();


                    for (Map.Entry<String, Socket> entry : entries) {
                        queue.put(entry.getKey(), entry.getValue());
                    }

                    executor.submit(() -> {
                        try {

                            playGame(temp, socketList,games);

                            synchronized (states){
                                for (Map.Entry<String, Socket> entry : temp.entrySet()){
                                    states.put(entry.getKey(),"online");
                                    states.notifyAll();
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });


                }



                for (int i=0;i<rankedQueues.length;i++){ //Ranked Queue
                    if (rankedQueues[i].getQueueSize() == maxPlayers){
                        //startGame
                        List<Socket> socketList = new ArrayList<>(rankedQueues[i].getQueue().values());
                        Map<String,Socket> temp = new HashMap<>(rankedQueues[i].getQueue());
                        rankedQueues[i].clearQueue();



                        executor.submit(() -> {
                            try {
                                playGame(temp, socketList,games);

                                synchronized (states){
                                    for (Map.Entry<String, Socket> entry : temp.entrySet()){
                                        states.put(entry.getKey(),"online");
                                        states.notifyAll();
                                    }
                                }

                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    else if (rankedQueues[i].getQueueSize() > 0){ //increment range
                        if(rankedQueues[i].getStartTime() == -1){
                            rankedQueues[i].initTime();
                        }
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - rankedQueues[i].getStartTime() >= 30000) {
                            rankedQueues[i].updateRange();
                            rankedQueues[i].newTime(currentTime);
                            for (int j=0;j<rankedQueues.length;j++){
                                if (j != i && rankedQueues[i].getQueueSize() >= rankedQueues[j].getQueueSize()){
                                    for (Map.Entry<String, Float> entry : rankedQueues[j].getScores().entrySet()){
                                        if (canJoin(rankedQueues[i],entry.getValue())){
                                            rankedQueues[i].insertPlayer(entry.getKey(),rankedQueues[j].getQueue().get(entry.getKey()),entry.getValue());
                                            rankedQueues[j].removePlayer(entry.getKey());
                                        }
                                    }
                                }
                            }
                        }

                        Thread.sleep(100); // Sleep for a short period to avoid consuming too much CPU
                    }
                }




            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void updateUserScore(String username, Integer score) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("users/users.txt"));

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(";");
            if (parts.length == 4 && parts[0].equals(username)) {

                // if the username matches, update the score and write the line back to the file

                double newScore = ((Float.parseFloat(parts[2]) * Float.parseFloat(parts[3])) + score) / (Integer.parseInt(parts[3]) + 1);
                newScore = Math.round(newScore * 100.0) / 100.0;
                // or whatever value you want to add

                lines.set(i, parts[0] + ";" + parts[1] + ";" + newScore + ";" + (Integer.parseInt(parts[3]) + 1));
                break; // no need to keep looping once we find the matching username
            }
        }

        Files.write(Paths.get("users/users.txt"), lines);
    }

    public static void playGame(Map<String,Socket> temp, List<Socket> socketList, List<Game> games) throws IOException, InterruptedException {

        Map<Socket,Integer> UserScores = new HashMap<>();
        List<String> userNames = new ArrayList<>(temp.keySet());
        Game game = new Game(temp.size(), socketList, userNames);
        synchronized (games){
            games.add(game);
        }

        System.out.println(temp.size());
        UserScores = game.start();

        Map<String, Integer> ScoresList = new HashMap<>();
        for (int i=0;i<game.getUserSockets().size();i++) {
            String username = game.getUsernames().get(i);
            Socket socket = game.getUserSockets().get(i);
            int score = UserScores.get(socket);
            ScoresList.put(username, score);
            Message messenger = new Message(socket);
            messenger.sendMessageString("info");
            messenger.sendMessageString("Your score was: " + score);
            updateUserScore(username,score);
        }
        List<Map.Entry<String, Integer>> sortedList = ScoresList.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        int position = 1;
        for (Map.Entry<String, Integer> entry : sortedList) {
            String username = entry.getKey();
            int index = game.getUsernames().indexOf(username);
            Socket socket = game.getUserSockets().get(index);
            Message messenger = new Message(socket);
            if (position == 1){
                messenger.sendMessageString("You Win!!");
            }
            messenger.sendMessageString("You finished in position: " + position);
            messenger.sendMessageString("over");
            position++;
        }

        synchronized (games){
            games.remove(game);
        }
    }

    public static float getUserScore(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("users/users.txt"));

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(";");
                if (parts.length == 4 && parts[0].equals(username)) {
                    // if the username matches, return the score
                    return Float.parseFloat(parts[2]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }


    public static float getDifference(float first, float second) {
        float difference = first - second;
        if (difference < 0) {
            return -difference;
        }
        return difference;
    }

    public static boolean canJoin(MyQueue queue, float score){
        if (queue.getMediumRank() + queue.getRange() >= score && queue.getMediumRank() - queue.getRange() <= score){
            return true;
        }
        return false;
    }

    public static int getBestQueue(MyQueue[] rankedQueues, String user, int maxPlayers) {
        int index = 0;
        float userScore = getUserScore(user);
        float bestRankDifference = getDifference(rankedQueues[index].getMediumRank(), userScore);

        for (int i = index + 1 ; i < rankedQueues.length ; i++) {
            float diff = getDifference(rankedQueues[i].getMediumRank(), userScore);
            if (diff < bestRankDifference && rankedQueues[i].getQueueSize() < maxPlayers) { // 2 is max players
                bestRankDifference = diff;
                index = i;
            }
        }


        return index;
    }
}
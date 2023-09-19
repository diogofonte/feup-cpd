package game;

import message.Message;

import java.io.*;
import java.net.*;
import java.util.*;


public class Game {
    private List<Socket> userSockets;
    private  List<String> usernames;
    private static final int MAX_GUESSES = 6;
    private static List<String> words = new ArrayList<>();
    private String target;
    private Map<Socket,Integer> Scores = new HashMap<>();
    private Map<Socket,Boolean> States = new HashMap<>();
    private Map<Socket, Integer> Guesses = new HashMap<>();
    private Map<Socket, Integer> Timeouts = new HashMap<>();
    private Message[] messengers;
    private int players;

    public Game(int players, List<Socket> userSockets, List<String> userNames) throws IOException {
        this.userSockets = userSockets;
        this.usernames = userNames;
        this.players = players;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("words/words.txt"));
            String line = reader.readLine();
            while (line != null) {
                words.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Random rand = new Random();
        this.target = words.get(rand.nextInt(words.size()));

        this.messengers = new Message[this.userSockets.size()];

        System.out.println("Starting game with " + this.userSockets.size() + " players");

    }

    /*
    Point System:
    Win: 500
    guesses: -50

    */

    public Map<Socket, Integer> start() throws IOException, InterruptedException {

        for (int i=0; i<this.userSockets.size();i++){
            Message messenger = new Message(this.userSockets.get(i));
            messengers[i] = messenger;
            Scores.put(this.userSockets.get(i),0);
            States.put(this.userSockets.get(i),false); // they haven't won
            Guesses.put(this.userSockets.get(i),MAX_GUESSES);
            Timeouts.put(this.userSockets.get(i),0);
            messenger.sendMessageString("info");
            messenger.sendMessageString("The Game is Starting");
            messenger.sendMessageString("You are player Nº " + (i + 1));
            messenger.sendMessageString("over");
        }




        System.out.println(this.target);


        boolean finished = false;
        while (!finished) {
            for (int i=0; i<this.players;i++){
                if (!States.get(this.userSockets.get(i))){
                    boolean validGuess = false;
                    Message messenger = messengers[i];

                    for (int j=0;j<this.userSockets.size();j++){
                        if (j == i){
                            messenger.sendMessageString("It is your turn");
                        }
                        else{
                            if (!States.get(this.userSockets.get(j))){
                                Message temp = messengers[j];
                                temp.sendMessageString("info");
                                temp.sendMessageString("It is player's " + (i + 1)  + " turn");
                                temp.sendMessageString("over");
                            }
                        }
                    }
                    String guess = "";
                    while (!validGuess){

                        messenger.sendMessageString("Enter your guess (5 letters): ");
                        messenger.sendMessageString("over");

                        try{

                            guess = messenger.waitForMessageServer(true);
                        }
                        catch (IOException e){
                            synchronized (this.userSockets){
                                if (Timeouts.get(this.userSockets.get(i)) < 10){
                                    Timeouts.put(this.userSockets.get(i),Timeouts.get(this.userSockets.get(i)) + 1);
                                }
                                else{
                                    Scores.put(this.userSockets.get(i), -250);
                                    States.put(this.userSockets.get(i),true);
                                    Guesses.put(this.userSockets.get(i),0);
                                }
                                this.userSockets.wait(5_000);
                            }
                            break;
                        }

                        System.out.println(guess);
                        if (guess.length() == 5){
                            validGuess = true;
                        }
                        else{
                            messenger.sendMessageString("info");
                            messenger.sendMessageString("It has to be a 5 letter word, please enter your guess again");
                            messenger.sendMessageString("over");
                        }
                    }

                    if (!validGuess){
                        continue;
                    }

                    if (guess.equals(this.target)) {
                        messenger.sendMessageString("info");
                        messenger.sendMessageString("You guessed the word!");
                        messenger.sendMessageString("over");
                        Scores.put(this.userSockets.get(i), Scores.get(this.userSockets.get(i)) + 500);
                        States.put(this.userSockets.get(i),true);
                        Guesses.put(this.userSockets.get(i),0);
                        continue;
                    }


                    String feedback = generateFeedback(guess, this.target);
                    messenger.sendMessageString("info");
                    messenger.sendMessageString(feedback);
                    messenger.sendMessageString("over");
                    Scores.put(this.userSockets.get(i), Scores.get(this.userSockets.get(i)) -50);
                    Guesses.put(this.userSockets.get(i),Guesses.get(this.userSockets.get(i)) - 1);
                }
            }
            finished = true;
            for (int i=0; i<this.players;i++){
                if (Guesses.get(userSockets.get(i))>0){
                    finished = false;
                }
            }
        }

        for (int i=0; i<this.userSockets.size();i++){
            if (!States.get(this.userSockets.get(i))){
                Message messenger = messengers[i];
                messenger.sendMessageString("info");
                messenger.sendMessageString("You didn't guess the word.");
                messenger.sendMessageString("The word was " + this.target);
                messenger.sendMessageString("over");
            }

        }



        return Scores;
    }

    
    private static String generateFeedback(String guess, String target) {
        String correctPosLetters = "";
        String correctLetters = "";
        String message = "";

        for (int i=0; i< target.length();i++){
            if (guess.charAt(i) == target.charAt(i)){
                correctPosLetters += (guess.charAt(i) + " ");
            }
            for (int j=0; j<target.length();j++){
                if (guess.charAt(i) == target.charAt(j) && j!=i){
                    correctLetters += (guess.charAt(i) + " ");
                }
            }

        }

        if (correctLetters.length() == 0){
            if (correctPosLetters.length() != 0){
                message = "Letters in the right position: " + correctPosLetters;
            }
            else{
                message = "No letters match";
            }
        }
        else{
            if (correctPosLetters.length() == 0){
                message = "Letters in the word: " + correctLetters;
            }
            else{
                message = "Letters in the word: " + correctLetters + "\n" + "Letters in the right position: " + correctPosLetters;
            }
        }


        return message;
    }

    public void updateUserSocket(String user, Socket socket) throws IOException {
        for (int i=0;i<this.usernames.size();i++){
            if (this.usernames.get(i).equals(user)){
                synchronized (this.userSockets){
                    int score = Scores.get(this.userSockets.get(i));
                    Scores.put(socket, score);
                    Scores.remove(this.userSockets.get(i));
                    boolean state = States.get(this.userSockets.get(i));
                    States.put(socket, state);
                    States.remove(this.userSockets.get(i));
                    int guesses = Guesses.get(this.userSockets.get(i));
                    Guesses.put(socket,guesses);
                    Guesses.remove(this.userSockets.get(i));
                    this.userSockets.set(i,socket);
                    this.messengers[i].updateSocket(socket);
                }
            }
        }
    }

    public List<String> getUsernames(){
        return this.usernames;
    }

    public List<Socket> getUserSockets(){
        return this.userSockets;
    }




    
    
}
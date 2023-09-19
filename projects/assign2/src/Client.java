import java.net.*;
import java.io.*;
import java.util.Scanner;
import message.Message;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);


        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to server: " + hostname + ":" + port);

            boolean connected = true;
            boolean authenticated = false;
            Message messenger = new Message(socket);

            //auth


            while (true){
                String message = messenger.waitForMessage();
                System.out.println(message);
                if (message.contains("Login Successful")){
                    authenticated = true;
                    break;
                }
                messenger.sendMessage();

            }



            //game
            while (connected && authenticated){
                String message = messenger.waitForMessage();
                if (!message.startsWith("info")) {
                    System.out.println(message);


                    messenger.sendMessage();
                }
                else{
                    int index = message.indexOf(System.lineSeparator());
                    message = index >= 0 ? message.substring(index + System.lineSeparator().length()) : "";
                    if (message.startsWith("quit")){
                        connected = false;
                    }
                    else{
                        System.out.println(message);
                    }

                }

            }

            socket.close();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }



    }

}
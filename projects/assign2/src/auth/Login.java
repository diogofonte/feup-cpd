package auth;
import message.Message;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static auth.Register.getSHA256;
import static auth.Register.registerUser;

public class Login {

    private Socket socket;
    public Login(Socket socket){
        this.socket = socket;
    }

    public String authenticate() throws IOException, NoSuchAlgorithmException {

        boolean authenticated = false;
        String username = "";

        Message messenger = new Message(this.socket);
        String[] welcomeMessages = {"", "Welcome to our Server!","", "1. Login", "2. Register","over"};
        messenger.sendMultipleMessages(welcomeMessages);

        while(!authenticated){
            String message = messenger.waitForMessageServer(false);


            switch (message){
                case "1":
                    messenger.sendMessageString("Enter Username:");
                    messenger.sendMessageString("over");
                    message = messenger.waitForMessageServer(false);
                    if (checkUser(message)){
                        username = message;
                        messenger.sendMessageString("Enter Password:");
                        messenger.sendMessageString("over");
                        message = messenger.waitForMessageServer(false);
                        if (checkPassword(username,message)){
                            messenger.sendMessageString("Login Successful");
                            messenger.sendMessageString("over");
                            authenticated = true;
                        }
                        else{
                            username = "";
                            String[] messages = {"Incorrect Password", "", "1. Login", "2. Register","over"};
                            messenger.sendMultipleMessages(messages);
                        }

                    }
                    else{
                        String[] messages = {"User not found", "", "1. Login", "2. Register","over"};
                        messenger.sendMultipleMessages(messages);
                    }

                    break;
                case "2":
                    messenger.sendMessageString("Please write your new username:");
                    messenger.sendMessageString("over");
                    message = messenger.waitForMessageServer(false);
                    if (!checkUser(message)){
                        username = message;
                        messenger.sendMessageString("Please write your new password:");
                        messenger.sendMessageString("over");
                        message = messenger.waitForMessageServer(false);
                        registerUser(username,message);
                        messenger.sendMessageString("Login Successful");
                        messenger.sendMessageString("over");
                        authenticated = true;

                    }
                    else{
                        String[] messages = {"User already exists", "", "1. Login", "2. Register","over"};
                        messenger.sendMultipleMessages(messages);
                    }
                    break;
                default:
                    String[] messages = {"Invalid Option", "", "1. Login", "2. Register","over"};
                    messenger.sendMultipleMessages(messages);
                    break;
            }
        }



        return username;


    }



    public static boolean checkUser(String username) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("users/users.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] words = line.split(";");
            if (words.length < 2) {
                continue;
            }
            String user = words[0];
            if (user.equals(username)) {
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }


    public static boolean checkPassword(String username,String password) throws IOException, NoSuchAlgorithmException {

        BufferedReader reader = new BufferedReader(new FileReader("users/users.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] words = line.split(";");
            if (words.length < 2) {
                continue;
            }
            String user = words[0];
            if (user.equals(username)) {
                String saltedPassword = words[1]; // assuming salted password is stored at index 1
                return saltedPassword.equals(getSHA256(password));
            }


        }
        reader.close();
        return false;
    }







}

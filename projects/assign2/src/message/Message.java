package message;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class Message {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;


    public Message(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        OutputStream output = this.socket.getOutputStream();
        this.writer = new PrintWriter(output, true);
    }

    public void updateSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream output = socket.getOutputStream();
        this.writer = new PrintWriter(output, true);
    }

    public String waitForMessage() throws IOException {
        String message = "";

        boolean messageReceived = false;
        while (!messageReceived) {
            String temp = this.reader.readLine();


            if (temp != null) {
                if (temp.equals("over")){
                    messageReceived = true;
                }
                else{
                    message +=(temp + System.lineSeparator());
                }


            }

        }
        return message;
    }

    public String waitForMessageServer(boolean game) throws IOException {
        String message = "";

        boolean messageReceived = false;
        boolean ackReceived = false;
        long startTime = System.currentTimeMillis();
        int c =0;
        while (!messageReceived) {
            String temp = this.reader.readLine();


            if (temp != null) {
                if (temp.equals("over")){
                    messageReceived = true;
                }
                else if (temp.equals("ack")){
                    ackReceived = true;
                }
                else{
                    message +=temp;
                }


            }

            if (!ackReceived){
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= 5_000 ){
                    throw new IOException();
                }
            }

            if (game){
                if (c >= 20){
                    System.out.println("timeout");
                    throw new IOException();
                }
                c++;
            }





        }
        return message;
    }



    public void sendMessage() throws IOException {
        // Send multiple values to server
        this.writer.println("ack");
        Scanner in = new Scanner(System.in);
        String inputLine;
        inputLine = in.nextLine();
        this.writer.println(inputLine);
        this.writer.println("over");

    }

    public void sendMessageString(String message) throws IOException {
        this.writer.println(message);
    }

    public void sendMultipleMessages(String[] messages){
        for (String message : messages){
            this.writer.println(message);
        }
    }
}

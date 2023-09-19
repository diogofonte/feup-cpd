package auth;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static auth.Login.checkUser;



public class Register {
    private Socket socket;

    public Register(Socket socket){
        this.socket = socket;
    }

    public static void registerUser(String username, String password) throws IOException, NoSuchAlgorithmException {
        String userEntry = username + ";" + getSHA256(password) + ";0;0";
        FileWriter fileWriter = new FileWriter("users/users.txt", true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(userEntry);
        printWriter.close();
        fileWriter.close();


    }

    public static String getSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}

import java.io.*;
import java.net.*;
import java.util.*;
import game.Game;

public class Test{
    
    public static void main(String[] args) throws IOException {
        int players = 0;
        List<Socket> userSockets = new ArrayList<>();
        Game game = new Game(players, userSockets, new ArrayList<String>());

        //game.start();
    }
}
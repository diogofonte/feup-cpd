package rankedQueue;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MyQueue {
    private float mediumRank;
    private int range;
    private long startTime;
    private Map<String, Socket> queue = new HashMap<>();
    private Map<String, Float> scores = new HashMap<>();

    public MyQueue() {
        this.range = 100;
        this.mediumRank = 125;
        this.startTime = -1;
    }

    public void initTime(){
        this.startTime = System.currentTimeMillis();
    }

    public void updateRange(){
        this.range += 100;
    }

    public int getRange(){return this.range;}

    public void newTime(long time){
        this.startTime = time;
    }

    public void insertPlayer(String player, Socket socket, Float score) {
        this.queue.put(player,socket);
        this.scores.put(player,score);
    }

    public int getQueueSize(){
        return this.queue.size();
    }

    public long getStartTime(){
        return this.startTime;
    }

    public Map<String,Socket> getQueue(){
        return this.queue;
    }

    public void updateRank(float rank){
        this.mediumRank = ((this.mediumRank * (this.queue.size() - 1)) + rank) / this.queue.size();
    }

    public float getMediumRank(){
        return this.mediumRank;
    }

    public void clearQueue(){
        this.startTime = -1;
        this.range = 100;
        this.mediumRank = 125;
        this.queue.clear();
    }

    public Map<String,Float> getScores(){
        return this.scores;
    }

    public void removePlayer(String username){
        this.queue.remove(username);
        this.scores.remove(username);
    }




}
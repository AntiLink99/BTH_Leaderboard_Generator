package com.privat.beatthehub;

public class ScoreSubmission implements Comparable<ScoreSubmission>{
    private String song;
    private String username;
    private int score;
    private double accuracy;
    
    public ScoreSubmission(String song, String username, int score, double accuracy) {
        this.song = song;
        this.username = username;
        this.score = score;
        this.accuracy = accuracy;
    }
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getSong() {
        return song;
    }
    public void setSong(String song) {
        this.song = song;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(ScoreSubmission arg) {
        String song = arg.getSong();
        int score = arg.getScore();
        
        if (this.song.equals(song)) {
            return score > this.score ? 1 : -1;
        }
        return this.song.compareTo(song);
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
}

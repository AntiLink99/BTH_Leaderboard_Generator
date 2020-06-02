package com.privat.beatthehub;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

public class ParseBotMessage {
    
    final static String line = "-------------------------------------------------------------------------------------------";
    
    public static void main(String[] args) throws IOException {
        
        final Pattern USERNAME = Pattern.compile("(?<=@).*(?=\\\")");
        final Pattern SCORE = Pattern.compile("(?<=scored\\s).*(?=\\son)");
        final Pattern SONG = Pattern.compile("(?<=on\\s).*(?=!)");

        HashMap<String,Integer> songTotalNotes = new HashMap<String,Integer>();
        songTotalNotes.put("Supernova (ExpertPlus) (Standard)", 959);
        songTotalNotes.put("The Foxs Wedding (ExpertPlus) (Standard)", 1530);
        songTotalNotes.put("Leave The Lights On KROT Remix (Expert) (Standard)", 545);
        songTotalNotes.put("Mizuoto to Curtain (ExpertPlus) (Standard)", 875);
        songTotalNotes.put("Shera  (ExpertPlus) (Standard)", 956);
        songTotalNotes.put("Light It Up (ExpertPlus) (Standard)", 672);
        
        Set<String> validSongs = songTotalNotes.keySet();
        
        File data = new File("src/main/java/com/privat/beatthehub/input.txt");
        
        DecimalFormatSymbols seperator = new DecimalFormatSymbols(Locale.GERMAN);
        seperator.setDecimalSeparator('.');
        DecimalFormat doubleFormat = new DecimalFormat("0.00",seperator);
        DecimalFormat accFormat = new DecimalFormat("0.###",seperator);
        DecimalFormat rankListFormat = new DecimalFormat("000",seperator);
        
        //Parse input.txt
        List<String> lines = FileUtils.readLines(data,"UTF-8");
        
        ArrayList<ScoreSubmission> submissions = new ArrayList<ScoreSubmission>();
        for (String line : lines) {
            Matcher matcherUsername = USERNAME.matcher(line);
            Matcher matcherScore = SCORE.matcher(line);
            Matcher matcherSong = SONG.matcher(line);
            
            if (!matcherUsername.find() || !matcherScore.find() || !matcherSong.find()) {
                continue;
            }
            String username = matcherUsername.group(0);
            int score = Integer.parseInt(matcherScore.group(0));
            String song = matcherSong.group(0);
            
            if (!validSongs.contains(song)) {
                continue;
            }
            
            double accuracy = score / (songTotalNotes.get(song).doubleValue() * 920 - 7245) * 100;
        
            boolean newSubmission = true;
            for (ScoreSubmission sm : submissions) {
                if (sm.getUsername().equals(username) && sm.getSong().equals(song)) {                
                    if (sm.getScore() < score) {
                        sm.setScore(score);
                        sm.setAccuracy(accuracy);
                        newSubmission = false;
                    }
                    else if (sm.getScore() == score) {
                        newSubmission = false;
                    }
                }
            }
            
            if (newSubmission) {
                submissions.add(new ScoreSubmission(song,username,score,accuracy));
            }
        }
        //Sort by 1. song name and 2. score
        Collections.sort(submissions);        
        
        HashMap<String, List<ScoreSubmission>> submissionsBySong = new HashMap<String, List<ScoreSubmission>>();
        for (ScoreSubmission sm : submissions) {
            if (!submissionsBySong.containsKey(sm.getSong())) {
                List<ScoreSubmission> list = new ArrayList<ScoreSubmission>();
                list.add(sm);    
                submissionsBySong.put(sm.getSong(), list);
            } else {
                submissionsBySong.get(sm.getSong()).add(sm);
            }
        }
        
        //Ranks by username
        HashMap<String,ArrayList<Integer>> actualRanksByUsername = new HashMap<String,ArrayList<Integer>>();
        for (Entry<String, List<ScoreSubmission>> set : submissionsBySong.entrySet()) {
            List<ScoreSubmission> songSubmissions = set.getValue();
            for (ScoreSubmission sm : songSubmissions) {
                int rank = (songSubmissions.indexOf(sm)+1);
                
                if(actualRanksByUsername.containsKey(sm.getUsername())) {
                    actualRanksByUsername.get(sm.getUsername()).add(rank);
                }
                else {
                    ArrayList<Integer> ranks = new ArrayList<Integer>();
                    ranks.add(rank);
                    actualRanksByUsername.put(sm.getUsername(), ranks);
                }
            }
        }
        
        //Create player list with actual ranks
        ArrayList<Player> players = new ArrayList<Player>();
        for (Entry<String, ArrayList<Integer>> set : actualRanksByUsername.entrySet()) {
            String username = set.getKey();
            ArrayList<Integer> ranks = set.getValue();
            
            if (ranks.size() == 0) {
                continue;
            }
            players.add(new Player(username, ranks));
        }
        
        //Filtered ranks by username
        HashMap<String,ArrayList<Integer>> filteredRanksByUsername = new HashMap<String,ArrayList<Integer>>();
        for (Entry<String, List<ScoreSubmission>> set : submissionsBySong.entrySet()) {
            List<ScoreSubmission> songSubmissions = set.getValue();
            songSubmissions = songSubmissions.stream().filter(sm -> 
                players.stream().anyMatch(p ->
                    p.getUsername().equals(sm.getUsername()) && p.isQualified()))
                    .collect(Collectors.toList());
            
            for (ScoreSubmission sm : songSubmissions) {
                int rank = (songSubmissions.indexOf(sm)+1);
                
                if(filteredRanksByUsername.containsKey(sm.getUsername())) {
                    filteredRanksByUsername.get(sm.getUsername()).add(rank);
                }
                else {
                    ArrayList<Integer> ranks = new ArrayList<Integer>();
                    ranks.add(rank);
                    filteredRanksByUsername.put(sm.getUsername(), ranks);
                }
            }
        }
        players.forEach(p -> p.setFilteredRanks(filteredRanksByUsername.get(p.getUsername()))); 
        
        //Set total score for every player
        for (ScoreSubmission sm : submissions) {           
            Player player = players.stream().filter(p -> p.getUsername().equals(sm.getUsername())).findFirst().get();            
            ArrayList<ScoreSubmission> bestScores = player.getBestScores();
            if (!bestScores.contains(sm)) {
                player.addToBestScores(sm);
            }
        }
        
        //Set average accuracy for every player
        for (Player p : players) {
            if (p.isQualified()) {
                ArrayList<ScoreSubmission> bestScores = p.getBestScores();
                double averageAcc = bestScores.stream().mapToDouble(sm -> Double.valueOf(sm.getAccuracy())).sum() / 6;
                p.setAverageAcc(averageAcc);
            }
        }
        

        //Build leaderboard
        
        String output = "";
        
        //Build metadata
        String metadata = "";
        metadata += "Parsed on "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))+" (CET)\n";
        metadata += "\nTotal scores: "+submissions.size();
        metadata += "\nPlayers: "+players.size();
        metadata += "\nQualified players: "+players.stream().filter(p -> p.isQualified()).collect(Collectors.toList()).size();
        metadata += "\n\n(You are qualified if you have played all the qualifier maps.)";
        output += metadata;
        
        //Total score
        String totalScoreInfo = "";
        totalScoreInfo += header("Total score");
        
        sortByTotalScore(players);
        for (Player player : players) {
            String username = player.getUsername();
            long totalScore = player.getTotalScore();
            
            int totalScoreRank = players.indexOf(player)+1;
            
            totalScoreInfo += "\n#"+fixedLength(String.valueOf(totalScoreRank),8)
                +fixedLength(username,30)
                +fixedLength(String.valueOf(totalScore),12)
                +(player.isQualified() ? "[Qualified]" : "");
        }
        output += totalScoreInfo;
        
        //Average accuracy
        String averageAccuracyInfo = "";
        averageAccuracyInfo += header("Average accuracy (Qualified only)");
        
        sortByAverageAccuracy(players);
        for (Player player : players) {
            String username = player.getUsername();
            double averageAcc = player.getAverageAcc();
            
            if (averageAcc == 0) {
                continue;
            }
            
            int totalScoreRank = players.indexOf(player)+1;
            
            averageAccuracyInfo += "\n#"+fixedLength(String.valueOf(totalScoreRank),8)
                +fixedLength(username,30)
                +fixedLength(accFormat.format(averageAcc)+"%",12);
        }
        output += averageAccuracyInfo;
        
        
        //Average rank [Qualified]
        String averageInfo = "";
        averageInfo += header("Average rank (Qualified only)");
        
        sortByFilteredAverageRank(players);
        for (Player player : players) {
            String username = player.getUsername();
            ArrayList<Integer> ranks = player.getFilteredRanks();
            if (ranks == null) {
                continue;
            }
            double filteredAverageRank = player.getFilteredAverageRank();
            
            averageInfo += "\n#"+fixedLength(doubleFormat.format(filteredAverageRank),8)
                +fixedLength(username,30)
                +fixedLength(formatRankedList(ranks,rankListFormat),50);
        }

        //Average rank [All]
        averageInfo += header("Average rank (All)");

        sortByAverageRank(players);
        for (Player player : players) {
            String username = player.getUsername();
            ArrayList<Integer> ranks = player.getRanks();
            double averageRank = player.getAverageRank();
            
            averageInfo += "\n#"+fixedLength(doubleFormat.format(averageRank),8)
                +fixedLength(username,28)
                +fixedLength(formatRankedList(ranks,rankListFormat),42)
                +(player.isQualified() ? "[Qualified]" : "");
        }        
        output += averageInfo;
        
        //Song-Leaderboards
        String songInfo = "";
        for (Entry<String, List<ScoreSubmission>> set : submissionsBySong.entrySet()) {
            List<ScoreSubmission> songSubmissions = set.getValue();
            
            songInfo += header(songSubmissions.get(0).getSong());
            
            for (ScoreSubmission sm : songSubmissions) {
                String songInfoLine = fixedLength("\n#"+(songSubmissions.indexOf(sm)+1),8)
                    +fixedLength(sm.getUsername(),28)
                    +fixedLength(String.valueOf(sm.getScore()),10)
                    +accFormat.format(sm.getAccuracy())+"%";
                songInfo += songInfoLine;
            }
        }
        output += songInfo;

        System.out.println(output);
        
        //Save file
        File file = new File("src/main/java/com/privat/beatthehub/Unofficial_BeatTheHub_Leaderboard.txt");    
        FileUtils.writeStringToFile(file,output.trim(),"UTF-8");
    }
    
    private static void sortByAverageRank(ArrayList<Player> players) {
        Comparator<Player> compareByAverageRank = (Player p1, Player p2) ->
            p1.getAverageRank() > p2.getAverageRank() ? 1 : -1;
        Collections.sort(players,compareByAverageRank);    
    }
    
    private static void sortByFilteredAverageRank(ArrayList<Player> players) {
        Comparator<Player> compareByFilteredAverage = (Player p1, Player p2) ->
            p1.getFilteredAverageRank() > p2.getFilteredAverageRank() ? 1 : -1;
        Collections.sort(players,compareByFilteredAverage);    
    }

    private static void sortByTotalScore(ArrayList<Player> players) {
        Comparator<Player> compareByTotalScore = (Player p1, Player p2) -> {
            if(p1.getTotalScore() > p2.getTotalScore()) {
                return -1;
            }
            else if (p1.getTotalScore() < p2.getTotalScore()){
                return 1;
            }
            return 0;
        };
        Collections.sort(players,compareByTotalScore);
    }
    
    private static void sortByAverageAccuracy(ArrayList<Player> players) {
        Comparator<Player> compareByAverageAcc = (Player p1, Player p2) ->
            p1.getAverageAcc() < p2.getAverageAcc() ? 1 : -1;
        Collections.sort(players,compareByAverageAcc);  
    }
    
    private static String fixedLength(String str,int length) {
        return String.format("%0$-"+length+"s", str);
    }
    
    private static String center(String str) {        
        int strLen = (line.length() - str.length()) / 2;
        String add = IntStream.range(0, strLen).mapToObj(i -> " ").collect(Collectors.joining(""));
        return add+str;
    }
    
    private static String header(String title) {
        String header = "";
        header += "\n\n"+line;
        header += "\n"+center(title);
        header += "\n"+line;
        return header;
    }
    
    private static String formatRankedList(ArrayList<Integer> ranks, DecimalFormat format) {
        return fixedLength("[ #"+ranks.stream().map(rank -> 
            format.format(rank)).collect(Collectors.joining(", #")),36)+" ]";        
    }
}

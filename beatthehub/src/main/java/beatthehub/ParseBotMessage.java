package beatthehub;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

import com.itextpdf.text.DocumentException;

import beatthehub.pdf.TxtToPdf;
import beatthehub.scoresaberapi.ScoreSaberAPI;
import beatthehub.tournamentapi.TournamentAPI;

public class ParseBotMessage {

	static String OUTPUT_FILEPATH = "leaderboard"+File.separator;
	final static String FILENAME = "Unofficial_BeatTheHub_Leaderboard";
	
	static ScoreSaberAPI sapi;
	static TournamentAPI tapi;
	
    final static String LINE = "-------------------------------------------------------------------------------------------";
	
    public static void main(String[] args) throws IOException {

    	if (args.length > 0) {
    		OUTPUT_FILEPATH = args[0];
    	}
    	
        sapi = new ScoreSaberAPI();
        sapi.loadDiscordScoreSaberMapping();
        
    	tapi = new TournamentAPI();
    	tapi.fetchAPIData();
        
        DecimalFormatSymbols seperator = new DecimalFormatSymbols(Locale.GERMAN);
        seperator.setDecimalSeparator('.');
        DecimalFormat doubleFormat = new DecimalFormat("0.00",seperator);
        DecimalFormat accFormat = new DecimalFormat("0.###",seperator);
        DecimalFormat rankListFormat = new DecimalFormat("000",seperator);
                
        List<ScoreSubmission> submissions = tapi.getSubmissionsBySongname();
        
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
                int rank = sm.getPlace();
                
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
                    p.getUsername().equals(sm.getUsername()) && p.isParticipating()))
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
            
            if (player.getGroup() == null) {
            	player.setGroup(sm.getTeam().toUpperCase());
            }
        }
        
        //Set average accuracy for every player
        for (Player p : players) {
            if (p.isParticipating()) {
                ArrayList<ScoreSubmission> bestScores = p.getBestScores();
                double averageAcc = bestScores.stream().mapToDouble(sm -> Double.valueOf(sm.getAccuracy())).sum() / 6;
                p.setAverageAcc(averageAcc);
            }
        }

        long groupACount = filterA(players).size();
        long groupAACount = filterAA(players).size();
        long groupAParticipatingCount = filterIsParticipating(filterA(players)).size();
        long groupAAParticipatingCount = filterIsParticipating(filterAA(players)).size();
        
        //Build leaderboard        
        String output = "";
        
        //Build metadata
        String metadata = "";
        OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
        metadata += "Parsed on "+utc.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))+" (UTC)";
        metadata += fixedLength("",45)+"Made by AntiLink99#1337\n";
        metadata += "\nTotal scores: "+submissions.size();
        metadata += "\nPlayers: "+players.size();
        metadata += "\nParticipating players: "+filterIsParticipating(players).size();
        
        metadata += "\n\nPlayers in group A: "+groupACount;
        metadata += "\nParticipating players in group A: "+groupAParticipatingCount;
        metadata += "\n\nPlayers in group AA: "+groupAACount;
        metadata += "\nParticipating players in group AA: "+groupAAParticipatingCount;
      
        sortByAverageAccuracy(players);        
    	List<Integer> scoreSaberRanksA = getScoreSaberRanks(filterA(players).subList(0, 63));
    	Collections.sort(scoreSaberRanksA);    	
    	List<Integer> scoreSaberRanksAA = getScoreSaberRanks(filterAA(players).subList(0, 31));
    	Collections.sort(scoreSaberRanksAA);
    	
        metadata += "\n\nMedian ScoreSaber rank of qualified players in group A: "+getMedianOfIntegers(scoreSaberRanksA);
        metadata += "\nMedian ScoreSaber rank of qualified players in group AA: "+getMedianOfIntegers(scoreSaberRanksAA);
        
        metadata += "\n\n(You are participating if you have played all the qualifier maps.)";
        metadata += "\n\nMR = Mixed rank";
        metadata += "\nGR = Group rank";
        output += metadata;
        
        //Total score
        String totalScoreInfo = "";
        totalScoreInfo += header("Total score");
        totalScoreInfo += "\n"+fixedLength("MR",9)
	    	+fixedLength("GR",9)
	        +fixedLength("Player",30)
	        +fixedLength("Score",12)
	        +fixedLength("Group",8)
	        +"IsParticipating";
        
        sortByTotalScore(players);
        for (Player player : players) {
            String username = player.getUsername();
            long totalScore = player.getTotalScore();
            
            int totalScoreRank = players.indexOf(player)+1;
            int totalScoreGroupRank = players.stream().filter(p ->
            	p.getGroup().equals(player.getGroup()))
            		.collect(Collectors.toList())
            		.indexOf(player)+1;
            
            totalScoreInfo += "\n#"+fixedLength(String.valueOf(totalScoreRank),8)
            	+"#"+fixedLength(String.valueOf(totalScoreGroupRank),8)
                +fixedLength(username,30)
                +fixedLength(String.valueOf(totalScore),12)
                +fixedLength(player.getGroup(),8)
                +(player.isParticipating() ? "Participating" : "");
        }
        output += totalScoreInfo;
        
        //Average accuracy
        String averageAccuracyInfo = "";
        averageAccuracyInfo += header("Average accuracy (Participating only)");
        averageAccuracyInfo += "\n"+fixedLength("MR",9)
	    	+fixedLength("GR",9)
	        +fixedLength("Player",30)
	        +fixedLength("Accuracy",12)
	        +fixedLength("Group",8);
        
        sortByAverageAccuracy(players);
        for (Player player : players) {
            String username = player.getUsername();
            double averageAcc = player.getAverageAcc();
            
            if (averageAcc == 0) {
                continue;
            }
            
            int accuracyRank = players.indexOf(player)+1;
            int accuracyGroupRank = players.stream().filter(p ->
        	p.getGroup().equals(player.getGroup()))
        		.collect(Collectors.toList())
        		.indexOf(player)+1;
            
            averageAccuracyInfo += "\n#"+fixedLength(String.valueOf(accuracyRank),8)
        		+"#"+fixedLength(String.valueOf(accuracyGroupRank),8)
                +fixedLength(username,30)
                +fixedLength(accFormat.format(averageAcc)+"%",12)
                +fixedLength(player.getGroup(),8);
        }
        output += averageAccuracyInfo;
        
        
        //Average rank [Participating]
        String averageInfo = "";
        averageInfo += header("Average rank (Participating only)");
        averageInfo += "\n"+fixedLength("MR",9)
	    	+fixedLength("GR",9)
	        +fixedLength("Player",27)
	        +fixedLength("Ranks",40)
	        +fixedLength("Group",8);
        
        sortByFilteredAverageRank(players);        
        for (Player player : players) {
            String username = player.getUsername();
            ArrayList<Integer> ranks = player.getFilteredRanks();
            if (ranks == null || !player.isParticipating()) {
                continue;
            }
            double filteredAverageRank = player.getFilteredAverageRank();
            int averageRankFilteredGroupRank = players.stream().filter(p ->
        		p.getGroup().equals(player.getGroup()))
        		.collect(Collectors.toList())
        		.indexOf(player)+1;
            
            averageInfo += "\n#"+fixedLength(doubleFormat.format(filteredAverageRank),8)
				+"#"+fixedLength(String.valueOf(averageRankFilteredGroupRank),8)
                +fixedLength(username,27)
                +fixedLength(formatRankedList(ranks,rankListFormat),40)
                +fixedLength(player.getGroup(),5);
        }

        //Average rank [All]
        averageInfo += header("Average rank (All)");
        averageInfo += "\n"+fixedLength("MR",9)
	    	+fixedLength("GR",9)
	        +fixedLength("Player",27)
	        +fixedLength("Ranks",34);

        sortByAverageRank(players);
        for (Player player : players) {
            String username = player.getUsername();
            ArrayList<Integer> ranks = player.getRanks();
            
            double averageRank = player.getAverageRank();
            int averageRankAllGroupRank = players.stream().filter(p ->
	        	p.getGroup().equals(player.getGroup()))
	        		.collect(Collectors.toList())
	        		.indexOf(player)+1;
            
            averageInfo += "\n#"+fixedLength(doubleFormat.format(averageRank),8)
				+"#"+fixedLength(String.valueOf(averageRankAllGroupRank),8)
                +fixedLength(username,27)
                +fixedLength(formatRankedList(ranks,rankListFormat),40)
                +fixedLength(player.getGroup(),5)
                +(player.isParticipating() ? "P" : "");
        }        
        output += averageInfo;
        
        //Song leaderboards
        String songInfo = "";
        for (Entry<String, List<ScoreSubmission>> set : submissionsBySong.entrySet()) {
            List<ScoreSubmission> songSubmissions = set.getValue();
            
            songInfo += header(songSubmissions.get(0).getSong());
            songInfo += "\n"+fixedLength("MR",7)
    	    	+fixedLength("GR",9)
    	        +fixedLength("Player",28)
    	        +fixedLength("Score",10)
    	        +fixedLength("Accuracy",12)
    	        +fixedLength("Group",9)
    	        +"FullCombo";
            
            for (ScoreSubmission sm : songSubmissions) {
                int rank = songSubmissions.indexOf(sm)+1;
                int groupRank = songSubmissions.stream().filter(s ->
                	s.getTeam().equals(sm.getTeam()))
                		.collect(Collectors.toList())
                		.indexOf(sm)+1;
                
                String songInfoLine = fixedLength("\n#"+rank,8)
            		+"#"+fixedLength(String.valueOf(groupRank),8)
                    +fixedLength(sm.getUsername(),28)
                    +fixedLength(String.valueOf(sm.getScore()),10)
                    +fixedLength(accFormat.format(sm.getAccuracy())+"%",12)
                    +fixedLength(sm.getTeam().toUpperCase(),9)
                    +(sm.isFullCombo() ? "FC" : "");
                songInfo += songInfoLine;
            }
        }
        output += songInfo;
        
        //Save file
        File file = new File(OUTPUT_FILEPATH+FILENAME+".txt");    
        FileUtils.writeStringToFile(file,output.trim(),"UTF-8");        
        System.out.println("\n"+file.getName()+" was saved successfully!");
        
        //Convert to .pdf
        try {
        	TxtToPdf.convertTxtToPdf(OUTPUT_FILEPATH+FILENAME);
		} catch (DocumentException e) {
			System.out.println("The .txt file could not be converted to .pdf");
			e.printStackTrace();
		}
        System.out.println(file.getName()+" was converted to .pdf successfully!");
        System.exit(0);
    }

	private static void sortByAverageRank(ArrayList<Player> players) {
        Comparator<Player> compareByAverageRank = (Player p1, Player p2) ->
            p1.getAverageRank() > p2.getAverageRank() ? 1 : -1;
        Collections.sort(players,compareByAverageRank);    
    }
    
    private static void sortByFilteredAverageRank(ArrayList<Player> players) {
        Comparator<Player> compareByFilteredAverage = (Player p1, Player p2) -> {
        	if (!p1.isParticipating()) {
        		return 1;
        	}
        	else if (!p2.isParticipating()) {
        		return -1;
        	}
        	else if(p1.getFilteredAverageRank() > p2.getFilteredAverageRank()) {
            	return 1;
            }
            return -1;
        };
        
        Collections.sort(players,compareByFilteredAverage);    
    }

    private static void sortByTotalScore(ArrayList<Player> players) {
        Comparator<Player> compareByTotalScore = (Player p1, Player p2) ->
        p1.getTotalScore() < p2.getTotalScore() ? 1 : -1;
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
        int strLen = (LINE.length() - str.length()) / 2;
        String add = IntStream.range(0, strLen).mapToObj(i -> " ").collect(Collectors.joining(""));
        return add + str;
    }
    
    private static String header(String title) {
        String header = "";
        header += "\n\n"+LINE;
        header += "\n"+center(title);
        header += "\n"+LINE;
        return header;
    }
    
    private static String formatRankedList(ArrayList<Integer> ranks, DecimalFormat format) {
        return fixedLength("[ #"+ranks.stream().map(rank -> 
            format.format(rank)).collect(Collectors.joining(", #")),36)+" ]";        
    }
    
	private static int getMedianOfIntegers(List<Integer> values) {
    	if (values.size() % 2 == 0) {
    		return (values.get(values.size() / 2) 
    				+ values.get(values.size() / 2 - 1)) / 2;
    	}
		return values.get(values.size() / 2);		
	}
	
	private static List<Integer> getScoreSaberRanks(List<Player> players) {		
		List<Integer> scoreSaberRanks = new ArrayList<Integer>();
		for (Player player : players) {
			try {
				scoreSaberRanks.add(sapi.getScoreSaberRank(player.getUsername()));
			} catch (IOException e) {
				scoreSaberRanks.add(-1);
				e.printStackTrace();
			}
		}
		return scoreSaberRanks;
	}
	
	private static List<Player> filterA(List<Player> players) {
		return players.stream().filter(p -> p.getGroup().equals("A")).collect(Collectors.toList());
	}
	
	private static List<Player> filterAA(List<Player> players) {
		return players.stream().filter(p -> p.getGroup().equals("AA")).collect(Collectors.toList());	
	}
	
	private static List<Player> filterIsParticipating(List<Player> players) {
		return players.stream().filter(p -> p.isParticipating()).collect(Collectors.toList());
	}
}

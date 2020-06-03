package beatthehub.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentSongs {

    List<Song> songs;

    public TournamentSongs(List<Song> songs) {
    	
    	HashMap<String,Integer> noteCounts = new HashMap<String,Integer>();
    	noteCounts.put("Supernova",959);
    	noteCounts.put("The Foxs Wedding", 1530);
    	noteCounts.put("Leave The Lights On KROT Remix", 545);
    	noteCounts.put("Mizuoto to Curtain", 875);
    	noteCounts.put("Shera ", 956);
    	noteCounts.put("Light It Up", 672);
    	
    	List<Song> songsWithNotecounts = new ArrayList<Song>();
    	for (Song song : songs) {
    		if (noteCounts.keySet().contains(song.getSongName())) {
    			song.setNoteCount(noteCounts.get(song.getSongName()));
    			songsWithNotecounts.add(song);
    		}
    	}
    	this.songs = songsWithNotecounts;
    }
    
    public int getSongNotecount(String songName) {
    	return findSong(songName).getNoteCount();
    }
    
    public String getSongHash(String songName) {
    	return findSong(songName).getSongHash();
    }
    
    public int getSongDifficulty(String songName) {
    	return findSong(songName).getDifficulty();
    }
    
    public List<String> getSongNames() {
    	return songs.stream().map(s -> s.getSongName()).collect(Collectors.toList());
    }
    
    private Song findSong(String songName) {
    	return songs.stream().filter(s -> s.getSongName().equals(songName)).findFirst().get();
    }
}

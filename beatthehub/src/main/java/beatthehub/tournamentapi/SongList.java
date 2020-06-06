package beatthehub.tournamentapi;

import java.util.List;

public class SongList {
	List<Song> songs;
	
	public SongList(List<Song> songs) {
		this.songs = songs;
	}
	
	public List<Song> getSongs() {
		return songs;
	}
}

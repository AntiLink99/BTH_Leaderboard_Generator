package beatthehub.api;

public class Song {

	private String songName;
	private int noteCount;
	private int difficulty;
	private String songHash;

	public Song(String songName,String songHash) {
		this.songName = songName;
		this.songHash = songHash;
	}
	
	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public int getNoteCount() {
		return noteCount;
	}

	public void setNoteCount(int noteCount) {
		this.noteCount = noteCount;
	}

	public String getSongHash() {
		return songHash;
	}

	public void setSongHash(String songHash) {
		this.songHash = songHash;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}
}

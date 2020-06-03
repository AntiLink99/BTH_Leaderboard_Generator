package beatthehub;

public class ScoreSubmission implements Comparable<ScoreSubmission> {
	private String song;
	private String player;
	private int score;
	private double accuracy;
	private int place;
	private boolean fullCombo;
	private String team;
	private int songNotecount;

	public ScoreSubmission(String song, String player, int score, double accuracy) {
		this.song = song;
		this.player = player;
		this.score = score;
		this.accuracy = accuracy;
	}

	public ScoreSubmission(String song, String player, int score, int place, boolean fullCombo, String team) {
		this.song = song;
		this.player = player;
		this.score = score;
		this.place = place;
		this.fullCombo = fullCombo;
		this.team = team;
	}

	public String getUsername() {
		return player;
	}

	public void setUsername(String player) {
		this.player = player;
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

	public int getPlace() {
		return place;
	}

	public void setPlace(int place) {
		this.place = place;
	}

	public boolean isFullCombo() {
		return fullCombo;
	}

	public void setFullCombo(boolean fullCombo) {
		this.fullCombo = fullCombo;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public int getSongNotecount() {
		return songNotecount;
	}

	public void setSongNotecount(int songNotecount) {
		this.songNotecount = songNotecount;
		this.accuracy = score / (Double.valueOf(songNotecount) * 920 - 7245) * 100;
	}
}

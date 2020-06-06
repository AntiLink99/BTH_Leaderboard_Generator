package beatthehub.tournamentapi;

import static com.google.gson.FieldNamingPolicy.IDENTITY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beatthehub.ScoreSubmission;
import beatthehub.http.HttpMethods;

public class TournamentAPI {

	HttpMethods http;
	private TournamentSongs info;

	private final String baseUrl = "http://networkauditor.org/api-bth/";
	private final String songUrl = baseUrl + "songs/";
	private final String leaderboardPrefix = baseUrl + "leaderboards/";
	private final String leaderboardSuffix = "Standard/-1/512";
	
	private List<ScoreSubmission> submissionsBySongName;

	public TournamentAPI() {
		http = new HttpMethods();
	}
	
	public void fetchAPIData() throws IOException {
		System.out.println("Fetching data...\n");
		List<Song> fetchedSongs = fetchTournamentSongs(songUrl);
		info = new TournamentSongs(fetchedSongs);
		
		List<ScoreSubmission> fetchedScoreSubmissions = new ArrayList<ScoreSubmission>();
		for (Song song : fetchedSongs) {
			String leaderboardUrl = getSongUrl(song.getSongName());
			List<ScoreSubmission> submissions = fetchSubmissionsBySong(leaderboardUrl);
			
			for (ScoreSubmission sm : submissions) {
				sm.setSong(song.getSongName());
				sm.setUsername(replaceSymbols(sm.getUsername()));
				sm.setSongNotecount(info.getSongNotecount(song.getSongName()));
			}			
			fetchedScoreSubmissions.addAll(submissions);
		}
		this.submissionsBySongName = fetchedScoreSubmissions;
	}

	public String replaceSymbols(String str) {
		return str.replaceAll("[^\\x00-\\x7F]", "?");
	}
	
	private String getSongUrl(String song) {
		return leaderboardPrefix+info.getSongHash(song)+"/"+info.getSongDifficulty(song)+"/"+leaderboardSuffix;
	}
	
	private List<Song> fetchTournamentSongs(String url) throws IOException {
		String response = http.get(url);
		
		//Build song list from response
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(SongList.class, new SongListDeserializer());
		Gson gson = builder.setFieldNamingPolicy(IDENTITY).create();
		return gson.fromJson(response, SongList.class).getSongs();
	}

	private List<ScoreSubmission> fetchSubmissionsBySong(String url) throws IOException {
		String response = http.get(url);
		
		//Build submission list from response
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ScoreSubmissionList.class, new ScoreSubmissionDeserializer());
		Gson gson = builder.setFieldNamingPolicy(IDENTITY).create();
		return gson.fromJson(response, ScoreSubmissionList.class).getSubmissions();
	}
	
	public List<ScoreSubmission> getSubmissionsBySongname() {
		return submissionsBySongName;
	}
}

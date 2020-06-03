package beatthehub.api;

import static com.google.gson.FieldNamingPolicy.IDENTITY;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import beatthehub.ScoreSubmission;

public class TournamentAPI {
	
	HttpClient http;
	private TournamentSongs info;

	private final String baseUrl = "http://networkauditor.org/api-bth/";
	private final String songUrl = baseUrl + "songs/";
	private final String leaderboardPrefix = baseUrl + "leaderboards/";
	private final String leaderboardSuffix = "Standard/-1/512";
	
	private List<ScoreSubmission> submissionsBySongName;

	public TournamentAPI() {
		http = new HttpClient();
		http.getParams().setSoTimeout(5000);
		http.getParams().setConnectionManagerTimeout(5000);
		http.getParams().setParameter(HttpClientParams.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
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
		String response = httpGet(url);
		
		//Build song list from response
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(SongList.class, new SongListDeserializer());
		Gson gson = builder.setFieldNamingPolicy(IDENTITY).create();
		return gson.fromJson(response, SongList.class).getSongs();
	}

	private List<ScoreSubmission> fetchSubmissionsBySong(String url) throws IOException {
		String response = httpGet(url);
		
		//Build submission list from response
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ScoreSubmissionList.class, new ScoreSubmissionDeserializer());
		Gson gson = builder.setFieldNamingPolicy(IDENTITY).create();
		return gson.fromJson(response, ScoreSubmissionList.class).getSubmissions();
	}
	
	private String httpGet(String url) throws IOException {
		GetMethod get = new GetMethod(url);
		setAgent(get);
		int statusCode = http.executeMethod(get);
		
		System.out.println(url);
		if (statusCode != 200) {
			throw new HttpException("Songs could not be fetched. Statuscode: "+statusCode);
		}
		
		InputStream response = null;
		try {
			response = get.getResponseBodyAsStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return IOUtils.toString(response,"UTF-8");
	}
	
	private void setAgent(HttpMethod method) {
		method.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
	}
	
	public List<ScoreSubmission> getSubmissionsBySongname() {
		return submissionsBySongName;
	}
}

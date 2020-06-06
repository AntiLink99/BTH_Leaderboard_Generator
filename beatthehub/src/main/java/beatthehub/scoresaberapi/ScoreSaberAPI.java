package beatthehub.scoresaberapi;

import java.io.IOException;

import com.google.gson.Gson;

import beatthehub.http.HttpMethods;

public class ScoreSaberAPI {

	HttpMethods http;
	Gson gson;
	String playerByNameUrl = "https://new.scoresaber.com/api/players/by-name/";
	
	public ScoreSaberAPI() {
		http = new HttpMethods();
		gson = new Gson();
	}
	
	public int getScoreSaberRank(String playername) throws IOException {
		ScoreSaberPlayer player = fetchPlayer(playername);
		return player.getRank();
	}
	
	public ScoreSaberPlayer fetchPlayer(String playername) throws IOException {
		String playerUrl = playerByNameUrl + playername;
		String response = http.get(playerUrl);
		response = response.substring(1,response.length()-1);
		return gson.fromJson(response, ScoreSaberPlayer.class);
	}
}

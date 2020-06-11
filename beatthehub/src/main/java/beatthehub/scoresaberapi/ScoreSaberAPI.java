package beatthehub.scoresaberapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import beatthehub.http.HttpMethods;

public class ScoreSaberAPI {

	HttpMethods http;
	Gson gson;
	final String playerByIdPrefix = "https://new.scoresaber.com/api/player/";
	final String playerByIdSuffix = "/full";
	
	final Pattern playernamePattern = Pattern.compile("(?<=User\\s).*(?=\\ssuccessfully)");
	final Pattern scoreSaberIdPattern = Pattern.compile("(?<=to\\s).*(?=\\swith)");

	final private String mappingDataPath = "src/main/resources/DiscordScoreSaberMapping.txt";
	InputStream fileStream;
	
	HashMap<String,String> discordScoreSaberMap = new HashMap<String,String>();
	
	public ScoreSaberAPI() throws IOException {
		http = new HttpMethods();
		gson = new Gson();

		fileStream = ClassLoader.getSystemResourceAsStream(mappingDataPath);
	}
	
	public void loadDiscordScoreSaberMapping() throws IOException {

		List<String> lines = new BufferedReader(new InputStreamReader(fileStream,
		          StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
		
		for (String line : lines) {
            Matcher matcherPlayername = playernamePattern.matcher(line);
            Matcher matcherScoreSaberId = scoreSaberIdPattern.matcher(line);
            
            if (!matcherPlayername.find() || !matcherScoreSaberId.find()) {
            	continue;
            }
            
            String playername = matcherPlayername.group(0).replaceAll("[^\\x00-\\x7F]", "?");;
            String scoreSaberId = matcherScoreSaberId.group(0);
            
            if (discordScoreSaberMap.containsKey(playername)) {
            	discordScoreSaberMap.replace(playername, scoreSaberId);
            }
            else {
            	discordScoreSaberMap.put(playername, scoreSaberId);
            }
		}
	}
	
	public int getScoreSaberRank(String playername) throws IOException {
		String playerid = findPlayerIdForName(playername);
		if (playerid == null) {
			System.out.println("Player '"+playername+"' could not be found");
			return -1;
		}
		ScoreSaberPlayer player = fetchPlayer(playerid);
		return player.getPlayerInfo().getRank();
	}
	
	private String findPlayerIdForName(String playername) {
		return discordScoreSaberMap.get(playername);
	}

	public ScoreSaberPlayer fetchPlayer(String playerid) throws IOException {
		String playerUrl = playerByIdPrefix + playerid + playerByIdSuffix;
		String response = http.get(playerUrl);
		return gson.fromJson(response, ScoreSaberPlayer.class);
	}
}

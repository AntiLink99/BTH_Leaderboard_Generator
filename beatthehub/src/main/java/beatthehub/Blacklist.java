package beatthehub;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class Blacklist {
	
	final static String blacklistPath = "src/main/resources/Blacklist.txt";
	public static List<String> getBannedPlayernames() {
		InputStream fileStream  = ClassLoader.getSystemResourceAsStream(blacklistPath);;
		List<String> playernames = new BufferedReader(new InputStreamReader(fileStream,
		          StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
		return playernames;
	}
}

package beatthehub.tournamentapi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SongListDeserializer implements JsonDeserializer<SongList>{

	@Override
	public SongList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		JsonObject jsonObject = json.getAsJsonObject();
		List<Song> songs = new ArrayList<Song>();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			
		    //Deserialization
		    Song song = context.deserialize(entry.getValue(), Song.class); 
		    songs.add(song);
		}
		return new SongList(songs);
	}
	
}

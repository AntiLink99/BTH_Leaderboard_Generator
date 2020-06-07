package beatthehub.tournamentapi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import beatthehub.ScoreSubmission;

public class ScoreSubmissionDeserializer implements JsonDeserializer<ScoreSubmissionList>{

	@Override
	public ScoreSubmissionList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		List<ScoreSubmission> submissions = new ArrayList<ScoreSubmission>();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			
		    //Deserialization
			ScoreSubmission submission = context.deserialize(entry.getValue(), ScoreSubmission.class); 
			submissions.add(submission);
		}
		return new ScoreSubmissionList(submissions);
	}

}

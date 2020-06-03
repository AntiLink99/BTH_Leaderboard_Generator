package beatthehub.api;

import java.util.List;

import beatthehub.ScoreSubmission;

public class ScoreSubmissionList {
	private List<ScoreSubmission> submissions;

	public ScoreSubmissionList(List<ScoreSubmission> submissions) {
		this.submissions = submissions;
	}
	
	public List<ScoreSubmission> getSubmissions() {
		return submissions;
	}
}

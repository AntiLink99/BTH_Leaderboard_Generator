package beatthehub;

import java.util.ArrayList;

public class Player {
	
    private String player;
    private String group;
    private ArrayList<Integer> actualRanks;
    private ArrayList<Integer> filteredRanks; // participating only
    private double averageActualRank = -1;
    private double filteredAverageRank = -1;
    private ArrayList<ScoreSubmission> bestScores = new ArrayList<ScoreSubmission>();
    private long totalScore = 0;
    private double averageAcc = 0;
    private int scoreSaberRank = -1;
    private boolean isBanned;

    public Player(String player, ArrayList<Integer> ranks) {
        this.player = player;
        this.actualRanks = ranks;
        this.averageActualRank = calculateAverage(ranks);
    }

    public String getUsername() {
        return player;
    }

    public void setUsername(String player) {
        this.player = player;
    }

    public ArrayList<Integer> getRanks() {
        return actualRanks;
    }

    public void setRanks(ArrayList<Integer> ranks) {
        this.actualRanks = ranks;
    }

    public double getAverageRank() {
        return averageActualRank;
    }

    public void setAverageRank(double averageRank) {
        this.averageActualRank = averageRank;
    }

    public ArrayList<Integer> getFilteredRanks() {
        return filteredRanks;
    }

    public void setFilteredRanks(ArrayList<Integer> filteredRanks) {
        this.filteredRanks = filteredRanks;
        if (filteredRanks != null) {
            this.filteredAverageRank = calculateAverage(filteredRanks);
        }
    }

    public double getFilteredAverageRank() {
        return filteredAverageRank;
    }

    public void setFilteredAverageRank(double filteredAverageRank) {
        this.filteredAverageRank = filteredAverageRank;
    }

    private double calculateAverage(ArrayList<Integer> values) {
        return Double.valueOf(values.stream().mapToInt(Integer::intValue).sum()) / values.size();
    }

    public long getTotalScore() {
        return totalScore;
    }

    private void addToTotalScore(long score) {
        this.totalScore += score;
    }

    public double getAverageAcc() {
        return averageAcc;
    }

    public void setAverageAcc(double averageAcc) {
        this.averageAcc = averageAcc;
    }

    public void addToBestScores(ScoreSubmission sm) {
        bestScores.add(sm);
        addToTotalScore(sm.getScore());
    }

    public ArrayList<ScoreSubmission> getBestScores() {
        return bestScores;
    }

    public boolean isParticipating() {
        return actualRanks.size() == 6;
    }

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getScoreSaberRank() {
		return scoreSaberRank;
	}

	public void setScoreSaberRank(int scoreSaberRank) {
		this.scoreSaberRank = scoreSaberRank;
	}

	public boolean isBanned() {
		return isBanned;
	}

	public void setBanned(boolean isBanned) {
		this.isBanned = isBanned;
	}
}

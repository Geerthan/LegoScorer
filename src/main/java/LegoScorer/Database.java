package LegoScorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Database {
	
	public static void createGameFolder() {
		
		File gameDir = new File("src/main/resources/games");
		
		if(!gameDir.exists())
			gameDir.mkdir();
		
	}
	
	public static void createTournamentFolder() {
		
		File tournamentDir = new File("src/main/resources/tournaments");
		
		if(!tournamentDir.exists())
			tournamentDir.mkdir();
		
	}
	
	public static String createTournamentFile(File tournamentFile, File gameFile, File teamFile) {
		
		ArrayList<String> teams;
		
		try {
			teams = getTeams(teamFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		ArrayList<String> gameFileStr = new ArrayList<String>();
		BufferedReader in;
		
		try {
			in = new BufferedReader(new FileReader(gameFile));
			String str;
			
			while((str = in.readLine()) != null)
				gameFileStr.add(str);
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter(tournamentFile));
			
			//File format: Start with modified game file, then team list, then tournament data
			writer.write(gameFileStr.get(0) + "\n" + gameFileStr.get(1) + "\n");
			
			//Amount of scoring fields
			writer.write(gameFileStr.size()/2-1 + "\n");
			
			//Each scoring field
			for(int i = 2;i < gameFileStr.size();i++) {
				writer.write(gameFileStr.get(i) + "\n");
			}
			
			//Each team name
			for(int i = 0;i < teams.size();i++)
				if(!teams.get(i).isBlank())
					writer.write(teams.get(i) + "\n");
			
			//End of team list
			writer.write("\n");
			
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		return "File save successful.";
		
	}

	public static String createGameType(String gameName, int teamAmt, int timeAmt, 
			String[] uniqueScoreStrs, double[] uniqueScorePtVals, String[] repeatScoreStrs, double[] repeatScorePtVals) {
		
		createGameFolder();
		
		File gameFile = new File("src/main/resources/games/" + gameName + ".gdat");
		if(gameFile.exists())
			return "A game type with this name already exists, please choose a unique name.";
		
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(gameFile));
			
			//File format: Start with game name, team amount, and time per game
			//All values are separated by newlines, a char that cannot be inputted into any label (game name, score labels)
			writer.write(gameName + "\n");
			writer.write(teamAmt + "\n");
			writer.write(timeAmt + "\n");
			
			//Use two lines for each scoring field, one for name and one for points
			//Unique scoring elements start with "u "
			for(int i = 0;i < uniqueScoreStrs.length;i++) {
				writer.write("u " + uniqueScoreStrs[i] + "\n");
				writer.write(uniqueScorePtVals[i] + "\n");
			}
			
			//Repeat scoring elements start with "r "
			for(int i = 0;i < repeatScoreStrs.length;i++) {
				
				writer.write("r " + repeatScoreStrs[i] + "\n");
				writer.write("" + repeatScorePtVals[i]);
				
				if(i != repeatScoreStrs.length-1) 
					writer.write("\n");
				
			}
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		return "";
		
	}
	
	public static ArrayList<String> getTeams(File file) throws IOException {
		
		ArrayList<String> teams = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		
		while((str = in.readLine()) != null) {
			if(str.charAt(str.length()-1) == ',')
				teams.add(str.substring(0, str.length()-1));
			else teams.add(str);		
		}
		
		in.close();
		
		return teams;
		
	}
	
	public static ObservableList<String> getObsTeams(File file) throws IOException {
		return FXCollections.observableArrayList(getTeams(file));
	}

	public static int getTeamAmt(File file) throws IOException {
		return getTeams(file).size();
	}
	
	public static int getTeamsPerMatch(File file) throws IOException {
		
		String str;
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		if(in.readLine() == null) {
			in.close();
			return 0;
		}
		else if((str = in.readLine()) == null) {
			in.close();
			return 0;
		}
		else {
			in.close();
			return Integer.valueOf(str);
		}
		
	}
	
	public static int getTimePerMatch(File file) throws IOException {
		
		String str;
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		if(in.readLine() == null) {
			in.close();
			return 0;
		}
		else if((str = in.readLine()) == null) {
			in.close();
			return 0;
		}
		else if((str = in.readLine()) == null) {
			in.close();
			return 0;
		}
		else {
			in.close();
			return Integer.valueOf(str);
		}
		
	}
	
	public static String createSchedule(File gameFile, File teamFile, int teamMatchCount) {
		
		int teamsPerMatch;
		
		try {
			teamsPerMatch = getTeamsPerMatch(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		int teamAmt = 0;
		
		try {
			teamAmt = getTeamAmt(teamFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		int teamIter = 0;
		int curTeam = 1;
		
		int scheduledTeamCount = 0;
		
		int[] teamList = new int[teamAmt];
		boolean[] teamSelect = new boolean[teamAmt];
		
		for(int i = 0;i < teamAmt;i++) {
			teamList[i] = i+1;
			teamSelect[i] = false;
		}
		
		for(int i = 0;i < teamMatchCount;i++) {
			
			teamIter++;
			if(teamIter == teamAmt) teamIter = 1;
			curTeam = 1;
			
			for(int j = 0;j < teamAmt;j++) teamSelect[j] = false;
			
			for(int j = 0;j < teamAmt;j++) {
				
				while(teamSelect[curTeam-1]) {
					curTeam++;
					if(curTeam > teamAmt) curTeam %= teamAmt;
				}
				
				System.out.print(curTeam + " "); //Select a team
				teamSelect[curTeam-1] = true;
				
				scheduledTeamCount++;
				if(scheduledTeamCount % teamsPerMatch == 0) System.out.println();
				
				curTeam += teamIter;
				if(curTeam > teamAmt) curTeam %= teamAmt;
				
			}
			
			if(i == teamMatchCount-1 && scheduledTeamCount % teamsPerMatch != 0) {
				
				System.out.print(" |fill| ");
				
				teamIter++;
				if(teamIter == teamAmt) teamIter = 1;
				curTeam = 1;
				
				for(int j = 0;j < teamAmt;j++) teamSelect[j] = false;
				
				for(int j = 0;j < teamsPerMatch - (scheduledTeamCount % teamsPerMatch);j++) {
					
					while(teamSelect[curTeam-1]) {
						curTeam++;
						if(curTeam > teamAmt) curTeam %= teamAmt;
					}
					
					System.out.print(curTeam + " "); //Select a team
					teamSelect[curTeam-1] = true;
					
					//scheduledTeamCount++;
					//if(scheduledTeamCount % teamsPerMatch == 0) System.out.println();
					
					curTeam += teamIter;
					if(curTeam > teamAmt) curTeam %= teamAmt;
					
				}
				
			}
			
		}
		
		return "";
		
	}

	public static String getTotalMatchCount(File gameFile, File teamFile, int teamMatchCount) {
		
		int teamsPerMatch;
		
		try {
			teamsPerMatch = getTeamsPerMatch(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "-1";
		}
		
		int teamAmt = 0;
		
		try {
			teamAmt = getTeamAmt(teamFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "-1";
		}
		
		return "" + (int) Math.ceil(((double) teamAmt * teamMatchCount) / teamsPerMatch);
		
	}
	
	public static String getMatchBreakTime(File gameFile, File teamFile, int teamMatchCount, 
			int startTime, int endTime) {
		
		String totalMatchCount = getTotalMatchCount(gameFile, teamFile, teamMatchCount);
		if(totalMatchCount == "-1") return "-1";
		
		int matchCountInt = Integer.valueOf(totalMatchCount);
		int timePerMatch;
		try {
			timePerMatch = getTimePerMatch(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		//TODO have TimeFields work on hr:min and min:sec
		int totTimeMin = ((endTime - startTime) / 100 * 60) + ((endTime - startTime) % 100);
		double timePerMatchMin = (timePerMatch / 100) + (timePerMatch % 100 / 60);

		double breakTime = (totTimeMin - (matchCountInt * timePerMatchMin)) / (matchCountInt - 1);
		if(breakTime < 0) return "ERROR";
		
		String breakTimeSec = "" + (int) ((breakTime % 1) * 60);
		if(breakTimeSec.length() == 1) breakTimeSec = "0" + breakTimeSec;
		
		String breakTimeStr = ""  + (int) breakTime + ":" + breakTimeSec;

		return breakTimeStr;
		
	}
	
}

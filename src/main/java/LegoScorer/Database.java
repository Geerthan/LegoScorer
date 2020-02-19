package LegoScorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
	
	public static String createTournamentFile(File tournamentFile, File gameFile, File teamFile, int startTime, int endTime, int teamMatchCount) {
		
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
		
		int teamsPerMatch;
		try {
			teamsPerMatch = getTeamsPerMatch(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		int[][] schedule;
		
		try {
			schedule = createSchedule(gameFile, teamFile, teamMatchCount);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		int gameTime;
		
		try {
			gameTime = getTimePerMatch(gameFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		int breakTime = Integer.valueOf(getMatchBreakTime(gameFile, teamFile, teamMatchCount, startTime, endTime).replace(":", ""));
		double timeDif = (gameTime / 100) + (breakTime / 100) + (gameTime % 100 / 60.0) + (breakTime % 100 / 60.0);
		
		double curTime = (startTime / 100 * 60) + (startTime % 100);
		
		for(int i = 0;i < schedule.length;i++) {
			schedule[i][0] = ((int) curTime / 60 * 100) + (int) (curTime % 60);
			curTime += timeDif;
		}
		
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter(tournamentFile));
			
			//File format: Start with modified game file, then team list, then tournament data
			writer.write(gameFileStr.get(0) + "\n" + gameFileStr.get(1) + "\n" + gameFileStr.get(2) + "\n");
			
			//Amount of scoring fields
			writer.write(gameFileStr.size()/2-1 + "\n");
			
			//Each scoring field
			for(int i = 3;i < gameFileStr.size();i++) {
				writer.write(gameFileStr.get(i) + "\n");
			}
			
			//Amount of teams
			writer.write(teams.size() + "\n");
			
			//Each team name
			for(int i = 0;i < teams.size();i++)
				if(!teams.get(i).isBlank())
					writer.write(teams.get(i) + "\n");
			
			//Amount of matches
			writer.write(schedule.length + "\n");
			
			//Teams in match, in format: [time team1 team2 .. teamx] (for all teams) [field1 .. fieldx]
			for(int i = 0;i < schedule.length;i++) {
				for(int j = 0;j < schedule[i].length;j++)
					writer.write(schedule[i][j] + " ");
				//gameFileStr.size()/2-1 is the amt of scoring fields
				for(int j = 0;j < (gameFileStr.size()/2-1)*teamsPerMatch;j++) 
					writer.write("0 ");
				writer.write("\n");
			}
			
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		return "";
		
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
	
	public static int[][] createSchedule(File gameFile, File teamFile, int teamMatchCount) throws IOException {
		
		int teamsPerMatch = getTeamsPerMatch(gameFile);
		
		int teamAmt = getTeamAmt(teamFile);
		
		int teamIter = 0;
		int curTeam = 1;
		
		int matchesScheduled = 0, teamsInMatch = 0;
		
		int scheduledTeamCount = 0;
		
		int[] teamList = new int[teamAmt];
		boolean[] teamSelect = new boolean[teamAmt];
		
		int totalMatchCount = Integer.valueOf(getTotalMatchCount(gameFile,  teamFile, teamMatchCount));

		//Schedule[Match#][0] is reserved for timing information
		int[][] schedule = new int[totalMatchCount][teamsPerMatch+1];
		
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
				
				schedule[matchesScheduled][teamsInMatch+1] = curTeam;
				teamsInMatch++;
				if(teamsInMatch == teamsPerMatch) {
					teamsInMatch = 0;
					matchesScheduled++;
				}
				
				scheduledTeamCount++;
				if(scheduledTeamCount % teamsPerMatch == 0) System.out.println();
				
				curTeam += teamIter;
				if(curTeam > teamAmt) curTeam %= teamAmt;
				
			}
			
			if(i == teamMatchCount-1 && scheduledTeamCount % teamsPerMatch != 0) {
				
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
					
					schedule[matchesScheduled][teamsInMatch+1] = curTeam;
					teamsInMatch++;
					if(teamsInMatch == teamsPerMatch) {
						teamsInMatch = 0;
						matchesScheduled++;
					}
					
					curTeam += teamIter;
					if(curTeam > teamAmt) curTeam %= teamAmt;
					
				}
				
			}
			
		}
		
		return schedule;
		
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
		double timePerMatchMin = (timePerMatch / 100) + (timePerMatch % 100 / 60.0);

		double breakTime = (totTimeMin - (matchCountInt * timePerMatchMin)) / (matchCountInt - 1);
		if(breakTime < 0) return "ERROR";
		
		String breakTimeSec = "" + (int) ((breakTime % 1) * 60);
		if(breakTimeSec.length() == 1) breakTimeSec = "0" + breakTimeSec;
		
		String breakTimeStr = ""  + (int) breakTime + ":" + breakTimeSec;

		return breakTimeStr;
		
	}
	
	public static String[] getUniqueScoreFields(File tournamentFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		int fields = Integer.valueOf(in.readLine());
		int uniqueFields = 0;
		int counter = 0;
		
		String[] fieldLines = new String[fields];
		for(int i = 0;i < fields;i++) {
			fieldLines[i] = in.readLine();
			in.readLine(); //Ignore score value
			if(fieldLines[i].charAt(0) == 'u') uniqueFields++;
		}
		
		String[] uniqueScoreFields = new String[uniqueFields];
		for(int i = 0;i < fields;i++) {
			if(fieldLines[i].charAt(0) == 'u') {
				uniqueScoreFields[counter] = fieldLines[i].substring(2);
				counter++;
			}
		}
		
		in.close();
		return uniqueScoreFields;
	}
	
	public static String[] getRepeatScoreFields(File tournamentFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		int fields = Integer.valueOf(in.readLine());
		int repeatFields = 0;
		int counter = 0;
		
		String[] fieldLines = new String[fields];
		for(int i = 0;i < fields;i++) {
			fieldLines[i] = in.readLine();
			in.readLine(); //Ignore score value
			if(fieldLines[i].charAt(0) == 'r') repeatFields++;
		}
		
		String[] repeatScoreFields = new String[repeatFields];
		for(int i = 0;i < fields;i++) {
			if(fieldLines[i].charAt(0) == 'r') {
				repeatScoreFields[counter] = fieldLines[i].substring(2);
				counter++;
			}
		}
		
		in.close();
		return repeatScoreFields;
	}
	
	public static int[][] getSchedule(File tournamentFile) throws IOException {
		
		int[][] errorSched = new int[0][0], schedule;
		String str;
		int amtMatches, teamsPerMatch;
		
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game title
		if(in.readLine() == null) {
			in.close();
			return errorSched;
		}
		
		//Get teams per match
		if((str = in.readLine()) == null) {
			in.close();
			return errorSched;
		}
		
		teamsPerMatch = Integer.valueOf(str);
		
		//Skip time per match
		if(in.readLine() == null) {
			in.close();
			return errorSched;
		}
		
		//Skip through all game files
		if((str = in.readLine()) == null) {
			in.close();
			return errorSched;
		}
		
		for(int i = 0;i < Integer.valueOf(str)*2;i++) {
			if(in.readLine() == null) {
				in.close();
				return errorSched;
			}
		}
		
		//Skip through team list
		if((str = in.readLine()) == null) {
			in.close();
			return errorSched;
		}
		
		for(int i = 0;i < Integer.valueOf(str);i++) {
			if(in.readLine() == null) {
				in.close();
				return errorSched;
			}
		}
		
		//Read amount of matches
		if((str = in.readLine()) == null) {
			in.close();
			return errorSched;
		}
		
		amtMatches = Integer.valueOf(str);
		schedule = new int[amtMatches][teamsPerMatch+1];
		String[] teamList;
		
		for(int i = 0;i < amtMatches;i++) {
			
			if((str = in.readLine()) == null) {
				in.close();
				return errorSched;
			}
			
			teamList = str.split(" ");
			for(int j = 0;j < teamsPerMatch+1;j++)
				schedule[i][j] = Integer.valueOf(teamList[j]);
			
		}
		
		in.close();
		return schedule;
		
	}
	
}

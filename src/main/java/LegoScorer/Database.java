package LegoScorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Database {
	
	public static void createMacDir() {
		
		File macDir = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "LegoScorer");
		
		if(!macDir.exists())
			macDir.mkdir();
		
	}
	
	public static void createGameFolder(String os) {
		
		File gameDir;
		
		if(os == "Windows")
			gameDir = new File("runtime/resources/games");
		else {
			createMacDir();
			gameDir = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "LegoScorer" 
										+ File.separator + "games");
		}
		
		
		if(!gameDir.exists())
			gameDir.mkdir();
		
	}
	
	public static void createTournamentFolder(String os) {
		
		File tournamentDir;
		
		if(os == "Windows")
			tournamentDir = new File("runtime/resources/tournaments");
		else {
			createMacDir();
			tournamentDir = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + "LegoScorer" 
										+ File.separator + "tournaments");
		}
			
		
		if(!tournamentDir.exists())
			tournamentDir.mkdir();
		
	}
	
	public static String createTournamentFile(File tournamentFile, File gameFile, File teamFile, int startTime, int endTime, 
			int breakStartTime, int breakDurationTime, int teamMatchCount) {
		
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
		
		int breakTime = Integer.valueOf(getMatchBreakTime(gameFile, teamFile, teamMatchCount, startTime, endTime, 
				breakStartTime, breakDurationTime).replace(":", ""));
		double timeDif = (gameTime / 100) + (breakTime / 100) + (gameTime % 100 / 60.0) + (breakTime % 100 / 60.0);
		
		double curTime = (startTime / 100 * 60) + (startTime % 100);
		
		double breakStartMin = (breakStartTime/100*60) + (breakStartTime%60);
		double breakDurationMin = (breakDurationTime/100*60) + (breakDurationTime%60);
		boolean breakNeeded = (breakDurationMin != 0);
		
		for(int i = 0;i < schedule.length;i++) {
			schedule[i][0] = ((int) curTime / 60 * 100) + (int) (curTime % 60);
			if(breakNeeded && curTime > breakStartMin) {
				curTime += breakDurationMin;
				curTime -= timeDif;
				breakNeeded = false;
			}
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
	
	public static String genElimsSchedule(File tournamentFile, int[] matchRoundCnts, int[] teamRoundCnts, int[] tpmCnts) throws IOException {
		
		int startRnd = 0;
		
		// Set starting round to first "playable" round
		for(int i = 0;i < 2;i++) {
			if(matchRoundCnts[i] == 0) startRnd++;
			else break;
		}
		
		FileWriter fileWriter = new FileWriter(tournamentFile, true);
		
		//Total match cnt, team cnt, and teams per match
		for(int i = startRnd;i < 3;i++) fileWriter.write("" + matchRoundCnts[i] + " ");
		fileWriter.write("\n");
		
		for(int i = startRnd;i < 3;i++) fileWriter.write("" + teamRoundCnts[i] + " ");
		fileWriter.write("\n");
		
		for(int i = startRnd;i < 3;i++) fileWriter.write("" + tpmCnts[i] + " ");
		fileWriter.write("\n");
		
		//Amount of currently played rounds
		fileWriter.write("0\n");
		
		fileWriter.close();
		
		return genPlayoffRound(tournamentFile, startRnd);
	}
	
	public static String genPlayoffRound(File tournamentFile, int rnd) {
		
		int[] matchRoundCnts;
		int[] teamRoundCnts;
		int[] tpmCnts;
		
		try {
			matchRoundCnts = getMatchRoundCnts(tournamentFile);
			teamRoundCnts = getTeamRoundCnts(tournamentFile);
			tpmCnts = getTPMCnts(tournamentFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		int scoringFields;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
			
			//Skip game data
			for(int i = 0;i < 3;i++)
				in.readLine();
			
			//Get amt of scoring fields
			scoringFields = Integer.valueOf(in.readLine());
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		//Balanced ranking gen (ex. for teams 1-8, matchmake 1-8-2-7, 3-6-4-5 instead of 1-2-3-4, 5-6-7-8)
		int[] roundSeeds = new int[teamRoundCnts[rnd]];
		
		int lo = 1, hi = teamRoundCnts[rnd];
		boolean toggle = false;
			
		for(int i = 0;i < teamRoundCnts[rnd];i++) {
			if(!toggle) {
				roundSeeds[i] = lo;
				lo++;
				toggle = true;
			}
			else {
				roundSeeds[i] = hi;
				hi--;
				toggle = false;
			}
		}
		
		double[][] rankings;
		try {
			if(rnd == 0) rankings = getSortedQualsRankings(tournamentFile);
			else rankings = getSortedElimsRankings(tournamentFile, rnd-1);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		//Now use seeding template on real rankings
		for(int i = 0;i < teamRoundCnts[rnd];i++) {
			roundSeeds[i] = (int) rankings[roundSeeds[i]-1][0];
		}
		
		int teamsPerMatch = tpmCnts[rnd];
		int teamAmt = teamRoundCnts[rnd];
		
		int teamIter = 1;
		int curTeam = 0;
		
		int matchesScheduled = 0;
		int scheduledTeamCount = 0;
		
		boolean[] teamSelect = new boolean[teamAmt];
		
		int totalMatchCount = matchRoundCnts[rnd];

		int[][] schedule = new int[totalMatchCount][teamsPerMatch];
		
		for(int i = 0;i < teamAmt;i++)
			teamSelect[i] = false;
		
		while(matchesScheduled < totalMatchCount) {
			for(int i = 0;i < teamsPerMatch;i++) {
				
				schedule[matchesScheduled][i] = roundSeeds[curTeam];
				teamSelect[curTeam] = true;
				
				scheduledTeamCount++;
				if(scheduledTeamCount % teamAmt == 0) {
					teamIter++;
					curTeam = 0 - teamIter;
					for(int j = 0;j < teamAmt;j++) teamSelect[j] = false;
				}
				
				System.out.print(schedule[matchesScheduled][i] + " ");
				
				curTeam = curTeam + teamIter;
				curTeam = curTeam % teamAmt;
				while(teamSelect[curTeam]) {
					curTeam++;
					curTeam = curTeam % teamAmt;
				}
				
			}
			System.out.println();
			matchesScheduled++;
		}		
		
		FileWriter fileWriter;
		
		try {
			fileWriter = new FileWriter(tournamentFile, true);
			
			for(int j = 0;j < matchRoundCnts[rnd];j++) {
				
				//Team data
				for(int k = 0;k < tpmCnts[rnd];k++) {
					fileWriter.write("" + schedule[j][k] + " ");
				}
				
				//Game data
				for(int l = 0;l < scoringFields * tpmCnts[rnd];l++)
					fileWriter.write("0 ");
				
				fileWriter.write("\n");
			}
			
			fileWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		return "";
	}
	
	public static int[] getMatchRoundCnts(File tournamentFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		//Skip scoring elements
		int lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++) {
			in.readLine();
			in.readLine();
		}
		
		//Skip team names
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip quals matches
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		String[] tokens = in.readLine().split(" ");
		in.close();
		
		int[] matchRoundCnts = new int[tokens.length];
		for(int i = 0;i < tokens.length;i++) {
			matchRoundCnts[i] = Integer.valueOf(tokens[i]);
		}
		
		return matchRoundCnts;
	}
	
	public static int[] getTeamRoundCnts(File tournamentFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		//Skip scoring elements
		int lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++) {
			in.readLine();
			in.readLine();
		}
		
		//Skip team names
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip quals matches
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip match cnts
		in.readLine();
		
		String[] tokens = in.readLine().split(" ");
		in.close();
		
		int[] teamRoundCnts = new int[tokens.length];
		for(int i = 0;i < tokens.length;i++) {
			teamRoundCnts[i] = Integer.valueOf(tokens[i]);
		}
		
		return teamRoundCnts;
	}
	
	public static int[] getTPMCnts(File tournamentFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		//Skip scoring elements
		int lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++) {
			in.readLine();
			in.readLine();
		}
		
		//Skip team names
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip quals matches
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip match and team round cnts
		in.readLine();
		in.readLine();
		
		String[] tokens = in.readLine().split(" ");
		in.close();
		
		int[] tpmCnts = new int[tokens.length];
		for(int i = 0;i < tokens.length;i++) {
			tpmCnts[i] = Integer.valueOf(tokens[i]);
		}
		
		return tpmCnts;
	}
	
	public static boolean hasPlayoffs(File tournamentFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Ignore game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		int lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++) {
			in.readLine();
			in.readLine();
		}
		
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		boolean ans = in.readLine() != null;
		in.close();
		
		return ans;
		
	}
	
	public static int[][] getScoreVals(File tournamentFile, int uniqueFieldAmt, int repeatFieldAmt, int teamAmt, int match) throws IOException {
		
		List<String> lines = Files.readAllLines(tournamentFile.toPath());
		
		int teamsPerMatch = getTeamsPerMatch(tournamentFile);
		
		int[][] scoreVals = new int[teamAmt][uniqueFieldAmt+repeatFieldAmt];
		int lineNum = 3 + 1 + (uniqueFieldAmt + repeatFieldAmt)*2 + 1 + teamAmt + 1 + match;
		String line = lines.get(lineNum);
		
		String[] tokens = line.split(" ");
		
		int activeTeam = 0, itemCnt = 0;
		for(int i = teamsPerMatch+1;i < tokens.length;i++) {
			scoreVals[activeTeam][itemCnt] = Integer.valueOf(tokens[i]);
			
			itemCnt++;
			if(itemCnt == uniqueFieldAmt + repeatFieldAmt) {
				itemCnt = 0;
				activeTeam++;
			}
		}
		
		return scoreVals;
		
	}
	
	public static int[][] getPlayoffsSchedule(File tournamentFile, int rnd) throws IOException {
		
		int[] matchCnt = getMatchRoundCnts(tournamentFile);
		int[] teamCnt = getTeamRoundCnts(tournamentFile);
		
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		int lines;
		String[] tokens;
		int[][] schedule = new int[matchCnt[rnd]][teamCnt[rnd]];
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		//Skip game field info
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++) {
			in.readLine();
			in.readLine();
		}
		
		//Skip team list
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip quals matches
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip match, team count, and teams per match info, and amt of played rounds
		in.readLine();
		in.readLine();
		in.readLine();
		in.readLine();
		
		//Skip any full rounds before current round
		for(int i = 0;i < rnd;i++) {
			for(int j = 0;j < matchCnt[i];j++)
				in.readLine();
		}
		
		for(int i = 0;i < matchCnt[rnd];i++) {
			tokens = in.readLine().split(" ");
			for(int j = 0;j < teamCnt[rnd];j++) //Team count, not total length, as game field values are after team values
				schedule[i][j] = Integer.valueOf(tokens[j]);
		}
		
		in.close();
		
		return schedule;
	}
	
	public static int[][] getPlayoffsScoreVals(File tournamentFile, int fieldCnt, int rnd, int match) throws IOException {
		
		int[][] scoreVals;
		int lines;
		String[] tokens;
		int[] matchCnt = getMatchRoundCnts(tournamentFile);
		int[] teamCnt = getTeamRoundCnts(tournamentFile);
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		//Skip game fields
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++) {
			in.readLine();
			in.readLine();
		}
		
		//Skip teams
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip quals matches
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		//Skip elims match and team data, and amt of played matches
		in.readLine();
		in.readLine();
		in.readLine();
		in.readLine();
		
		for(int i = 0;i < rnd;i++) {
			for(int j = 0;j < matchCnt[i];j++)
				in.readLine();
		}
		
		for(int i = 0;i < match;i++)
			in.readLine();
		
		tokens = in.readLine().split(" ");
		scoreVals = new int[teamCnt[rnd]][fieldCnt];
		
		int activeTeam = 0;
		
		for(int i = teamCnt[rnd];i < tokens.length;i++) {
			//If there has been enough fields to account for one team
			if(i != teamCnt[rnd] && (i-teamCnt[rnd]) % fieldCnt == 0)
				activeTeam++;
			
			//scoreVals[Team number][Item number]
			//i - teamCnt[rnd] equals total item number
			scoreVals[activeTeam][(i - teamCnt[rnd]) % fieldCnt] = Integer.valueOf(tokens[i]);
		}	
		
		in.close();
		
		return scoreVals;
	}
	
	public static double[][] getSortedElimsRankings(File tournamentFile, int rnd) throws IOException {
		int[][] schedule = getPlayoffsSchedule(tournamentFile, rnd);
		double[] uniqueScorePtVals = getUniqueScorePtVals(tournamentFile);
		double[] repeatScorePtVals = getRepeatScorePtVals(tournamentFile);
		int teamAmt = getTournamentTeamAmt(tournamentFile);
		double[] teamScores = new double[teamAmt];
		double curScore;
		int[][] scoreVals;
		
		for(int i = 0;i < teamAmt;i++)
			teamScores[i] = 0;
		
		for(int i = 0;i < schedule.length;i++) {
			scoreVals = getPlayoffsScoreVals(tournamentFile, uniqueScorePtVals.length + repeatScorePtVals.length, rnd, i);
			for(int j = 0;j < schedule[i].length;j++) {
				curScore = 0;
				for(int k = 0;k < uniqueScorePtVals.length;k++) curScore += scoreVals[j][k] * uniqueScorePtVals[k];
				for(int k = 0;k < repeatScorePtVals.length;k++) curScore += scoreVals[j][uniqueScorePtVals.length + k] * repeatScorePtVals[k];
				teamScores[schedule[i][j]-1] += curScore;
			}
		}
		
		double[][] rankings = new double[teamAmt][2];
		for(int i = 0;i < teamAmt;i++) {
			rankings[i][0] = i+1;
			rankings[i][1] = -teamScores[i];
		}
		
		Arrays.sort(rankings, Comparator.comparingDouble(r -> r[1]));
		return rankings;
	}
	
	public static double[][] getSortedQualsRankings(File tournamentFile) throws IOException {
		int[][] schedule = getSchedule(tournamentFile);
		double[] uniqueScorePtVals = getUniqueScorePtVals(tournamentFile);
		double[] repeatScorePtVals = getRepeatScorePtVals(tournamentFile);
		int teamAmt = getTournamentTeamAmt(tournamentFile);
		double[] teamScores = new double[teamAmt];
		double curScore;
		int[][] scoreVals;
		
		for(int i = 0;i < teamAmt;i++)
			teamScores[i] = 0;
		
		for(int i = 0;i < schedule.length;i++) {
			scoreVals = getScoreVals(tournamentFile, uniqueScorePtVals.length, repeatScorePtVals.length, teamAmt, i);
			for(int j = 1;j < schedule[i].length;j++) {
				curScore = 0;
				for(int k = 0;k < uniqueScorePtVals.length;k++) curScore += scoreVals[j-1][k] * uniqueScorePtVals[k];
				for(int k = 0;k < repeatScorePtVals.length;k++) curScore += scoreVals[j-1][uniqueScorePtVals.length + k] * repeatScorePtVals[k];
				teamScores[schedule[i][j]-1] += curScore;
			}
		}
		
		double[][] rankings = new double[teamAmt][2];
		for(int i = 0;i < teamAmt;i++) {
			rankings[i][0] = i+1;
			rankings[i][1] = -teamScores[i];
		}
		
		Arrays.sort(rankings, Comparator.comparingDouble(r -> r[1]));
		return rankings;
	}
	
	public static int getQualsMatchCnt(File tournamentFile) throws IOException {

		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		//Skip game field data
		int lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++) {
			in.readLine();
			in.readLine();
		}
		
		//Skip team list
		lines = Integer.valueOf(in.readLine());
		for(int i = 0;i < lines;i++)
			in.readLine();
		
		lines = Integer.valueOf(in.readLine());
		in.close();
		
		return lines;
		
	}
	
	public static String replaceFileLine(File file, int line, String val) {
		
		List<String> lines;
		
		try {
			lines = Files.readAllLines(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		lines.set(line, val);
		
		try {
			Files.write(file.toPath(), lines);
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR: " + e.toString();
		}
		
		return "";
		
	}

	public static String createGameType(String gameName, int teamAmt, int timeAmt, 
			String[] uniqueScoreStrs, double[] uniqueScorePtVals, String[] repeatScoreStrs, double[] repeatScorePtVals, String os) {
		
		File gameFile;
		
		if(os == "Windows")
			gameFile = new File("runtime/resources/games/" + gameName + ".gdat");
		else {
			gameFile = new File(System.getProperty("user.home") + File.separator + "Documents" 
												+ File.separator + "LegoScorer" + File.separator + "games" + File.separator + gameName + ".gdat");
		}
		
		
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

	public static int getTeamAmt(File teamFile) throws IOException {
		return getTeams(teamFile).size();
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
	
	public static int getTournamentTeamsPerMatch(File tournamentFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip title
		in.readLine();
		
		int teamAmt = Integer.valueOf(in.readLine());
		in.close();
		return teamAmt;
		
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
			int startTime, int endTime, int breakStartTime, int breakDurationTime) {
		
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
		
		// Breaks can be calculated by reducing the actual max time
		if(breakDurationTime != 0) {
			int endTimeMin = (endTime/100*60) + (endTime%60);
			int breakTimeMin = (breakDurationTime/100*60) + (breakDurationTime%60);
			System.out.println(endTime + " " + endTimeMin + " " + breakTimeMin);
			endTimeMin = endTimeMin - breakTimeMin;
			endTime = (endTimeMin/60*100) + (endTimeMin%60);
			System.out.println(endTime + " " + endTimeMin + " " + breakTimeMin);
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
	
	public static int getTournamentTeamAmt(File tournamentFile) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		int fieldAmt = Integer.valueOf(in.readLine());
		for(int i = 0;i < fieldAmt*2;i++)
			in.readLine();
		
		int teamAmt = Integer.valueOf(in.readLine());
		in.close();
		return teamAmt;
		
	}
	
	public static int getTournamentTPM(File tournamentFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		in.readLine();
		int tpm = Integer.valueOf(in.readLine());
		in.close();
		
		return tpm;
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
	
	public static double[] getUniqueScorePtVals(File tournamentFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		int fields = Integer.valueOf(in.readLine());
		int uniqueFields = 0;
		int counter = 0;
		
		String[] fieldLines = new String[fields];
		double[] ptLines = new double[fields];
		for(int i = 0;i < fields;i++) {
			fieldLines[i] = in.readLine();
			ptLines[i] = Double.valueOf(in.readLine()); // Score value multiplier
			if(fieldLines[i].charAt(0) == 'u') uniqueFields++;
		}
		
		double[] uniqueScorePtVals = new double[uniqueFields];
		for(int i = 0;i < fields;i++) {
			if(fieldLines[i].charAt(0) == 'u') {
				uniqueScorePtVals[counter] = ptLines[i];
				counter++;
			}
		}
		
		in.close();
		return uniqueScorePtVals;
	}
	
	public static double[] getRepeatScorePtVals(File tournamentFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(tournamentFile));
		
		//Skip game data
		for(int i = 0;i < 3;i++)
			in.readLine();
		
		int fields = Integer.valueOf(in.readLine());
		int repeatFields = 0;
		int counter = 0;
		
		String[] fieldLines = new String[fields];
		double[] ptLines = new double[fields];
		for(int i = 0;i < fields;i++) {
			fieldLines[i] = in.readLine();
			ptLines[i] = Double.valueOf(in.readLine()); // Score value multiplier
			if(fieldLines[i].charAt(0) == 'r') repeatFields++;
		}
		
		double[] repeatScorePtVals = new double[repeatFields];
		for(int i = 0;i < fields;i++) {
			if(fieldLines[i].charAt(0) == 'r') {
				repeatScorePtVals[counter] = ptLines[i];
				counter++;
			}
		}
		
		in.close();
		return repeatScorePtVals;
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
	
	public static void updateQualsWorkbook(File tournamentFile) throws IOException {
		Workbook book = new XSSFWorkbook();
		Sheet scheduleSheet = book.createSheet("Quals Schedule");
		int[][] schedule = getSchedule(tournamentFile);
		int tpm = getTournamentTPM(tournamentFile);
		Row r;
		Cell c;
		String timeStr;
		
		// Quals Schedule
		r = scheduleSheet.createRow(0);
		c = r.createCell(0);
		c.setCellValue("Match Number");
		c = r.createCell(1);
		c.setCellValue("Match Time");
		
		for(int i = 0;i < tpm;i++) {
			c = r.createCell(i+2);
			c.setCellValue("Team " + (i+1));
		}
		
		for(int i = 0;i < schedule.length;i++) {
			r = scheduleSheet.createRow(i+1);
			c = r.createCell(0);
			c.setCellValue(i+1);
			for(int j = 0;j < schedule[i].length;j++) {
				c = r.createCell(j+1);
				if(j == 0) {
					timeStr = String.valueOf(schedule[i][j]);
					timeStr = timeStr.substring(0, timeStr.length()-2) + ":" + timeStr.substring(timeStr.length()-2);
					c.setCellValue(timeStr);
				}
				else c.setCellValue(schedule[i][j]);
			}
		}
		
		// Quals Scoring
		String[] uniqueScoreFields = getUniqueScoreFields(tournamentFile);
		String[] repeatScoreFields = getRepeatScoreFields(tournamentFile);
		int teamAmt = getTournamentTeamAmt(tournamentFile);
		int[][] scoreVals;
		
		int[][] scoreTable = new int[teamAmt][uniqueScoreFields.length + repeatScoreFields.length];
		for(int i = 0;i < teamAmt;i++) {
			for(int j = 0;j < uniqueScoreFields.length + repeatScoreFields.length;j++)
				scoreTable[i][j] = 0;
		}
		
		Sheet scoreSheet = book.createSheet("Quals Scoring");
		r = scoreSheet.createRow(0);
		c = r.createCell(0);
		c.setCellValue("Team");
		
		for(int i = 0;i < uniqueScoreFields.length;i++) {
			c = r.createCell(i+1);
			c.setCellValue(uniqueScoreFields[i]);
		}
		
		for(int i = 0;i < repeatScoreFields.length;i++) {
			c = r.createCell(i + 1 + uniqueScoreFields.length);
			c.setCellValue(repeatScoreFields[i]);
		}
		
		for(int i = 0;i < schedule.length;i++) {
			scoreVals = getScoreVals(tournamentFile, uniqueScoreFields.length, repeatScoreFields.length, teamAmt, i);
			for(int j = 1;j < schedule[i].length;j++) {
				for(int k = 0;k < uniqueScoreFields.length+repeatScoreFields.length;k++) {
					scoreTable[schedule[i][j]-1][k] += scoreVals[j-1][k];
				}
			}
		}
		
		for(int i = 0;i < scoreTable.length;i++) {
			r = scoreSheet.createRow(i+1);
			c = r.createCell(0);
			c.setCellValue(i+1);
			for(int j = 0;j < uniqueScoreFields.length+repeatScoreFields.length;j++) {
				c = r.createCell(j+1);
				c.setCellValue(scoreTable[i][j]);
			}
		}
		
		String pth = tournamentFile.getPath();
		pth = pth.substring(0, pth.length()-5) + "-quals.xlsx";
		
		OutputStream out = new FileOutputStream(pth);
		book.write(out);
		book.close();
	}
	
	public static void updateElimsWorkbook(File tournamentFile, int rnd) throws IOException {
		Workbook book = new XSSFWorkbook();
		Sheet scheduleSheet = book.createSheet("Elims " + (rnd+1) + " Schedule");
		int[][] schedule = getPlayoffsSchedule(tournamentFile, rnd);
		int tpm = getTPMCnts(tournamentFile)[rnd];
		Row r;
		Cell c;
		String timeStr;
		
		// Elims Schedule
		r = scheduleSheet.createRow(0);
		c = r.createCell(0);
		c.setCellValue("Match Number");
		
		for(int i = 0;i < tpm;i++) {
			c = r.createCell(i+1);
			c.setCellValue("Team " + (i+1));
		}
		
		for(int i = 0;i < schedule.length;i++) {
			r = scheduleSheet.createRow(i+1);
			c = r.createCell(0);
			c.setCellValue(i+1);
			for(int j = 0;j < schedule[i].length;j++) {
				c = r.createCell(j+1);
				c.setCellValue(schedule[i][j]);
			}
		}
		
		// Elims Scoring
		String[] uniqueScoreFields = getUniqueScoreFields(tournamentFile);
		String[] repeatScoreFields = getRepeatScoreFields(tournamentFile);
		int teamAmt = getTeamRoundCnts(tournamentFile)[rnd];
		int tournamentTeamAmt = getTournamentTeamAmt(tournamentFile);
		int[][] scoreVals;
		int curRow = 0;
		boolean[] isPlaying = new boolean[tournamentTeamAmt];
		
		int[][] scoreTable = new int[tournamentTeamAmt][uniqueScoreFields.length + repeatScoreFields.length];
		for(int i = 0;i < tournamentTeamAmt;i++) {
			isPlaying[i] = false;
			for(int j = 0;j < uniqueScoreFields.length + repeatScoreFields.length;j++)
				scoreTable[i][j] = 0;
		}
		
		Sheet scoreSheet = book.createSheet("Elims " + (rnd+1) + " Scoring");
		r = scoreSheet.createRow(0);
		c = r.createCell(0);
		c.setCellValue("Team");
		
		for(int i = 0;i < uniqueScoreFields.length;i++) {
			c = r.createCell(i+1);
			c.setCellValue(uniqueScoreFields[i]);
		}
		
		for(int i = 0;i < repeatScoreFields.length;i++) {
			c = r.createCell(i + 1 + uniqueScoreFields.length);
			c.setCellValue(repeatScoreFields[i]);
		}
		
		for(int i = 0;i < schedule.length;i++) {
			scoreVals = getPlayoffsScoreVals(tournamentFile, uniqueScoreFields.length+repeatScoreFields.length, rnd, i);
			for(int j = 0;j < schedule[i].length;j++) {
				isPlaying[schedule[i][j]-1] = true;
				for(int k = 0;k < uniqueScoreFields.length+repeatScoreFields.length;k++) {
					scoreTable[schedule[i][j]-1][k] += scoreVals[j][k];
				}
			}
		}
		
		for(int i = 0;i < scoreTable.length;i++) {
			if(!isPlaying[i]) continue;
			curRow++;
			r = scoreSheet.createRow(curRow);
			c = r.createCell(0);
			c.setCellValue(i+1);
			for(int j = 0;j < uniqueScoreFields.length+repeatScoreFields.length;j++) {
				c = r.createCell(j+1);
				c.setCellValue(scoreTable[i][j]);
			}
		}
		
		String pth = tournamentFile.getPath();
		pth = pth.substring(0, pth.length()-5) + "-elims" + (rnd+1) + ".xlsx";
		
		OutputStream out = new FileOutputStream(pth);
		book.write(out);
		book.close();
	}
}

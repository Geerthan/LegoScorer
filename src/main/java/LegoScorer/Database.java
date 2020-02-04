package LegoScorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

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

	public static String createGameType(String gameName, int teamAmt, 
			String[] uniqueScoreStrs, double[] uniqueScorePtVals, String[] repeatScoreStrs, double[] repeatScorePtVals) {
		
		createGameFolder();
		
		File gameFile = new File("src/main/resources/games/" + gameName + ".gdat");
		if(gameFile.exists())
			return "A game type with this name already exists, please choose a unique name.";
		
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(gameFile));
			
			//File format: Start with game name and team amount
			//All values are separated by newlines, a char that cannot be inputted into any label (game name, score labels)
			writer.write(gameName + "\n");
			writer.write(teamAmt + "\n");
			
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

//	public static 
	
}

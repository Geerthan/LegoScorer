package LegoScorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Database {
	
	public static void createGameFolder() {
		
		File gameDir = new File("src/main/resources/games");
		
		if(!gameDir.exists())
			gameDir.mkdir();
		
	}
	
	public static String saveGameType(String gameName, int teamAmt, 
			String[] uniqueScoreStrs, double[] uniqueScorePtVals, String[] repeatScoreStrs, double[] repeatScorePtVals) {
		
		createGameFolder();
		
		File gameFile = new File("src/main/resources/games/" + gameName + ".dat");
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
	
	public static ObservableList<String> getTeams(File file) throws IOException {
		
		ArrayList<String> teams = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		
		while((str = in.readLine()) != null)
			teams.add(str.substring(0, str.length()-1));
		
		in.close();
		
		return FXCollections.observableArrayList(teams);
		
	}

}

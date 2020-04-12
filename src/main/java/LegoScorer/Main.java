package LegoScorer;

import java.io.File;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This program manages the Ontario Tech Engineering Robotics Competition.
 * More specifically, it handles match generation and scoring, integrating with Google Sheets.
 * Its main purpose is to reduce workload on competition organizers.
 * All menu scenes are located within their own classes.
 * @author Geerthan
 */

public class Main extends Application {
	
	public static MainMenu mainMenu;
	public static CreateTournamentMenu createTournamentMenu;
	public static CreateGameMenu createGameMenu;
	public static TournamentView tournamentView;
	public static PlayoffsView playoffsView;
	
	public void start(Stage primaryStage) throws Exception {
		
		mainMenu = new MainMenu(primaryStage);
		createTournamentMenu = new CreateTournamentMenu(primaryStage);
		createGameMenu = new CreateGameMenu(primaryStage);
		tournamentView = new TournamentView(primaryStage);
		playoffsView = new PlayoffsView(primaryStage);
		
		primaryStage.setTitle("LegoScorer");
		primaryStage.setResizable(false);
		primaryStage.setScene(mainMenu.getScene());
		primaryStage.show();
		
	}

	public static void main(String[] args) {
		launch(args);
	}
	
}

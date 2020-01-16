package LegoScorer;

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
	
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setTitle("LegoScorer");
		primaryStage.setResizable(false);
		primaryStage.setScene(MainMenu.getScene());
		primaryStage.show();
		
	}

	public static void main(String[] args) {
		launch(args);
	}
	
}

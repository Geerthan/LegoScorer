package LegoScorer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This class handles the creation of the main menu scene.
 * The main menu directs the user to three different options:
 * Creating a tournament, importing a tournament, or creating a game type.
 * @author Geerthan
 */

public class MainMenu {
	
	Stage primaryStage;
	
	public MainMenu(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public Scene getScene() {
		
		VBox root = new VBox(); //Main container of scene
		root.alignmentProperty().set(Pos.CENTER);
		
		Text title = new Text("LegoScorer");
		title.setId("title"); //For css purposes
		
		Text subtitle = new Text("For managing the Engineering Robotics Competition");
		subtitle.setId("subtitle");
		
		root.getChildren().addAll(title, subtitle);
		VBox.setMargin(title, new Insets(0,0,5,0)); //Add spacing below title for visual effect
		
		HBox buttonRow = new HBox();
		buttonRow.alignmentProperty().setValue(Pos.CENTER); //Align to middle of VBox
		
		Button createTournament = new Button("Create Tournament");
		Button importTournament = new Button("Import Tournament");
		Button createGame = new Button("Create Game Type");
		
		//Prevent any button from starting as "focused"
		createTournament.setFocusTraversable(false);
		importTournament.setFocusTraversable(false);
		createGame.setFocusTraversable(false);
		
		createTournament.setId("menu-button");
		importTournament.setId("menu-button");
		createGame.setId("menu-button");
		
		createTournament.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(Main.createTournamentMenu.getScene());
			}
		});
		
		buttonRow.getChildren().addAll(createTournament, importTournament, createGame);
		
		root.getChildren().add(buttonRow);
		VBox.setMargin(buttonRow, new Insets(15,0,0,0));
		
		Scene s = new Scene(root, 500, 150);
		s.getStylesheets().add("main-menu.css");
		return s;
		
	}

}

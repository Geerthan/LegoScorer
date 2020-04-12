package LegoScorer;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

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
		
		String os = System.getProperty("os.name").substring(0, 7);
		
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
		
		importTournament.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				
				Database.createTournamentFolder(os);
				
				FileChooser fileChooser = new FileChooser();
				
				if(os == "Windows")
					fileChooser.setInitialDirectory(new File("runtime/resources/tournaments"));
				else {
					fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Documents" 
														+ File.separator + "LegoScorer" + File.separator + "tournaments"));
				}
				
				fileChooser.getExtensionFilters().add(
						new ExtensionFilter("Tournament Data Files", "*.tdat")
				);
				File tournamentFile = fileChooser.showOpenDialog(primaryStage);
				
				primaryStage.setScene(Main.tournamentView.getScene(tournamentFile));
			}
		});
		
		createGame.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(Main.createGameMenu.getScene());
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

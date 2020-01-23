package LegoScorer;

import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class CreateTournamentMenu {
	
	Stage primaryStage;
	Stage gameChoosePopup;
	
	int completedSteps = 0;
	
	public CreateTournamentMenu(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public Scene getScene() {
		
		VBox root = new VBox();
		
		StackPane topStack = new StackPane();
		
		Rectangle topRect = new Rectangle(500, 50, Color.web("#003C71"));
		
		Image logo = new Image("img/otu_dark.png");
		ImageView logoView = new ImageView(logo);
		logoView.setPreserveRatio(true);
		logoView.setFitHeight(50);
		
		topStack.getChildren().addAll(topRect, logoView);
		
		HBox chooseGameBox = new HBox();
		
		Text chooseGameText = new Text("Choose which game type this tournament is running.");
		Button chooseGameButton = new Button("Choose Game");
		
		chooseGameButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				showGameChoosePopup();
			}
		});
		
		HBox.setMargin(chooseGameText, new Insets(0, 15, 0, 0));
		chooseGameBox.setAlignment(Pos.CENTER_RIGHT);
		chooseGameBox.getChildren().addAll(chooseGameText, chooseGameButton);
		
//		Button importTeamButton = new Button("Import Teams");
//		Button genScheduleButton = new Button("Generate Schedule");
		
		Button exitButton = new Button("Return to Menu");
		exitButton.setId("exit-button");
		
		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(Main.mainMenu.getScene());
			}
		});
		
		VBox.setMargin(chooseGameBox, new Insets(0, 10, 0, 0));
		VBox.setMargin(exitButton, new Insets(0, 10, 0, 0));
		root.setAlignment(Pos.CENTER_RIGHT);
		root.getChildren().addAll(topStack, chooseGameBox, exitButton);
		
//		root.getChildren().addAll(topStack, chooseGameButton, importTeamButton, genScheduleButton, exitButton);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("create-tournament-menu.css");
		return s;
		
	}
	
	public void showGameChoosePopup() {
		
		VBox root = new VBox();
		root.setPadding(new Insets(15));
		root.setAlignment(Pos.CENTER);
		
		String[] gameNames = CreateGameMenu.getGameNames();
		if(gameNames.length == 0) {
			Text noGamesText = new Text("You must make a game first.");
			root.getChildren().add(noGamesText);
		}
		else {
			
			Text chooseGameText = new Text("Choose your game from the list.");
			ComboBox<String> chooseGameBox = 
					new ComboBox<String>(FXCollections.observableList(Arrays.asList(gameNames)));
			chooseGameBox.setId("popup-combobox");
			
			VBox.setMargin(chooseGameBox, new Insets(5, 0, 5, 0));
			root.getChildren().addAll(chooseGameText, chooseGameBox);
		}
		
		Button exitButton = new Button("Exit");
		exitButton.setId("exit-popup-button");
		
		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				gameChoosePopup.hide();
			}
		});
		
		
		root.getChildren().add(exitButton);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("create-tournament-menu.css");
		gameChoosePopup = new Stage();
		gameChoosePopup.setScene(s);
		gameChoosePopup.initModality(Modality.WINDOW_MODAL);
		gameChoosePopup.show();
	}
	
}

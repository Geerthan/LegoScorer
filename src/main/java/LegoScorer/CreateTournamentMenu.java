package LegoScorer;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CreateTournamentMenu {
	
	final String actionTextVal = "Manage the creation of your tournament in this window. "
			+ "Hover over each button for more details.";
	
	final String chooseGameTextVal = "Choose which game type this tournament is running. "
			+ "They are typically stored in the resources/games folder, as \"game name.dat\".";
	
	final String importTeamsTextVal = "Import your team list for the tournament. "
			+ "The team list should be a csv file of the team names (ex. St. Joseph CSS Team A)";
	
	final String genScheduleTextVal = "Generate the match schedule. This button can only be used after choosing a game type "
			+ "and importing a team list.";
	
	Stage primaryStage, popupStage;
	
	File gameFile, teamFile;
	
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
		
		Text actionText = new Text(actionTextVal);
		actionText.setWrappingWidth(250);
		
		HBox.setMargin(actionText, new Insets(0, 50, 0, 15));
		chooseGameBox.setAlignment(Pos.CENTER);
		chooseGameBox.getChildren().add(actionText);
		
		VBox buttonBox = new VBox();
		
		Button chooseGameButton = new Button("Choose Game");
		addActionTextReset(chooseGameButton, actionText);
		
		chooseGameButton.addEventHandler(MouseEvent.MOUSE_ENTERED, 
			new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					actionText.setText(chooseGameTextVal);
				}
			}
		);
		
		chooseGameButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				gameFile = showGameChoosePopup();
				chooseGameButton.setText(gameFile.getName().substring(0, gameFile.getName().length()-4));
				chooseGameButton.setId("completed-button");
			}
		});
		
		Button importTeamButton = new Button("Import Teams");
		addActionTextReset(importTeamButton, actionText);
		
		importTeamButton.addEventHandler(MouseEvent.MOUSE_ENTERED, 
			new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					actionText.setText(importTeamsTextVal);
				}
			}
		);
		
		importTeamButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				showTeamChoosePopup(importTeamButton);
			}
		});
		
		Button genScheduleButton = new Button("Generate Schedule");
		addActionTextReset(genScheduleButton, actionText);
		
		genScheduleButton.addEventHandler(MouseEvent.MOUSE_ENTERED, 
			new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					actionText.setText(genScheduleTextVal);
				}
			}
		);
		
		Button exitButton = new Button("Return to Menu");
		exitButton.setId("exit-button");
		
		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(Main.mainMenu.getScene());
			}
		});
		
		buttonBox.getChildren().addAll(chooseGameButton, importTeamButton, genScheduleButton, exitButton);
		chooseGameBox.getChildren().add(buttonBox);
		
		root.getChildren().addAll(topStack, chooseGameBox);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("create-tournament-menu.css");
		return s;
		
	}
	
	public void addActionTextReset(Button button, Text text) {
		button.addEventHandler(MouseEvent.MOUSE_EXITED, 
			new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					text.setText(actionTextVal);
				}
			}
		);
	}
	
	public File showGameChoosePopup() {
		Database.createGameFolder();
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("src/main/resources/games"));
		fileChooser.getExtensionFilters().add(
				new ExtensionFilter("Game Data Files", "*.dat")
		);
		File gameFile = fileChooser.showOpenDialog(primaryStage);
		
		return gameFile;
	}
	
	public void showTeamChoosePopup(Button button) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(
			new ExtensionFilter("CSV Files", "*.csv")
		);
		File teamFile = fileChooser.showOpenDialog(primaryStage);
		
		VBox root = new VBox();
		root.setPadding(new Insets(15));
		root.setAlignment(Pos.CENTER);
		
		Text teamPreviewText = new Text("Team Preview");
		teamPreviewText.setId("dialog-title-text");
		root.getChildren().add(teamPreviewText);
		
		ListView<String> teamPreviewListView = new ListView<String>();
		teamPreviewListView.setFocusTraversable(false);
		teamPreviewListView.setPrefHeight(120);
		VBox.setMargin(teamPreviewListView, new Insets(0, 10, 10, 10));
		
		try {
			teamPreviewListView.setItems(Database.getTeams(teamFile));
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog(e.toString());
			return;
		}
		root.getChildren().add(teamPreviewListView);
		
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.CENTER);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setId("cancel-popup-button");
		
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				popupStage.hide();
			}
		});
		
		Button importButton = new Button("Import");
		importButton.setId("popup-button");
		
		importButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				CreateTournamentMenu.this.teamFile = teamFile;
				button.setId("completed-button");
				popupStage.hide();
			}
		});
		
		buttonBox.getChildren().addAll(cancelButton, importButton);
		
		root.getChildren().add(buttonBox);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("create-tournament-menu.css");
		popupStage = new Stage();
		popupStage.setScene(s);
		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.show();
	}
	
	public void showErrorDialog(String errorStr) {
		VBox root = new VBox();
		root.setPadding(new Insets(15));
		root.setAlignment(Pos.CENTER);
		
		Text errorText = new Text(errorStr);
		VBox.setMargin(errorText, new Insets(0, 0, 5, 0));
		root.getChildren().add(errorText);
		
		Button exitButton = new Button("Exit");
		exitButton.setId("exit-popup-button");
		
		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				popupStage.hide();
			}
		});
		
		root.getChildren().add(exitButton);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("create-tournament-menu.css");
		popupStage = new Stage();
		popupStage.setScene(s);
		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.show();
	}
	
}

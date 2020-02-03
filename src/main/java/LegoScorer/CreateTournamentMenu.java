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
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
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
	
	final String saveTournamentTextVal = "Choose a save file location for the tournament. "
			+ "Tournaments are stored as .tdat files in the resources/tournaments folder.";
	
	final String genScheduleTextVal = "Generate the match schedule. This button can only be used after choosing a game type "
			+ "and importing a team list.";
	
	Stage primaryStage, popupStage;
	
	File gameFile, teamFile, tournamentFile;
	
	public CreateTournamentMenu(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public Scene getScene() {
		
		//Find better solution for initialization
		gameFile = null; teamFile = null; tournamentFile = null;
		
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
		
		//Move event handlers and button declarations for clarity
		Button genScheduleButton = new Button("Generate Schedule");
		genScheduleButton.setId("disabled-button");
		
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
				chooseGameButton.setText(gameFile.getName().substring(0, gameFile.getName().length()-5));
				chooseGameButton.setId("completed-button");
				updateScheduleColor(genScheduleButton);
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
				showTeamChoosePopup(importTeamButton, genScheduleButton);
			}
		});
		
		Button saveTournamentButton = new Button("Save Tournament");
		addActionTextReset(saveTournamentButton, actionText);
		
		saveTournamentButton.addEventHandler(MouseEvent.MOUSE_ENTERED, 
			new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					actionText.setText(saveTournamentTextVal);
				}
			}
		);
			
		saveTournamentButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				tournamentFile = showTournamentSavePopup();
				saveTournamentButton.setText(tournamentFile.getName()
						.substring(0, tournamentFile.getName().length()-5));
				saveTournamentButton.setId("completed-button");
				updateScheduleColor(genScheduleButton);
			}
		});
		
		addActionTextReset(genScheduleButton, actionText);
		
		genScheduleButton.addEventHandler(MouseEvent.MOUSE_ENTERED, 
			new EventHandler<MouseEvent>() {
				public void handle(MouseEvent e) {
					actionText.setText(genScheduleTextVal);
				}
			}
		);
		
		genScheduleButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
//				if(gameFile == null || teamFile == null || tournamentFile == null) return;
				
				showScheduleConfigPopup();
				
//				showErrorDialog(Database.createTournamentFile(tournamentFile, gameFile, teamFile));
			}
		});
		
		Button exitButton = new Button("Return to Menu");
		exitButton.setId("exit-button");
		
		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(Main.mainMenu.getScene());
			}
		});
		
		buttonBox.getChildren().addAll(chooseGameButton, importTeamButton, 
				saveTournamentButton, genScheduleButton, exitButton);
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
	
	public void updateScheduleColor(Button button) {
		if(gameFile != null && teamFile != null && tournamentFile != null) 
			button.setId("");
	}
	
	public File showGameChoosePopup() {
		Database.createGameFolder();
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("src/main/resources/games"));
		fileChooser.getExtensionFilters().add(
				new ExtensionFilter("Game Data Files", "*.gdat")
		);
		File gameFile = fileChooser.showOpenDialog(primaryStage);
		
		return gameFile;
	}
	
	public File showTournamentSavePopup() {
		
		Database.createTournamentFolder();
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("src/main/resources/tournaments"));
		fileChooser.getExtensionFilters().add(
			new ExtensionFilter("Tournament Data Files", "*.tdat")
		);
		
		File tournamentFile = fileChooser.showSaveDialog(primaryStage);
		return tournamentFile;
		
	}

	public void showTeamChoosePopup(Button teamButton, Button scheduleButton) {
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
			teamPreviewListView.setItems(Database.getObsTeams(teamFile));
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog(e.toString());
			return;
		}
		root.getChildren().add(teamPreviewListView);
		
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.CENTER);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setId("exit-popup-button");
		
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
				teamButton.setId("completed-button");
				updateScheduleColor(scheduleButton);
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
	
	public void showScheduleConfigPopup() {
		
		VBox root = new VBox();
		root.setPadding(new Insets(15));
		root.setAlignment(Pos.CENTER);
		
		TextField startTimeField = new TextField("00:00");
		startTimeField.setTextFormatter(new TextFormatter<String>(field -> {
			
			String text = field.getControlNewText();
			String newText = "";
			
			for(int i = 0;i < text.length();i++) {
				if(newText.length() == 2) newText += ':';
				
				if(Character.isDigit(text.charAt(i)))
					newText += text.charAt(i);
				
				if(newText.length() == 5) break;
			}
			
			while(newText.length() < 5) {
				if(newText.length() == 2) newText += ':';
				newText += '0';
			}
			
			if(Integer.valueOf("" + newText.charAt(0) + newText.charAt(1)) > 12) {
				newText = "24" + newText.substring(2);
			}
			
			if(Integer.valueOf("" + newText.charAt(3) + newText.charAt(4)) > 59) {
				newText = newText.substring(0, 3) + "00";
			}
			
			field.setRange(0, 5);
			field.setText(newText);
			
			if(field.getCaretPosition() == 2) {
				field.setCaretPosition(3);
				field.setAnchor(3);
			}
			
			return field;
			
		}));
		
		TextField endTimeField = new TextField("00:00");
		endTimeField.setTextFormatter(new TextFormatter<String>(field -> {
			
			String text = field.getControlNewText();
			String newText = "";
			
			for(int i = 0;i < text.length();i++) {
				if(newText.length() == 2) newText += ':';
				
				if(Character.isDigit(text.charAt(i)))
					newText += text.charAt(i);
				
				if(newText.length() == 5) break;
			}
			
			while(newText.length() < 5) {
				if(newText.length() == 2) newText += ':';
				newText += '0';
			}
			
			if(Integer.valueOf("" + newText.charAt(0) + newText.charAt(1)) > 12) {
				newText = "24" + newText.substring(2);
			}
			
			if(Integer.valueOf("" + newText.charAt(3) + newText.charAt(4)) > 59) {
				newText = newText.substring(0, 3) + "00";
			}
			
			field.setRange(0, 5);
			field.setText(newText);
			
			if(field.getCaretPosition() == 2) {
				field.setCaretPosition(3);
				field.setAnchor(3);
			}
			
			return field;
			
		}));
		
		root.getChildren().addAll(startTimeField, endTimeField);
		
		Spinner<Integer> matchSpinner = new Spinner<Integer>(1, 400, 50);
		matchSpinner.setEditable(true);
		
//		matchSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
//			if(!newValue) {
//				matchSpinner.increment(0);
//			}
//		});
		
		
		root.getChildren().addAll(matchSpinner);
		
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

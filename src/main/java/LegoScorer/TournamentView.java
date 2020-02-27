package LegoScorer;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TournamentView {
	
	Stage primaryStage, popupStage;
	int[][] schedule;
	String[] uniqueScoreFields, repeatScoreFields;
	GridPane scorePane;
	
	boolean unsaved;
	
	public TournamentView(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public Scene getScene(File tournamentFile) {
		
		VBox root = new VBox();
		
		StackPane topStack = new StackPane();
		
		Rectangle topRect = new Rectangle(1000, 50, Color.web("#003C71"));
		topRect.widthProperty().bind(primaryStage.widthProperty());
		
		Image logo = new Image("img/otu_dark.png");
		ImageView logoView = new ImageView(logo);
		logoView.setPreserveRatio(true);
		logoView.setFitHeight(50);
		
		topStack.getChildren().addAll(topRect, logoView);
		
		root.getChildren().add(topStack);
		
		HBox viewBox = new HBox();
		
		try {
			schedule = Database.getSchedule(tournamentFile);
		} catch (IOException e) {
			e.printStackTrace();
			schedule = new int[0][0];
		}

		try {
			uniqueScoreFields = Database.getUniqueScoreFields(tournamentFile);
		} catch (IOException e) {
			e.printStackTrace();
			uniqueScoreFields = new String[0];
		}
		
		try {
			repeatScoreFields = Database.getRepeatScoreFields(tournamentFile);
		} catch (IOException e) {
			e.printStackTrace();
			repeatScoreFields = new String[0];
		}
		
		ListView<HBox> gameLV = new ListView<HBox>();
		gameLV.setPrefHeight(550);
		gameLV.setMaxWidth(230);
		
		for(int i = 0;i < schedule.length;i++) {
			HBox gameBox = new HBox();
			gameBox.setAlignment(Pos.CENTER_LEFT);
			
			VBox gameData = new VBox();
			gameData.setSpacing(3);
			Label gameNumLabel = new Label("Game " + (i+1));
			
			String teamList = "";
			for(int j = 1;j < schedule[i].length;j++) {
				teamList += schedule[i][j];
				if(j != schedule[i].length-1) teamList += " ";
			}
			Label gameTeamLabel = new Label(teamList);
			gameTeamLabel.setId("team-list-label");
			
			gameData.getChildren().addAll(gameNumLabel, gameTeamLabel);
			
			String gameTime = "" + schedule[i][0];
			if(gameTime.length() == 3) gameTime = "0" + gameTime;
			gameTime = gameTime.substring(0, 2) + ":" + gameTime.substring(2, 4);
			
			Label gameTimeLabel = new Label(gameTime);
			gameTimeLabel.setId("time-label");
			
			gameBox.getChildren().addAll(gameData, gameTimeLabel);
			HBox.setHgrow(gameData, Priority.ALWAYS);
			
			gameLV.getItems().add(gameBox);
			
		}
		
		gameLV.getSelectionModel().select(0);
		
		scorePane = new GridPane();
		scorePane.setAlignment(Pos.TOP_CENTER);
		scorePane.setHgap(25);
		scorePane.setVgap(10);
		scorePane = updateScorePane(scorePane, schedule, uniqueScoreFields, repeatScoreFields, 0);
		
		gameLV.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			if(unsaved)
				showSaveDialog(scorePane, schedule, uniqueScoreFields, repeatScoreFields, (int) newValue);
			else
				scorePane = updateScorePane(scorePane, schedule, uniqueScoreFields, repeatScoreFields, (int) newValue);
		});
		
		viewBox.getChildren().addAll(gameLV, scorePane);
		
		root.getChildren().add(viewBox);
		
		// TODO make resizable
		Scene s = new Scene(root, 1000, 600);
		s.getStylesheets().add("tournament-view.css");
		return s;
		
	}
	
	public GridPane updateScorePane(GridPane scorePane, int[][] schedule, String[] uniqueScoreFields, String[] repeatScoreFields, int match) {
		
		unsaved = false;
		
		scorePane.getChildren().clear();
		scorePane.getColumnConstraints().clear();
		
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHalignment(HPos.RIGHT);
		scorePane.getColumnConstraints().add(labelCol);
		
		for(int i = 1;i < schedule[match].length;i++) {
			ColumnConstraints scoreCol = new ColumnConstraints();
			scoreCol.setHalignment(HPos.CENTER);
			scorePane.getColumnConstraints().add(scoreCol);
		}
		
		Button saveButton = new Button("Save");
		saveButton.setId("disabled-button");
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if(!unsaved) return;
				unsaved = false;
				saveButton.setId("disabled-button");
				//TODO handle saving
			}
		});
		
		scorePane.add(saveButton, schedule[match].length-2, 0, 3, 1);
		
		Label titleLabel = new Label("Game " + (match+1));
		titleLabel.setId("title-label");
		scorePane.add(titleLabel, 1, 0, schedule[match].length-1, 1);
		
		for(int i = 1;i < schedule[match].length;i++) {
			Label teamLabel = new Label("Team " + schedule[match][i]);
			scorePane.add(teamLabel, i, 1);
		}
		
		for(int i = 0;i < uniqueScoreFields.length;i++) {
			Label scoreLabel = new Label(uniqueScoreFields[i]);
			scorePane.add(scoreLabel, 0, i+2);
			for(int j = 1;j < schedule[match].length;j++) {
				CheckBox c = new CheckBox();
				c.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						unsaved = true;
						saveButton.setId("");
					}
				});
				scorePane.add(c, j, i+2);
			}
		}
		
		for(int i = 0;i < repeatScoreFields.length;i++) {
			Label scoreLabel = new Label(repeatScoreFields[i]);
			scorePane.add(scoreLabel, 0, uniqueScoreFields.length+i+2);
			for(int j = 1;j < schedule[match].length;j++) {
				Spinner<Integer> s = new Spinner<Integer>(0, 999, 0);
				s.valueProperty().addListener((obs, oldValue, newValue) -> {
					unsaved = true;
					saveButton.setId("");
				});
				scorePane.add(s, j, uniqueScoreFields.length+i+2);
			}
		}
		
		HBox.setHgrow(scorePane, Priority.ALWAYS);
		return scorePane;
	}

	public void showSaveDialog(GridPane scorePane2, int[][] schedule, String[] uniqueScoreFields, String[] repeatScoreFields, int match) {
		VBox root = new VBox();
		root.setPadding(new Insets(15));
		root.setAlignment(Pos.CENTER);
		
		Text saveText = new Text("You have unsaved changes. Would you like to save?");
		VBox.setMargin(saveText, new Insets(0, 0, 5, 0));
		root.getChildren().add(saveText);
		
		Button noButton = new Button("No");
		noButton.setId("no-button");
		
		noButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				popupStage.hide();
				scorePane = updateScorePane(scorePane2, schedule, uniqueScoreFields, repeatScoreFields, match);
			}
		});
		
		Button saveButton = new Button("Save");
		saveButton.setId("popup-button");
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				// TODO handle saving
				popupStage.hide();
				scorePane = updateScorePane(scorePane2, schedule, uniqueScoreFields, repeatScoreFields, match);
			}
		});
		
		HBox buttonBox = new HBox();
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setSpacing(5);
		buttonBox.getChildren().addAll(noButton, saveButton);
		
		root.getChildren().add(buttonBox);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("tournament-view.css");
		popupStage = new Stage();
		popupStage.setScene(s);
		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.show();
	}
	
}

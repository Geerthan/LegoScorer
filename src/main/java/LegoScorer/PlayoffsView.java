package LegoScorer;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlayoffsView {

	File tournamentFile;
	Stage primaryStage, popupStage;
	String[] uniqueScoreFields, repeatScoreFields;
	GridPane scorePane;
	Button qualsButton, saveButton, nextButton, backButton;
	ListView<HBox> gameLV;
	
	int[][] schedule, scoreVals;
	int[] matchRoundCnt;
	int teamAmt;
	
	int curRound;
	int qualsMatchCnt;
	
	boolean unsaved;
	
	public PlayoffsView(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public Scene getScene(File tournamentFile) {
		return getScene(tournamentFile, 0);
	}
	
	public Scene getScene(File tournamentFile, int curRound) {
		
		this.curRound = curRound;
		
		this.tournamentFile = tournamentFile;
		
		VBox root = new VBox();
		
		StackPane topStack = new StackPane();
		
		Rectangle topRect = new Rectangle(1000, 50, Color.web("#003C71"));
		topRect.widthProperty().bind(primaryStage.widthProperty());
		
		Image logo = new Image(getClass().getResource("/img/otu_dark.png").toExternalForm().toString());
		ImageView logoView = new ImageView(logo);
		logoView.setPreserveRatio(true);
		logoView.setFitHeight(50);
		
		HBox headerButtonBox = new HBox();
		headerButtonBox.setAlignment(Pos.CENTER_RIGHT);
		headerButtonBox.setSpacing(5);
		
		nextButton = new Button("Next Round");
		nextButton.setId("header-button");
		
		backButton = new Button("Previous Round");
		backButton.setId("header-button");
		
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		qualsButton = new Button("Quals Mode");
		qualsButton.setId("orange-header-button");
		
		saveButton = new Button("Save");
		
		headerButtonBox.getChildren().addAll(backButton, nextButton, spacer, qualsButton, saveButton);
		
		StackPane.setMargin(headerButtonBox, new Insets(0, 20, 0, 20));
		topStack.getChildren().addAll(topRect, logoView, headerButtonBox);
		
		root.getChildren().add(topStack);
		
		HBox viewBox = new HBox();
		
		try {
			Database.updateElimsWorkbook(tournamentFile, curRound);
		} catch (IOException e) {
			//TODO Show error message
			e.printStackTrace();
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
		
		try {
			matchRoundCnt = Database.getMatchRoundCnts(tournamentFile);
		} catch (IOException e) {
			e.printStackTrace();
			matchRoundCnt = new int[0];
		}
		
		try {
			qualsMatchCnt = Database.getQualsMatchCnt(tournamentFile);
		} catch (IOException e) {
			e.printStackTrace();
			qualsMatchCnt = -99999;
		}
		
		gameLV = new ListView<HBox>();
		gameLV.setPrefHeight(550);
		gameLV.setMaxWidth(230);
		
		loadElimsSchedule(curRound);
		
		scorePane = new GridPane();
		scorePane.setAlignment(Pos.TOP_CENTER);
		scorePane.setHgap(25);
		scorePane.setVgap(10);
		scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, curRound, 0);
		
		gameLV.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			if(unsaved)
				showSaveDialog(schedule, uniqueScoreFields, repeatScoreFields, scoreVals, curRound, (int) newValue, (int) oldValue);
			else
				scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, curRound, (int) newValue);
		});
		
		viewBox.getChildren().addAll(gameLV, scorePane);
		
		root.getChildren().add(viewBox);
		
		// TODO Make resizable
		Scene s = new Scene(root, 1000, 600);
		s.getStylesheets().add("playoffs-view.css");
		return s;
		
	}
	
	public void loadElimsSchedule(int curRound) {
		
		gameLV.getItems().clear();
		
		try {
			schedule = Database.getPlayoffsSchedule(tournamentFile, curRound);
		} catch (IOException e) {
			e.printStackTrace();
			schedule = new int[0][0];
		}
		
		try {
			teamAmt = Database.getTournamentTeamAmt(tournamentFile);
		} catch (IOException e) {
			e.printStackTrace();
			teamAmt = 0;
			//TODO Show error msg!
		}
		
		for(int i = 0;i < schedule.length;i++) {
			HBox gameBox = new HBox();
			gameBox.setAlignment(Pos.CENTER_LEFT);
			
			VBox gameData = new VBox();
			gameData.setSpacing(3);
			Label gameNumLabel = new Label("Match " + (i+1));
			gameNumLabel.setId("big-label");
			
			String teamList = "";
			for(int j = 0;j < schedule[i].length;j++) {
				teamList += schedule[i][j];
				if(j != schedule[i].length-1) teamList += " ";
			}
			Label gameTeamLabel = new Label(teamList);
			gameTeamLabel.setId("team-list-label");
			
			gameData.getChildren().addAll(gameNumLabel, gameTeamLabel);
			
			String gameTime = "" + (i+1);
			
			Label gameTimeLabel = new Label(gameTime);
			gameTimeLabel.setId("time-label");
			
			gameBox.getChildren().addAll(gameData, gameTimeLabel);
			HBox.setHgrow(gameData, Priority.ALWAYS);
			
			gameLV.getItems().add(gameBox);
			
		}
		
		gameLV.getSelectionModel().select(0);
	}
	
	public GridPane updateScorePane(String[] uniqueScoreFields, String[] repeatScoreFields, int curRound, int match) {
		
		this.curRound = curRound;
		
		try {
			scoreVals = Database.getPlayoffsScoreVals(tournamentFile, uniqueScoreFields.length + repeatScoreFields.length, 
					curRound, match);
		} catch (IOException e) {
			e.printStackTrace();
			scoreVals = new int[0][0];
		}
		
		unsaved = false;
		
		scorePane.getChildren().clear();
		scorePane.getColumnConstraints().clear();
		
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHalignment(HPos.RIGHT);
		scorePane.getColumnConstraints().add(labelCol);
		
		for(int i = 0;i < schedule[match].length;i++) {
			ColumnConstraints scoreCol = new ColumnConstraints();
			scoreCol.setHalignment(HPos.CENTER);
			scorePane.getColumnConstraints().add(scoreCol);
		}
		
		if(curRound == 0) {
			backButton.setId("disabled-header-button");
			backButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					return;
				}
			});
		}
		else {
			backButton.setId("header-button");
			backButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
//					if(unsaved)
//						showSaveDialog(schedule, uniqueScoreFields, repeatScoreFields, scoreVals, curRound, (int) newValue, (int) oldValue);
//					else
					primaryStage.setScene(Main.playoffsView.getScene(tournamentFile, curRound-1));
					//scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, curRound+1, 1);
				}
			});
		}
		
		int totalRnds = matchRoundCnt.length;
		if(curRound == totalRnds-1) {
			nextButton.setId("disabled-header-button");
			nextButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					return;
				}
			});
		}
		else {
			nextButton.setId("header-button");
			nextButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
//					if(unsaved)
//						showSaveDialog(schedule, uniqueScoreFields, repeatScoreFields, scoreVals, curRound, (int) newValue, (int) oldValue);
//					else if ungenerated
//					else
					Database.genPlayoffRound(tournamentFile, curRound+1);
					primaryStage.setScene(Main.playoffsView.getScene(tournamentFile, curRound+1));
				}
			});
		}
		
//		qualsButton.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent e) {
//				//TODO handle this
//				primaryStage.setScene(Main.tournamentView.getScene(tournamentFile));
//			}
//		});
		
		saveButton.setId("disabled-header-button");
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if(!unsaved) return;
				unsaved = false;
				saveButton.setId("disabled-header-button");
				save(schedule, uniqueScoreFields, repeatScoreFields, curRound, match);
			}
		});
		
		Label titleLabel = new Label("Eliminations Round " + (curRound+1) + "\nMatch " + (match+1));
		titleLabel.setId("title-label");
		titleLabel.setTextAlignment(TextAlignment.CENTER);
		scorePane.add(titleLabel, 1, 0, schedule[match].length, 1);
		
		for(int i = 0;i < schedule[match].length;i++) {
			Label teamLabel = new Label("Team " + schedule[match][i]);
			teamLabel.setId("big-label");
			scorePane.add(teamLabel, i+1, 1);
		}
		
		for(int i = 0;i < uniqueScoreFields.length;i++) {
			Label scoreLabel = new Label(uniqueScoreFields[i]);
			scoreLabel.setId("big-label");
			scoreLabel.setMaxWidth(120);
			scorePane.add(scoreLabel, 0, i+2);
			for(int j = 0;j < schedule[match].length;j++) {
				CheckBox c = new CheckBox();
				if(scoreVals[j][i] == 1) c.setSelected(true);
				c.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						unsaved = true;
						saveButton.setId("header-button");
					}
				});
				scorePane.add(c, j+1, i+2);
			}
		}
		
		for(int i = 0;i < repeatScoreFields.length;i++) {
			Label scoreLabel = new Label(repeatScoreFields[i]);
			scoreLabel.setId("big-label");
			scoreLabel.setMaxWidth(120);
			scorePane.add(scoreLabel, 0, uniqueScoreFields.length+i+2);
			for(int j = 0;j < schedule[match].length;j++) {
				Spinner<Integer> s = new Spinner<Integer>(0, 999, 0);
				s.getValueFactory().setValue(scoreVals[j][uniqueScoreFields.length+i]);
				s.valueProperty().addListener((obs, oldValue, newValue) -> {
					unsaved = true;
					saveButton.setId("header-button");
				});
				scorePane.add(s, j+1, uniqueScoreFields.length+i+2);
			}
		}
		
		HBox.setHgrow(scorePane, Priority.ALWAYS);
		
		return scorePane;
	}
	
	public void showSaveDialog(int[][] schedule, String[] uniqueScoreFields, String[] repeatScoreFields, int[][] scoreVals, int rnd, int match, int oldMatch) {
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
				scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, rnd, match);
			}
		});
		
		Button saveButton = new Button("Save");
		saveButton.setId("popup-button");
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				save(schedule, uniqueScoreFields, repeatScoreFields, rnd, oldMatch);
				popupStage.hide();
				scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, rnd, match);
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
	
	// TODO Show string error
	public void save(int[][] schedule, String[] uniqueScoreFields, String[] repeatScoreFields, int rnd, int match) {
		
		int lineAmt = 3 + 1 + (uniqueScoreFields.length + repeatScoreFields.length)*2 + 1 + teamAmt + 1 + qualsMatchCnt + 4 + match;
		for(int i = 0;i < rnd;i++)
			lineAmt += matchRoundCnt[i];
		
		String lineVal = "";
		for(int i = 0;i < schedule[match].length;i++)
			lineVal += schedule[match][i] + " ";
		
		int[] scoreVals;
		
		for(int i = 0;i < schedule[match].length;i++) {
			scoreVals = getScoreVals(i+1, uniqueScoreFields.length, repeatScoreFields.length);
			for(int j = 0;j < scoreVals.length;j++)
				lineVal += scoreVals[j] + " ";
		}
		
		String msg = Database.replaceFileLine(tournamentFile, lineAmt, lineVal);
		if(msg != "") {
			//TODO Show error message
		}
		
		try {
			Database.updateElimsWorkbook(tournamentFile, rnd);
		} catch (IOException e) {
			//TODO Show error message
			e.printStackTrace();
		}
	}
	
	private int[] getScoreVals(int col, int uniqueCnt, int repeatCnt) {
		
		int[] vals = new int[uniqueCnt + repeatCnt];
		Node n;
		CheckBox c;
		Spinner<Integer> s;
		
		for(int i = 0;i < scorePane.getChildren().size();i++) {
			n = scorePane.getChildren().get(i);
			if(GridPane.getColumnIndex(n) == col && GridPane.getRowIndex(n) != 0) {
				if(GridPane.getRowIndex(n)-1 > uniqueCnt) {
					s = (Spinner<Integer>) n;
					vals[GridPane.getRowIndex(n)-2] = s.getValue();
				}
				else if(GridPane.getRowIndex(n) > 1) {
					c = (CheckBox) n;
					if(c.isSelected())
						vals[GridPane.getRowIndex(n)-2] = 1;
					else vals[GridPane.getRowIndex(n)-2] = 0;
				}
			}
		}
		
		System.out.print("" + col + ": ");
		for(int i = 0;i < uniqueCnt+repeatCnt;i++) {
			System.out.print(vals[i] + " ");
		}
		System.out.println();
		
		return vals;
	}
	
}

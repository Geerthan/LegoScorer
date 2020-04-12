package LegoScorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TournamentView {
	
	File tournamentFile;
	Stage primaryStage, popupStage;
	int[][] schedule, scoreVals;
	String[] uniqueScoreFields, repeatScoreFields;
	int teamAmt;
	GridPane scorePane;
	Button playoffsButton, saveButton;
	ListView<HBox> gameLV;
	
	boolean unsaved;
	
	public TournamentView(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public Scene getScene(File tournamentFile) {
		
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
		
		playoffsButton = new Button("Playoffs Mode");
		playoffsButton.setId("orange-header-button");
		
		saveButton = new Button("Save");
		
		headerButtonBox.getChildren().addAll(playoffsButton, saveButton);
		
		StackPane.setMargin(headerButtonBox, new Insets(0, 20, 0, 0));
		topStack.getChildren().addAll(topRect, logoView, headerButtonBox);
		
		root.getChildren().add(topStack);
		
		HBox viewBox = new HBox();

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
		
		gameLV = new ListView<HBox>();
		gameLV.setPrefHeight(550);
		gameLV.setMaxWidth(230);
		
		loadQualsSchedule();		
		
		scorePane = new GridPane();
		scorePane.setAlignment(Pos.TOP_CENTER);
		scorePane.setHgap(25);
		scorePane.setVgap(10);
		scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, 0);
		
		gameLV.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			if(unsaved)
				showSaveDialog(schedule, uniqueScoreFields, repeatScoreFields, scoreVals, (int) newValue, (int) oldValue);
			else
				scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, (int) newValue);
		});
		
		viewBox.getChildren().addAll(gameLV, scorePane);
		
		root.getChildren().add(viewBox);
		
		// TODO Make resizable
		Scene s = new Scene(root, 1000, 600);
		s.getStylesheets().add("tournament-view.css");
		return s;
		
	}
	
	public void loadQualsSchedule() {
		
		gameLV.getItems().clear();
		
		try {
			schedule = Database.getSchedule(tournamentFile);
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
		
		try {
			scoreVals = Database.getScoreVals(tournamentFile, uniqueScoreFields.length, repeatScoreFields.length, teamAmt, 0);
		} catch (IOException e) {
			e.printStackTrace();
			scoreVals = new int[0][0];
		}
		
		for(int i = 0;i < schedule.length;i++) {
			HBox gameBox = new HBox();
			gameBox.setAlignment(Pos.CENTER_LEFT);
			
			VBox gameData = new VBox();
			gameData.setSpacing(3);
			Label gameNumLabel = new Label("Match " + (i+1));
			gameNumLabel.setId("big-label");
			
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
	}
	
	public void loadElimsSchedule(File tournamentFile) {
		
		schedule = new int[0][0];
		gameLV.getItems().clear();
		
		//int[] ranking = Database.getRankings(tournamentFile);
		int[] ranking;
		
	}
	
	public GridPane updateScorePane(String[] uniqueScoreFields, String[] repeatScoreFields, int match) {
		
		try {
			scoreVals = Database.getScoreVals(tournamentFile, uniqueScoreFields.length, repeatScoreFields.length, teamAmt, match);
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
		
		for(int i = 1;i < schedule[match].length;i++) {
			ColumnConstraints scoreCol = new ColumnConstraints();
			scoreCol.setHalignment(HPos.CENTER);
			scorePane.getColumnConstraints().add(scoreCol);
		}
		
		playoffsButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				//TODO If playoffs have been generated, give a different prompt
				showPlayoffsDialog();				
			}
		});
		
		saveButton.setId("disabled-header-button");
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if(!unsaved) return;
				unsaved = false;
				saveButton.setId("disabled-header-button");
				save(schedule, uniqueScoreFields, repeatScoreFields, match);
			}
		});
		
		Label titleLabel = new Label("Match " + (match+1));
		titleLabel.setId("title-label");
		scorePane.add(titleLabel, 1, 0, schedule[match].length-1, 1);
		
		for(int i = 1;i < schedule[match].length;i++) {
			Label teamLabel = new Label("Team " + schedule[match][i]);
			teamLabel.setId("big-label");
			scorePane.add(teamLabel, i, 1);
		}
		
		for(int i = 0;i < uniqueScoreFields.length;i++) {
			Label scoreLabel = new Label(uniqueScoreFields[i]);
			scoreLabel.setId("big-label");
			scoreLabel.setMaxWidth(120);
			scorePane.add(scoreLabel, 0, i+2);
			for(int j = 1;j < schedule[match].length;j++) {
				CheckBox c = new CheckBox();
				if(scoreVals[j-1][i] == 1) c.setSelected(true);
				c.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						unsaved = true;
						saveButton.setId("header-button");
					}
				});
				scorePane.add(c, j, i+2);
			}
		}
		
		for(int i = 0;i < repeatScoreFields.length;i++) {
			Label scoreLabel = new Label(repeatScoreFields[i]);
			scoreLabel.setId("big-label");
			scoreLabel.setMaxWidth(120);
			scorePane.add(scoreLabel, 0, uniqueScoreFields.length+i+2);
			for(int j = 1;j < schedule[match].length;j++) {
				Spinner<Integer> s = new Spinner<Integer>(0, 999, 0);
				s.getValueFactory().setValue(scoreVals[j-1][uniqueScoreFields.length+i]);
				s.valueProperty().addListener((obs, oldValue, newValue) -> {
					unsaved = true;
					saveButton.setId("header-button");
				});
				scorePane.add(s, j, uniqueScoreFields.length+i+2);
			}
		}
		
		HBox.setHgrow(scorePane, Priority.ALWAYS);
		return scorePane;
	}

	public void showPlayoffsDialog() {

		String[] roundNames = {"Finals", "Semifinals", "Quarterfinals"};
		Spinner<Integer>[] matchSpinners = new Spinner[3];
		Spinner<Integer>[] teamSpinners = new Spinner[3];
		Spinner<Integer>[] tpmSpinners = new Spinner[3];
		
		VBox root = new VBox();
		root.setPadding(new Insets(15));
		root.setAlignment(Pos.CENTER);
		
		GridPane labelledInputs = new GridPane();
		labelledInputs.setAlignment(Pos.CENTER);
		labelledInputs.setHgap(5);
		labelledInputs.setVgap(5);
		
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHalignment(HPos.RIGHT);
		
		ColumnConstraints spinCol = new ColumnConstraints();
		spinCol.setHalignment(HPos.CENTER);
		
		labelledInputs.getColumnConstraints().addAll(labelCol, spinCol, spinCol);
		
		RowConstraints rowConstraint = new RowConstraints();
		rowConstraint.setValignment(VPos.CENTER);
		
		labelledInputs.getRowConstraints().add(rowConstraint);
		
		Label titleLabel = new Label("Playoffs Parameters");
		titleLabel.setId("dialog-title-label");
		root.getChildren().add(titleLabel);
		
		Label matchLabel = new Label("Match Count");
		matchLabel.setId("dialog-subtitle-label");
		labelledInputs.add(matchLabel, 1, 1);
		
		Label teamLabel = new Label("Team Count");
		teamLabel.setId("dialog-subtitle-label");
		labelledInputs.add(teamLabel, 2, 1);
		
		Label tpmLabel = new Label("Teams per Match");
		tpmLabel.setId("dialog-subtitle-label");
		labelledInputs.add(tpmLabel, 3, 1);
		
		for(int i = 0;i < roundNames.length;i++) {
			
			//Round labels (Finals, Semis, Quarters)
			Label roundLabel = new Label(roundNames[i] + ": ");
			labelledInputs.add(roundLabel, 0, i+2);
			
			//Spinner for amount of matches in a round (ex. 4 quarterfinal matches)
			Spinner<Integer> matchSpinner = new Spinner<Integer>(0, 99, (int) Math.pow(2, i));
			matchSpinners[i] = matchSpinner;
			
			labelledInputs.add(matchSpinner, 1, i+2);
			
			//TODO Add default numbers
			//Spinner for amount of teams in the round total (ex. 16 teams)
			Spinner<Integer> teamSpinner = new Spinner<Integer>(0, 99, (int) Math.pow(2, i+1));
			teamSpinners[i] = teamSpinner;
			
			labelledInputs.add(teamSpinner, 2, i+2);
			
			// Spinner for amount of teams in a match
			Spinner<Integer> tpmSpinner = new Spinner<Integer>(0, 8, (int) Math.pow(2, i+1));
			tpmSpinners[i] = tpmSpinner;
			
			labelledInputs.add(tpmSpinner, 3, i+2);
		}
		
		root.getChildren().add(labelledInputs);
		VBox.setMargin(labelledInputs, new Insets(0, 0, 5, 0));
		
		Text descText = new Text("Set team and match count to 0 if you would like to skip the round (ex. play only semifinals and finals).");
		descText.setWrappingWidth(450);
		descText.setTextAlignment(TextAlignment.CENTER);
		VBox.setMargin(descText, new Insets(0, 0, 5, 0));
		root.getChildren().add(descText);
		
		Button genButton = new Button("Generate");
		genButton.setId("popup-button");
		
		genButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				
				boolean hasPlayoffs;
				
				try {
					hasPlayoffs = Database.hasPlayoffs(tournamentFile);
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}
				
				if(!hasPlayoffs) {
					//TODO Error checking of team counts
					int[] matchRoundCnts = {matchSpinners[2].getValue(), matchSpinners[1].getValue(), matchSpinners[0].getValue()};
					int[] teamRoundCnts = {teamSpinners[2].getValue(), teamSpinners[1].getValue(), teamSpinners[0].getValue()};
					int[] tpmCnts = {tpmSpinners[2].getValue(), tpmSpinners[1].getValue(), tpmSpinners[0].getValue()};
					
					//TODO Show error!
					try {
						Database.genElimsSchedule(tournamentFile, matchRoundCnts, teamRoundCnts, tpmCnts);
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
				}
				else System.out.println("Already generated playoffs");
				
				popupStage.hide();
				primaryStage.setScene(Main.playoffsView.getScene(tournamentFile));
			}
		});
		
		root.getChildren().add(genButton);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("tournament-view.css");
		popupStage = new Stage();
		popupStage.setScene(s);
		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.show();
		
	}
	
	public void showSaveDialog(int[][] schedule, String[] uniqueScoreFields, String[] repeatScoreFields, int[][] scoreVals, int match, int oldMatch) {
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
				scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, match);
			}
		});
		
		Button saveButton = new Button("Save");
		saveButton.setId("popup-button");
		
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				save(schedule, uniqueScoreFields, repeatScoreFields, oldMatch);
				popupStage.hide();
				scorePane = updateScorePane(uniqueScoreFields, repeatScoreFields, match);
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
	public void save(int[][] schedule, String[] uniqueScoreFields, String[] repeatScoreFields, int match) {
		
		int lineAmt = 3 + 1 + (uniqueScoreFields.length + repeatScoreFields.length)*2 + 1 + teamAmt + 1 + match;
		
		String lineVal = "";
		for(int i = 0;i < schedule[match].length;i++)
			lineVal += schedule[match][i] + " ";
		
		int[] scoreVals;
		
		for(int i = 1;i < schedule[match].length;i++) {
			scoreVals = getScoreVals(i, uniqueScoreFields.length, repeatScoreFields.length);
			for(int j = 0;j < scoreVals.length;j++)
				lineVal += scoreVals[j] + " ";
		}
		
		String msg = Database.replaceFileLine(tournamentFile, lineAmt, lineVal);
		if(msg != "") {
			//TODO Show error message
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
		
		return vals;
	}
	
}

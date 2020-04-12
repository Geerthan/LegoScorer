package LegoScorer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.util.Duration;

public class CreateGameMenu {
	
	Stage primaryStage, popupStage;
	
	public CreateGameMenu(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public Scene getScene() {
		
		String os = System.getProperty("os.name").substring(0, 7);
		
		VBox root = new VBox();
		root.setMaxWidth(700);
		
		StackPane topStack = new StackPane();
		
		Rectangle topRect = new Rectangle(700, 50, Color.web("#003C71"));
		
		Image logo = new Image(getClass().getResource("/img/otu_dark.png").toExternalForm().toString());
		ImageView logoView = new ImageView(logo);
		logoView.setPreserveRatio(true);
		logoView.setFitHeight(50);
		
		topStack.getChildren().addAll(topRect, logoView);
		
		root.getChildren().add(topStack);
		
		HBox formFields = new HBox();
		formFields.setAlignment(Pos.CENTER); //TODO move to css
		formFields.setSpacing(25);
		formFields.setPadding(new Insets(15, 0, 0, 0));
		
		HBox nameFields = new HBox();
		nameFields.setAlignment(Pos.CENTER);
		
		Label nameLabel = new Label("Game name: ");
		TextField nameTextField = new TextField();
		nameTextField.setMaxWidth(135);
		
		nameFields.getChildren().addAll(nameLabel, nameTextField);
		
		HBox teamAmtFields = new HBox();
		teamAmtFields.setAlignment(Pos.CENTER);
		
		Label teamAmtLabel = new Label("Teams per match: ");
		Spinner<Integer> teamAmtSpinner = new Spinner<Integer>(1, 16, 8);
		
		teamAmtFields.getChildren().addAll(teamAmtLabel, teamAmtSpinner);
		
		HBox matchTimeFields = new HBox();
		matchTimeFields.setAlignment(Pos.CENTER);
		
		Label matchTimeLabel = new Label("Time per match: ");
		TimeField matchTimeField = new TimeField("02:00"); //TODO make into Minutes format instead of hrs
		matchTimeField.setPrefWidth(60); //TODO move to .css
		
		matchTimeFields.getChildren().addAll(matchTimeLabel, matchTimeField);
		
		formFields.getChildren().addAll(nameFields, teamAmtFields, matchTimeFields);
		root.getChildren().add(formFields);
		
		GridPane formPane = new GridPane();
		formPane.setPadding(new Insets(0, 20, 10, 20));
		formPane.setHgap(15);
		
		HBox uniqueScoreTitle = new HBox();
		HBox repeatScoreTitle = new HBox();
		
		uniqueScoreTitle.setAlignment(Pos.CENTER);
		repeatScoreTitle.setAlignment(Pos.CENTER);
		
		Label uniqueScoreLabel = new Label("Unique Scoring Fields");
		Label repeatScoreLabel = new Label("Repeatable Scoring Fields");
		
		uniqueScoreTitle.getChildren().add(uniqueScoreLabel);
		repeatScoreTitle.getChildren().add(repeatScoreLabel);
		
		ImageView uniqueHelpImg = new ImageView();
		uniqueHelpImg.setId("help-img");
		uniqueHelpImg.setPreserveRatio(true);
		uniqueHelpImg.setPickOnBounds(true);
		uniqueHelpImg.setFitHeight(30);
		
		ImageView repeatHelpImg = new ImageView();
		repeatHelpImg.setId("help-img");
		repeatHelpImg.setPreserveRatio(true);
		repeatHelpImg.setPickOnBounds(true);
		repeatHelpImg.setFitHeight(30);
		
		Tooltip uniqueTip = new Tooltip("Represents a scoring method that can only happen once per team (or total). "
				+ "For example, placing 1st or 2nd is unique, and would count as a unique scoring field.");
		uniqueTip.setShowDelay(Duration.ZERO);
		uniqueTip.setHideDelay(Duration.ZERO);
		uniqueTip.setPrefWidth(350);
		uniqueTip.setWrapText(true);
		Tooltip.install(uniqueHelpImg, uniqueTip);
		
		Tooltip repeatTip = new Tooltip("Represents a repeatable scoring method. For example, scoring basketballs "
				+ "into a basket is a repeatable scoring method.");
		repeatTip.setShowDelay(Duration.ZERO);
		repeatTip.setHideDelay(Duration.ZERO);
		repeatTip.setPrefWidth(350);
		repeatTip.setWrapText(true);
		Tooltip.install(repeatHelpImg, repeatTip);
		
		
		HBox.setMargin(uniqueHelpImg, new Insets(0, 0, 0, 10));
		HBox.setMargin(repeatHelpImg, new Insets(0, 0, 0, 10));
		
		uniqueScoreTitle.getChildren().add(uniqueHelpImg);
		repeatScoreTitle.getChildren().add(repeatHelpImg);
		
		GridPane.setMargin(uniqueScoreTitle, new Insets(10, 0, 0, 0));
		GridPane.setMargin(repeatScoreTitle, new Insets(10, 0, 0, 0));
		
		formPane.getChildren().addAll(uniqueScoreTitle, repeatScoreTitle);
		GridPane.setConstraints(uniqueScoreTitle, 0, 0);
		GridPane.setConstraints(repeatScoreTitle, 1, 0);
		
		ListView<HBox> uniqueScoreLV = new ListView<HBox>();
		uniqueScoreLV.setPrefHeight(250);
		uniqueScoreLV.setFocusTraversable(false);
		//TODO find proper selection removal
		uniqueScoreLV.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
			uniqueScoreLV.getSelectionModel().clearSelection();
		});
		
		ListView<HBox> repeatScoreLV = new ListView<HBox>();
		repeatScoreLV.setPrefHeight(250);
		repeatScoreLV.setFocusTraversable(false);
		//TODO find proper selection removal
		repeatScoreLV.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
			repeatScoreLV.getSelectionModel().clearSelection();
		});
		
		formPane.getChildren().addAll(uniqueScoreLV, repeatScoreLV);
		GridPane.setConstraints(uniqueScoreLV, 0, 1);
		GridPane.setConstraints(repeatScoreLV, 1, 1);
		
		Button addUniqueScoreBtn = new Button("Add New");
		Button addRepeatScoreBtn = new Button("Add New");
				
		addUniqueScoreBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				
				HBox uniqueItemBox = new HBox();
				uniqueItemBox.setAlignment(Pos.CENTER_LEFT);
				uniqueItemBox.setPadding(new Insets(5, 0, 5, 0));
				
				VBox uniqueItemFormBox = new VBox();
				uniqueItemFormBox.setSpacing(5);
				
				HBox nameBox = new HBox();
				nameBox.setAlignment(Pos.CENTER_LEFT);
				
				Label nameLabel = new Label("Field name: ");
				TextField nameTextField = new TextField();
				nameTextField.setPrefWidth(150);
				nameBox.getChildren().addAll(nameLabel, nameTextField);
				
				HBox pointsBox = new HBox();
				pointsBox.setAlignment(Pos.CENTER_LEFT);
				
				Label pointsLabel = new Label("Point value: ");
				Spinner<Double> pointsSpinner = new Spinner<Double>(0, 50, 1, 0.5);
				pointsBox.getChildren().addAll(pointsLabel, pointsSpinner);
				
				uniqueItemFormBox.getChildren().addAll(nameBox, pointsBox);
				
				Button deleteBtn = new Button();
				deleteBtn.setId("delete-item-button");
				deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						uniqueScoreLV.getItems().remove(uniqueItemBox);
					}
				});
				
				HBox.setHgrow(uniqueItemFormBox, Priority.ALWAYS);
				
				uniqueItemBox.getChildren().addAll(uniqueItemFormBox, deleteBtn);
				
				uniqueScoreLV.getItems().add(uniqueItemBox);
				
				primaryStage.sizeToScene();
				
			}
		});
		
		addRepeatScoreBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				
				HBox repeatItemBox = new HBox();
				repeatItemBox.setAlignment(Pos.CENTER_LEFT);
				repeatItemBox.setPadding(new Insets(5, 0, 5, 0));
				
				VBox repeatItemFormBox = new VBox();
				repeatItemFormBox.setSpacing(5);
				
				HBox nameBox = new HBox();
				nameBox.setAlignment(Pos.CENTER_LEFT);
				
				Label nameLabel = new Label("Field name: ");
				TextField nameTextField = new TextField();
				nameTextField.setPrefWidth(150);
				nameBox.getChildren().addAll(nameLabel, nameTextField);
				
				HBox pointsBox = new HBox();
				pointsBox.setAlignment(Pos.CENTER_LEFT);
				
				Label pointsLabel = new Label("Point value: ");
				Spinner<Double> pointsSpinner = new Spinner<Double>(0, 50, 1, 0.5);
				pointsBox.getChildren().addAll(pointsLabel, pointsSpinner);
				
				repeatItemFormBox.getChildren().addAll(nameBox, pointsBox);
				
				Button deleteBtn = new Button();
				deleteBtn.setId("delete-item-button");
				deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						repeatScoreLV.getItems().remove(repeatItemBox);
					}
				});
				
				HBox.setHgrow(repeatItemFormBox, Priority.ALWAYS);
				
				repeatItemBox.getChildren().addAll(repeatItemFormBox, deleteBtn);
				
				repeatScoreLV.getItems().add(repeatItemBox);
				
				primaryStage.sizeToScene(); //TODO Check if required
				
			}
		});

		formPane.getChildren().addAll(addUniqueScoreBtn, addRepeatScoreBtn);
		
		GridPane.setConstraints(addUniqueScoreBtn, 0, 2);
		GridPane.setConstraints(addRepeatScoreBtn, 1, 2);
		
		Button exitBtn = new Button("Back");
		exitBtn.setId("exit-button");
		
		Button saveBtn = new Button("Save");
		
		exitBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				primaryStage.setScene(Main.mainMenu.getScene());
			}
		});
		
		saveBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				
				String errorText = checkForInputErrors(nameTextField, uniqueScoreLV, repeatScoreLV);
				if(errorText != "") {
					showErrorDialog(errorText);
					return;
				}
				
				String gameName = nameTextField.getText();
				int teamAmt = teamAmtSpinner.getValue();
				int timeAmt = matchTimeField.getValue();
				
				String[] uniqueScoreStrs = new String[uniqueScoreLV.getItems().size()];
				double[] uniqueScorePtVals = new double[uniqueScoreLV.getItems().size()];
				
				for(int i = 0;i < uniqueScoreLV.getItems().size();i++) {
					VBox scoreBox = (VBox) ((HBox) uniqueScoreLV.getItems().get(i)).getChildren().get(0);
					uniqueScoreStrs[i] = getFieldName(scoreBox);
					uniqueScorePtVals[i] = getFieldPtVal(scoreBox);
				}
				
				String[] repeatScoreStrs = new String[repeatScoreLV.getItems().size()];
				double[] repeatScorePtVals = new double[repeatScoreLV.getItems().size()];
				
				for(int i = 0;i < repeatScoreLV.getItems().size();i++) {
					VBox scoreBox = (VBox) ((HBox) repeatScoreLV.getItems().get(i)).getChildren().get(0);
					repeatScoreStrs[i] = getFieldName(scoreBox);
					repeatScorePtVals[i] = getFieldPtVal(scoreBox);
				}
				
				Database.createGameFolder(os);
				
				String errorStr = Database.createGameType(gameName, teamAmt, timeAmt, 
						uniqueScoreStrs, uniqueScorePtVals, repeatScoreStrs, repeatScorePtVals, os);
				
				if(errorStr != "") {
					showErrorDialog(errorStr);
					return;
				}
				
				showSaveDialog(gameName);
				
			}
		});
		
		formPane.getChildren().addAll(exitBtn, saveBtn);
		
		GridPane.setConstraints(exitBtn, 0, 4);
		GridPane.setConstraints(saveBtn, 1, 4);
		
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(50);
		col1.setHalignment(HPos.CENTER);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(50);
		col2.setHalignment(HPos.CENTER);
		formPane.getColumnConstraints().addAll(col1, col2);
		
		root.getChildren().addAll(formPane);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("create-game-menu.css");
		return s;
		
	}
	
	public String checkForInputErrors(TextField nameTextField, ListView<HBox> uniqueScoreLV, ListView<HBox> repeatScoreLV) {
		if(nameTextField.getText().isEmpty())
			return "The game requires a name (ex. Battle Royale).";
		
		else if(uniqueScoreLV.getItems().size() == 0 && repeatScoreLV.getItems().size() == 0)
			return "The game requires at least one scoring field.";
						
		for(int i = 0;i < uniqueScoreLV.getItems().size();i++) {
			VBox scoreBox = (VBox) ((HBox) uniqueScoreLV.getItems().get(i)).getChildren().get(0);
			if(getFieldName(scoreBox).isEmpty())
				return "All scoring fields require a valid name.";
		}
			
		for(int i = 0;i < repeatScoreLV.getItems().size();i++) {
			VBox scoreBox = (VBox) ((HBox) repeatScoreLV.getItems().get(i)).getChildren().get(0);
			if(getFieldName(scoreBox).isEmpty())
				return "All scoring fields require a valid name.";
		}
		
		return "";
	}
	
	public String getFieldName(VBox scoreBox) {
		HBox nameBox = (HBox) scoreBox.getChildren().get(0);
		TextField txtfd = (TextField) nameBox.getChildren().get(1);
		return txtfd.getText();
	}
	
	public double getFieldPtVal(VBox scoreBox) {
		HBox ptBox = (HBox) scoreBox.getChildren().get(1);
		Spinner<Double> ptSpinner = (Spinner<Double>) ptBox.getChildren().get(1);
		return ptSpinner.getValue();
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
		s.getStylesheets().add("create-game-menu.css");
		popupStage = new Stage();
		popupStage.setScene(s);
		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.show();
	}
	
	public void showSaveDialog(String gameName) {
		VBox root = new VBox();
		root.setPadding(new Insets(15));
		root.setAlignment(Pos.CENTER);
		
		Text exitText = new Text("Game \"" + gameName + "\" successfully saved as file \"" + gameName + ".gdat\".");
		VBox.setMargin(exitText, new Insets(0, 0, 5, 0));
		root.getChildren().add(exitText);
		
		Button exitButton = new Button("Return to Menu");
		exitButton.setId("exit-popup-button");
		
		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				popupStage.hide();
				primaryStage.setScene(Main.mainMenu.getScene());
			}
		});
		
		root.getChildren().add(exitButton);
		
		Scene s = new Scene(root);
		s.getStylesheets().add("create-game-menu.css");
		popupStage = new Stage();
		popupStage.setScene(s);
		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.show();
	}
	
}

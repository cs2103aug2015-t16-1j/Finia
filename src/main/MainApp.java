package main;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import main.view.RootLayoutController;

public class MainApp extends Application {
	private Stage primaryStage;
	private RootLayoutController rootLayoutController;
	private Controller controller;

	@Override
	public void start(Stage primaryStage) throws Exception{
		controller = Controller.getInstance();
		rootLayoutController = new RootLayoutController(controller);
		
		initPrimaryStage(primaryStage);

		// Provide a stage handle in controller so that controller can close it when exiting
		assert primaryStage != null;
		controller.setStage(this.primaryStage);
	}

    private void initPrimaryStage(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
		this.primaryStage.getIcons().add(new Image("/main/resource/image/icon.png")); 
		this.primaryStage.setTitle("Fini");
		this.primaryStage.setMinWidth(600);
		this.primaryStage.setMinHeight(600);
		assert rootLayoutController != null;
		
		AnchorPane welcome = FXMLLoader.load(MainApp.class.getResource("/main/view/WelcomeScene.fxml"));		
		Button buttonEnter = new Button("ENTER");
		buttonEnter.setLayoutX(250);
		buttonEnter.setLayoutY(360);
		buttonEnter.setOnAction(e -> primaryStage.setScene(new Scene(rootLayoutController)));
		welcome.getChildren().add(buttonEnter);
		Scene welcomeScene = new Scene(welcome);
		
		this.primaryStage.setScene(welcomeScene);
        this.primaryStage.show();
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}

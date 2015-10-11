package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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

    private void initPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
//		this.primaryStage.getIcons().add(new Image("/images/icon.png")); 
		this.primaryStage.setTitle("Fini");
		this.primaryStage.setMinWidth(600);
		this.primaryStage.setMinHeight(600);
		assert rootLayoutController != null;
		this.primaryStage.setScene(new Scene(rootLayoutController));
        this.primaryStage.show();
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}

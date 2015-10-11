package main;

import java.io.IOException;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    Scene welcomeScene = new Scene(welcome);
    
    
    // dummy textfield to recognise when user presses ENTER
    final TextField instructions = new TextField();
    instructions.setLayoutX(-100);
    instructions.setLayoutY(-100);
    instructions.setOnKeyPressed(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent userPressesEnter) {
        if(userPressesEnter.getCode().equals(KeyCode.ENTER)) {
          primaryStage.setScene(new Scene(rootLayoutController));
        }
      }
    });
    
    welcome.getChildren().add(instructions);
    
    this.primaryStage.setScene(welcomeScene);
    this.primaryStage.show();


    //    Button buttonEnter = new Button("ENTER");
    //    buttonEnter.setLayoutX(250);
    //    buttonEnter.setLayoutY(360);
    //    buttonEnter.setOnAction(e -> primaryStage.setScene(new Scene(rootLayoutController)));
    //    welcome.getChildren().add(buttonEnter);
  }

  public static void main(String[] args) {
    launch(args);
  }
}

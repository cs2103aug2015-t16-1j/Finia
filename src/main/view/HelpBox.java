package main.view;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class HelpBox extends HBox {
    @FXML
    private Label description;
    @FXML
    private Label command;

    public HelpBox(String description, String command) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
            		"/view/HelpBox.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.description.setText(description);
        this.command.setText(command);
    }
}
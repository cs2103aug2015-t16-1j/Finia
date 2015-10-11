package main.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class Folder extends HBox {
	@FXML
	private Label folderIcon;
	@FXML
	private Label folderName;
	@FXML
	private Label taskNumber;
	
	public Folder(String folderName, int taskNumber) {
        loadFxml();
        this.folderIcon.setText("ICON");
        this.folderName.setText(folderName);
        this.taskNumber.setText(taskNumber + "");
    }
	
	private void loadFxml() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("view/Folder.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package main.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import main.MainApp;

public class TaskBox extends HBox {

    // ================================================================
    // FXML Fields
    // ================================================================
    @FXML
    private Label index;

    @FXML
    private Label recurringIcon;

    @FXML
    private Label description;
    
    @FXML
    private Label timeAndDate;

    @FXML
    private Label countdown;
    
    @FXML
    private Label priority;
    
    @FXML
    private Label group;

    
    // ================================================================
    // Constants
    // ================================================================
    private static final String STRING_EMPTY = "";
    private static final String STRING_ONE_SPACING = " ";
    private static final String LOCATION_TASK_BOX_FXML = "/view/TaskBox.fxml";
//    private static final String KEYWORD_COMPLETE = Command.Type.COMPLETE.toString();
//    private static final String KEYWORD_INCOMPLETE = Command.Type.INCOMPLETE.toString();
//    private static final String KEYWORD_DELETE = Command.Type.DELETE.toString();
    private static final String ICON_RECURRING = STRING_ONE_SPACING + "\uf01e" +
                                                 STRING_ONE_SPACING;
    private static final String FORMAT_DATE = "%s";

    private static final int TIMELINE_FRAME_DELAY_MILLISECONDS = 10;
    private static final int HIGHLIGHT_DISPLAY_SECONDS = 7;
    private static final String STYLE_HIGHLIGHT_TAG_FORMAT = "-fx-background-color: %s;";
    private static final String STYLE_HIGHLIGHT_COLOR_FORMAT = "rgb(255, 187, 141, %.2f)";


    // ================================================================
    // Constructors
    // ================================================================
    /**
     * Creates a TaskBox
     * 
     * @param idx           Index to be shown next to the task.
     * @param desc          Description of the task.
     * @param timeAndDate   Formatted time and Date to be shown next to the description.
     * @param isRecurring   To indicate if a TaskBox shows a recurring icon.
     */
    public TaskBox(int idx, String desc, String timeAndDate, boolean isRecurring) {
        loadFxml();
        initListenerAndFields(idx, desc, timeAndDate, isRecurring);
    }

    /**
     * Creates a TaskBox
     * 
     * @param idx           Index to be shown next to the task.
     * @param desc          Description of the task.
     * @param timeAndDate   Formatted time and Date to be shown next to the description.
     * @param isRecurring   To indicate if a TaskBox shows a recurring icon.
     * @param isCompleted   To indicate if a TaskBox shows its checkbox as being ticked.
     */
    public TaskBox(int idx, String desc, String timeAndDate,
                   boolean isRecurring, boolean isCompleted) {
        loadFxml();
        initListenerAndFields(idx, desc, timeAndDate, isRecurring);
    }

    
    // ================================================================
    // Public methods
    // ================================================================
    public String getDescription() {
        return description.getText();
    }
    
    public String getTimeAndDate() {
        return timeAndDate.getText();
    }

    public void highlight() {
        float opacity = 1;
        highlight(String.format(STYLE_HIGHLIGHT_COLOR_FORMAT, opacity));
        generateHighlightTimeline().play();
    }
    

    // ================================================================
    // Initialisation methods
    // ================================================================
    private void loadFxml() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/TaskBox.fxml"));
            loader.setRoot(null);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initListenerAndFields(int idx, String desc, String timeAndDate, boolean isRecurring) {
        initFxmlFields(idx, desc, timeAndDate, isRecurring);
    }

    private void initFxmlFields(int idx,
                                String desc,
                                String timeAndDate,
                                boolean isRecurring
                                ) {
        if (isRecurring) {
            this.recurringIcon.setText(ICON_RECURRING);
        } else {
            this.recurringIcon.setText(STRING_EMPTY);
        }
        this.index.setText(idx + STRING_EMPTY);
        this.description.setText(desc);
        this.timeAndDate.setText(String.format(FORMAT_DATE, timeAndDate));
    }
    
    
    // ================================================================
    // Methods for handling highlighting of task boxes
    // ================================================================
    private void highlight(String color) {
        this.setStyle(String.format(STYLE_HIGHLIGHT_TAG_FORMAT, color));
    }

    private Timeline generateHighlightTimeline() {
        Timeline timeline = new Timeline();
        // Add an intial key frame to delay the fading out of the highlight.
        timeline.getKeyFrames()
                .add(new KeyFrame(Duration.seconds(HIGHLIGHT_DISPLAY_SECONDS)));

        // Generates the keyframes for the fade out effect.
        for (int i = 100; i >= 0; i--) {
            // Calculates the opacity to be used in the css of TaskBox.
            float opacity = (float) i / 100;
            String color = String.format(STYLE_HIGHLIGHT_COLOR_FORMAT, opacity);
            timeline.getKeyFrames()
                    .add(new KeyFrame(timeline.getTotalDuration() // Add to the end of the timeline.
                                              .add(Duration.millis(TIMELINE_FRAME_DELAY_MILLISECONDS)),
                                      new EventHandler<ActionEvent>() {
                                          @Override
                                          public void handle(ActionEvent event) {
                                              highlight(color);
                                          }
                                      }));
        }
        return timeline;
    }
}

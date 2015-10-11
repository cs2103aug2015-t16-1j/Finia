package main.view;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import main.Task;

public class DisplayController extends VBox {
    // ================================================================
    // FXML Fields
    // ================================================================
    @FXML
    private ListView<HBox> taskView;
    @FXML
    private Label feedbackLabel;
    @FXML
    private VBox noTaskOverlay;
    @FXML
    private Label noTaskOverlayIcon;
    @FXML
    private Label noTaskOverlayGreeting;
    @FXML
    private Label noTaskOverlayMessage;
    @FXML
    private VBox helpOverlay;
    @FXML
    private Label helpOverlayIcon;
    @FXML
    private Label helpOverlayTitle;
    @FXML
    private ListView<Folder> folderView;
    @FXML
    private ListView<Group> groupView; 
    @FXML
    private ListView<HelpBox> helpOverlayContents;

    private static DisplayController displayController;

    private Timeline feedbackTimeline;
    private Timeline overlayTimeline;
    private ArrayList<String> allExampleCommands;
    private ObservableList<HelpBox> helpList;

    private int currentScrollIndex;
    private int numExcessTasks;
    private boolean isCurrentDisplayOverview;

    private static final String HELP_OVERLAY_ICON = "\uf05a";
    private static final String NO_TASK_OVERLAY_ICON = "\uf14a";

    // ================================================================
    // Constructor
    // ================================================================
    private DisplayController() {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Display.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initTimelines();
        initExampleCommands();
        initfolderView();
        initHelpList();
    }

    
    public static DisplayController getInstance() {
        if (displayController == null) {
            displayController = new DisplayController();
        }
        return displayController;
    }


    // ================================================================
    // Public methods
    // ================================================================
    public void hideOverlays() {
        noTaskOverlay.toBack();
        helpOverlay.toBack();
        noTaskOverlay.setOpacity(0);
        helpOverlay.setOpacity(0);
    }

    /**
     * Update display of tasks.
     * The input tasks follows a predefined order of overdue tasks, floating
     * tasks, this week's tasks then all other tasks.
     * This ensures that the index shown in the display corresponds with the
     * actual index in the input ObservableList.
     * 
     * @param tasks Must be sorted in the above order.
     */
    public void updateOverviewDisplay(ObservableList<Task> tasks) {
        handleOverlays(tasks);

        ArrayList<Task> listOfTasks = trimListOfTasks(tasks);

        ObservableList<HBox> displayBoxes = FXCollections.observableArrayList();

        addTasksToOverviewDisplay(listOfTasks, displayBoxes);
        addNumExcessTasksLabel(displayBoxes);

        handleChangeHighlights(displayBoxes);
        
        taskView.setItems(displayBoxes);
        isCurrentDisplayOverview = true;
    }

    /**
     * Update display to show search results.
     * The input tasks follows a predefined order of incomplete tasks then
     * completed tasks.
     * This ensures that the index shown in the display corresponds with the
     * actual index in the input ObservableList.
     * 
     * @param searchResults Must be sorted in the above order.
     * @param searchQuery   User's input query
     */
    public void updateSearchDisplay(ObservableList<Task> searchResults,
                                    String searchQuery) {
        hideOverlays();

        ArrayList<Task> listOfResults = trimListOfTasks(searchResults);

        ObservableList<HBox> displayBoxes = FXCollections.observableArrayList();

        addSearchLabel(displayBoxes, searchResults, searchQuery);
        addTasksToSearchDisplay(listOfResults, displayBoxes);
        addNumExcessTasksLabel(displayBoxes);
        taskView.setItems(displayBoxes);
        isCurrentDisplayOverview = false;
    }

    public void setFeedback(String feedback) {
        FadeTransition fadeIn = initFadeIn(feedbackLabel,
                                           500);
        FadeTransition fadeOut = initFadeOut(feedbackLabel,
                                             1000);

        feedbackTimeline.stop();
        feedbackTimeline = generateFeedbackTimeline(feedback, fadeIn, fadeOut);
        feedbackTimeline.play();
    }

    // Will result in the help menu to appear
    public void showHelpDisplay() {
        hideOverlays();
        FadeTransition fadeIn = initFadeIn(helpOverlay,
                                           200);

        overlayTimeline = generateHelpOverlayTimeline(fadeIn);
        overlayTimeline.play();
    }

    public void resetScrollIndex() {
        currentScrollIndex = 0;
    }

    public void scrollDown() {
        if (currentScrollIndex == 0 &&
            taskView.getItems().size() < 14) {
            currentScrollIndex = 0;
        } else if (currentScrollIndex < taskView.getItems().size() -
                                        14) {
            currentScrollIndex += 5;
            taskView.scrollTo(currentScrollIndex);
        }
    }

    public void scrollUp() {
        if (currentScrollIndex > 0) {
            currentScrollIndex -= 5;
            taskView.scrollTo(currentScrollIndex);
        } else if (currentScrollIndex < 0) {
            currentScrollIndex = 0;
        }
    }

    // ================================================================
    // Private overlay method
    // ================================================================
    /**
     * Shows an overlay message when there are no tasks to display.
     */
    private void showNoTaskOverlay() {
        setFeedback("");
        String exampleCommands = generateExampleCommands();

        FadeTransition fadeIn = initFadeIn(noTaskOverlay,
                                           200);

        overlayTimeline = generateNoTaskOverlayTimeline(exampleCommands, fadeIn);
        overlayTimeline.play();
    }

    private String generateExampleCommands() {
        Collections.shuffle(allExampleCommands);
        String exampleCommands = generateParagraph(allExampleCommands,
                                                   3);
        return exampleCommands;
    }

    // ================================================================
    // Initialization methods
    // ================================================================
    private void initTimelines() {
        feedbackTimeline = new Timeline();
        overlayTimeline = new Timeline();
    }

    // Append example commands for users to see when their Veto is empty
    private void initExampleCommands() {
        allExampleCommands = new ArrayList<String>();
        allExampleCommands.add("add meet Isabel from 5pm to 6pm today");
        allExampleCommands.add("add do tutorial 10 tomorrow");
        allExampleCommands.add("add finish assignment by 2359 tomorrow");
        allExampleCommands.add("add find easter eggs by 10 apr");
        allExampleCommands.add("add complete proposal by friday");
        allExampleCommands.add("add exercise every tuesday");
        allExampleCommands.add("add lunch with boss tomorrow");
        allExampleCommands.add("add remember to buy milk");
        allExampleCommands.add("add watch movie with friends today");
        allExampleCommands.add("add remember wedding anniversary on 12 October");
        allExampleCommands.add("add buy the latest Harry Potter book");
        allExampleCommands.add("add sneak into Apple WWDC");
        allExampleCommands.add("add remember to complete SOC project");
        allExampleCommands.add("add find partner for Orbital");
        allExampleCommands.add("add make funny YouTube video next week");
        allExampleCommands.add("add study for final exams");
        allExampleCommands.add("add plan for overseas trip next week");
        allExampleCommands.add("add meeting with boss 23 July");
        allExampleCommands.add("add return money owed to John");
        allExampleCommands.add("add run for presidential campaign");
        allExampleCommands.add("add do some community work next week");
    }

    // Initialize the help display
    private void initHelpList() {
        helpList = FXCollections.observableArrayList();
        helpList.add(new HelpBox("Add a task", "add <description> <time> <day>"));
        helpList.add(new HelpBox("Edit a task", "edit <index> <description> <time> <day>"));
        helpList.add(new HelpBox("Delete a task", "delete <index>"));
        helpList.add(new HelpBox("Mark a task as completed", "complete <index>"));
        helpList.add(new HelpBox("Mark a task as incomplete", "incomplete <index>"));
        helpList.add(new HelpBox("Undo previous action", "undo"));
        helpList.add(new HelpBox("Set a file as save file",
                                 "set <directory>"));
        helpList.add(new HelpBox("Change save directory",
                                 "move <directory>"));
        helpList.add(new HelpBox("Search for a task", "search <keyword/day>"));
        helpList.add(new HelpBox("Display overview",
                                 "display"));
        helpList.add(new HelpBox("Display completed tasks",
                                 "display completed"));
        helpList.add(new HelpBox("Exit Veto", "exit"));
    }
    
    private void initfolderView() {
    	folderView = FXCollections.observableArrayList();
    	folderView.add(new Folder("All", 0));
    	folderView.add(new Folder("Today", 1));
    	folderView.add(new Folder("Next seven days", 2));
    }

    private void initNoTaskOverlay(String exampleCommands) {
        noTaskOverlay.setOpacity(0);
        noTaskOverlay.toFront();
        noTaskOverlayIcon.setText(NO_TASK_OVERLAY_ICON);
        noTaskOverlayGreeting.setText("Hello!");
        noTaskOverlayMessage.setText("Looks like you've got no pending tasks. "
                                                          + "Type \"help\" for a list of commands or "
                                                          + "try entering the following:\n\n" + exampleCommands);
    }

    private void initFeedbackLabel(String feedback) {
        feedbackLabel.setOpacity(0);
        feedbackLabel.setText(feedback);
    }

    private void initHelpOverlay() {
        helpOverlay.toFront();
        helpOverlayIcon.setText(HELP_OVERLAY_ICON);
        helpOverlayTitle.setText("Need help?");
        helpOverlayContents.setItems(helpList);
    }

    private FadeTransition initFadeIn(Node node, int duration) {
        FadeTransition fadeIn = new FadeTransition(new Duration(duration));
        fadeIn.setNode(node);
        fadeIn.setToValue(1);
        return fadeIn;
    }

    private FadeTransition initFadeOut(Node node, int duration) {
        FadeTransition fadeOut = new FadeTransition(new Duration(duration));
        fadeOut.setNode(node);
        fadeOut.setToValue(0);
        return fadeOut;
    }

    // ================================================================
    // Timeline generators
    // ================================================================
    private Timeline generateFeedbackTimeline(String feedback,
                                              FadeTransition fadeIn,
                                              FadeTransition fadeOut) {
        // First KeyFrame is to fade in the feedback message and the second
        // KeyFrame is to fade it out after several seconds.
        return new Timeline(new KeyFrame(new Duration(1), // JavaFx does not
                                                          // work properly when
                                                          // a duration of zero
                                                          // is given.
                                         new EventHandler<ActionEvent>() {
                                             @Override
                                             public void handle(ActionEvent event) {
                                                 initFeedbackLabel(feedback);
                                                 fadeIn.play();
                                             }
                                         }),
                            new KeyFrame(Duration.seconds(8),
                                         new EventHandler<ActionEvent>() {
                                             @Override
                                             public void handle(ActionEvent event) {
                                                 fadeOut.play();
                                             }
                                         }));
    }

    private Timeline generateNoTaskOverlayTimeline(String exampleCommands,
                                                   FadeTransition fadeIn) {
        return new Timeline(new KeyFrame(new Duration(1),
                                         new EventHandler<ActionEvent>() {
                                             @Override
                                             public void handle(ActionEvent event) {
                                                 initNoTaskOverlay(exampleCommands);
                                                 fadeIn.play();
                                             }
                                         }));
    }

    private Timeline generateHelpOverlayTimeline(FadeTransition fadeIn) {
        return new Timeline(new KeyFrame(new Duration(1),
                                         new EventHandler<ActionEvent>() {
                                             @Override
                                             public void handle(ActionEvent event) {
                                                 initHelpOverlay();
                                                 fadeIn.play();
                                             }
                                         }));
    }


    // ================================================================
    // Logic methods for updateOverviewDisplay
    // ================================================================
    private void handleOverlays(ObservableList<Task> tasks) {
        hideOverlays();
        if (tasks.isEmpty()) {
            showNoTaskOverlay();
        }
    }

    private void addTasksToOverviewDisplay(ArrayList<Task> listOfTasks,
                                           ObservableList<HBox> displayBoxes) {
        LocalDate now = LocalDate.now();
        int index = 1;
        index = addOverdueTasks(displayBoxes, listOfTasks, index);
        index = addFloatingTasks(displayBoxes, listOfTasks, index);
        index = addThisWeeksTasks(displayBoxes, listOfTasks, now, index);
        index = addAllOtherTasks(displayBoxes, listOfTasks, now, index);
    }

    private void handleChangeHighlights(ObservableList<HBox> displayBoxes) {
        if (isCurrentDisplayOverview) {
            highlightChanges(displayBoxes);
        }
    }

    private int addOverdueTasks(ObservableList<HBox> displayBoxes,
                                ArrayList<Task> listOfTasks,
                                int index) {
        // Add category label to be displayed above that category.
        CategoryBox overdue = new CategoryBox("Overdue");
        displayBoxes.add(overdue);

        boolean hasOverdue = false;

        // Tasks before index-1 are not applicable (not overdue).
        List<Task> applicableTasks = listOfTasks.subList(index - 1,
                                                         listOfTasks.size());
        for (Task task : applicableTasks) {
            if (task.isOverdue()) {
                hasOverdue = true;
                addTask(displayBoxes, index, task, true);
                index++;
            } else {
                break;
            }
        }

        // Changes the color of the category label to indicate the lack of tasks
        // that fall under this category.
        if (!hasOverdue) {
            overdue.dim();
        }

        return index;
    }

    private int addFloatingTasks(ObservableList<HBox> displayBoxes,
                                 ArrayList<Task> listOfTasks,
                                 int index) {
        CategoryBox floating = new CategoryBox("Floating");
        displayBoxes.add(floating);

        boolean hasFloating = false;

        // Tasks before index-1 are not applicable (not floating).
        List<Task> applicableTasks = listOfTasks.subList(index - 1,
                                                         listOfTasks.size());
        for (Task task : applicableTasks) {
            if (task.getType() == Task.Type.FLOATING) {
                hasFloating = true;
                addTask(displayBoxes, index, task, true);
                index++;
            } else {
                break;
            }
        }

        if (!hasFloating) {
            floating.dim();
        }

        return index;
    }

    private int addThisWeeksTasks(ObservableList<HBox> displayBoxes,
                                  ArrayList<Task> listOfTasks,
                                  LocalDate now,
                                  int index) {
        // generate the dates of the 7 days from today
        ArrayList<LocalDate> days = generateDaysOfWeek(now);
        assert days.size() == 7;

        for (LocalDate day : days) {
            CategoryBox label = generateDayLabel(now, day);
            displayBoxes.add(label);

            boolean hasTaskOnThisDay = false;

            // Tasks before index-1 are not applicable (not equal to day).
            List<Task> applicableTasks = listOfTasks.subList(index - 1,
                                                             listOfTasks.size());
            for (Task task : applicableTasks) {
                if (day.equals(task.getDate())) {
                    hasTaskOnThisDay = true;
                    addTask(displayBoxes, index, task, false);
                    index++;
                } else {
                    break;
                }
            }

            if (!hasTaskOnThisDay) {
                label.dim();
            }
        }
        return index;
    }

    private int addAllOtherTasks(ObservableList<HBox> displayBoxes,
                                 ArrayList<Task> listOfTasks,
                                 LocalDate now,
                                 int index) {
        CategoryBox otherTasks = new CategoryBox("Everything else");
        displayBoxes.add(otherTasks);

        boolean hasOtherTasks = false;
        LocalDate dayOneWeekFromNow = now.plusWeeks(1);

        // Tasks before index-1 are not applicable as they fall under previous
        // categories.
        List<Task> applicableTasks = listOfTasks.subList(index - 1,
                                                         listOfTasks.size());
        for (Task task : applicableTasks) {
            if (task.getDate() != null &&
                (dayOneWeekFromNow.equals(task.getDate()) || dayOneWeekFromNow.isBefore(task.getDate()))) {
                hasOtherTasks = true;
                addTask(displayBoxes, index, task, true);
                index++;
            }
        }

        if (!hasOtherTasks) {
            otherTasks.dim();
        }

        return index;
    }

    private CategoryBox generateDayLabel(LocalDate now, LocalDate day) {

        // formats the date for the day label, eg. Monday, Tuesday, etc
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");

        // formats the date for the date label, eg. 1 April
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM");

        // formats the date for the date label of special cases, eg. Wednesday,
        // 1 April
        DateTimeFormatter dateFormatterForSpecialCase = DateTimeFormatter.ofPattern("EEEE, d MMMM");

        CategoryBox label;

        // special cases to show "Today" and "Tomorrow" instead of day
        if (day.equals(now)) {
            label = new CategoryBox("Today",
                                    day.format(dateFormatterForSpecialCase));
        } else if (day.equals(now.plusDays(1))) {
            label = new CategoryBox("Tomorrow",
                                    day.format(dateFormatterForSpecialCase));
        } else {
            label = new CategoryBox(day.format(dayFormatter),
                                    day.format(dateFormatter));
        }
        return label;
    }

    private ArrayList<LocalDate> generateDaysOfWeek(LocalDate now) {
        ArrayList<LocalDate> days = new ArrayList<LocalDate>();
        for (int i = 0; i < 7; i++) {
            days.add(now.plusDays(i));
        }
        return days;
    }

    private void highlightChanges(ObservableList<HBox> displayBoxes) {
        ObservableList<HBox> oldDisplayBoxes = taskView.getItems();

        // Tasks previously added will not be highlighed when Veto is loaded.
        if (oldDisplayBoxes.isEmpty()) {
            return;
        }
        for (HBox newBox : displayBoxes) {
            if (newBox instanceof TaskBox) {
                TaskBox newTaskBox = (TaskBox) newBox;
                String query = newTaskBox.getDescription() +
                               newTaskBox.getTimeAndDate();
                if (!hasMatchingBox(oldDisplayBoxes, query)) {
                    newTaskBox.highlight();
                }
            }
        }
    }

    private boolean hasMatchingBox(ObservableList<HBox> oldDisplayBoxes,
                                   String query) {
        for (HBox oldBox : oldDisplayBoxes) {
            if (oldBox instanceof TaskBox) {
                TaskBox tBox = (TaskBox) oldBox;
                String descAndDate = tBox.getDescription() +
                                     tBox.getTimeAndDate();
                if (query.equals(descAndDate)) {
                    oldDisplayBoxes.remove(oldBox);
                    return true;
                }
            }
        }
        return false;
    }


    // ================================================================
    // Logic methods for updateSearchDisplay
    // ================================================================
    private void addTasksToSearchDisplay(ArrayList<Task> listOfResults,
                                         ObservableList<HBox> displayBoxes) {
        int index = 1;
        index = addIncompleteTasks(displayBoxes, listOfResults, index);
        index = addCompletedTasks(displayBoxes, listOfResults, index);
    }

    private int addIncompleteTasks(ObservableList<HBox> displayBoxes,
                                   ArrayList<Task> listOfResults,
                                   int index) {
        CategoryBox incompleteLabel = new CategoryBox("Incomplete");
        displayBoxes.add(incompleteLabel);

        boolean hasIncompleteTask = false;

        List<Task> applicableTasks = listOfResults.subList(index - 1,
                                                           listOfResults.size());
        for (Task task : applicableTasks) {
            if (!task.isCompleted()) {
                hasIncompleteTask = true;
                addTask(displayBoxes, index, task, true);
                index++;
            }
        }

        if (!hasIncompleteTask) {
            incompleteLabel.dim();
        }

        return index;
    }

    private int addCompletedTasks(ObservableList<HBox> displayBoxes,
                                  ArrayList<Task> listOfResults,
                                  int index) {
        CategoryBox completedLabel = new CategoryBox("Completed");
        displayBoxes.add(completedLabel);

        boolean hasCompletedTask = false;

        List<Task> applicableTasks = listOfResults.subList(index - 1,
                                                           listOfResults.size());
        for (Task task : applicableTasks) {
            if (task.isCompleted()) {
                hasCompletedTask = true;
                addTask(displayBoxes, index, task, true);
                index++;
            }
        }

        if (!hasCompletedTask) {
            completedLabel.dim();
        }

        return index;
    }

    private void addSearchLabel(ObservableList<HBox> displayBoxes,
                                ObservableList<Task> searchResults,
                                String searchQuery) {
        CategoryBox searchLabel = generateSearchLabel(searchResults,
                                                      searchQuery);
        displayBoxes.add(searchLabel);
    }

    private CategoryBox generateSearchLabel(ObservableList<Task> searchResults,
                                            String searchQuery) {
        CategoryBox searchLabel;
        if (searchQuery.isEmpty()) {
            // No search query will cause all tasks to be shown.
            searchQuery = "all tasks";
        }

        if (searchResults.isEmpty()) {
            searchLabel = new CategoryBox(String.format("No results for \"%s\"",
                                                        searchQuery));
        } else {
            searchLabel = new CategoryBox(String.format("Results for \"%s\"",
                                                        searchQuery));
        }

        return searchLabel;
    }

    // ================================================================
    // Utility methods
    // ================================================================
    private void addTask(ObservableList<HBox> displayBoxes,
                         int index,
                         Task task,
                         boolean includeDate) {
        if (task.isCompleted()) {
            displayBoxes.add(new TaskBox(index,
                                         task.getDescription(),
                                         task.getFormattedTimeAndDate(includeDate),
                                         task.isRecurring(),
                                         true));
        } else {
            displayBoxes.add(new TaskBox(index,
                                         task.getDescription(),
                                         task.getFormattedTimeAndDate(includeDate),
                                         task.isRecurring()));
        }
    }

    /**
     * Adds a label showing the number of tasks that are not displayed.
     * 
     * @param displayBoxes
     */
    private void addNumExcessTasksLabel(ObservableList<HBox> displayBoxes) {
        if (numExcessTasks > 0) {
            displayBoxes.add(new CategoryBox(String.format("... and %d more tasks",
                                                           numExcessTasks)));
        }
    }

    /**
     * Returns a sublist of MAX_NUM_OF_TASKS number of tasks.
     * 
     * @param tasks
     * @return ArrayList of size MAX_NUM_OF_TASKS
     */
    private ArrayList<Task> trimListOfTasks(ObservableList<Task> tasks) {
        numExcessTasks = tasks.size() - 100;
        if (numExcessTasks > 0) {
            return new ArrayList<Task>(tasks.subList(0, 100));
        }
        return new ArrayList<Task>(tasks);
    }

    // Formats the ArrayList<String> so that it prints element by element
    private String generateParagraph(ArrayList<String> list, int size) {
        return StringUtils.join(list.toArray(), "\n", 0, size);
    }
}

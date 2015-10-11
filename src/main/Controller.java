package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import main.view.DisplayController;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Controller {
    private Stage stage;
    private static Controller controller;
    private static Logger logger;
    private Storage storage;
    private History previousStates;
    private DateParser parser;
    private CreateTask taskCreator;
    private DisplayController displayController = DisplayController.getInstance();
    
    private ArrayList<Task> allTasks;
    private ObservableList<Task> displayedTasks = FXCollections.observableArrayList();
    private String searchArgument;

    private boolean switchDisplayToSearch = false;
    
    private Controller() {
        logger = Logger.getLogger("Controller");
        logger.setLevel(Level.OFF);

        parser = DateParser.getInstance();
        storage = Storage.getInstance();
        taskCreator = CreateTask.getInstance();
        allTasks = storage.readFile();
        previousStates = new History();       
        loadIncompleteTasks();
        warmUpParser();
    }

	public static Controller getInstance() {
	    if (controller == null) {
	        controller = new Controller();
	        controller.initFirstDisplay();
	    }
	    return controller;
	}

	// To load the tasks into the display on the first load
    private void initFirstDisplay() {
        displayController.setFeedback(getWelcomeMessage());
        displayController.updateOverviewDisplay(displayedTasks);
    }

    // ================================================================
    // Public methods
    // ================================================================
    public String getWelcomeMessage() {
        return "Welcome to Fini!";
    }

    // Based on what the user has type, this method will call the respective methods
    public String executeCommand(String input) {
        Command currentCommand = new Command(input);
        Command.Type commandType = currentCommand.getCommandType();
        String arguments = currentCommand.getArguments();
        String feedback = "";
        boolean helpUser = false;

        logger.log(Level.INFO, "User's input: " + input);
        logger.log(Level.INFO, "Type of command: " + commandType.toString());
        logger.log(Level.INFO, "Arguments: " + arguments);

        switch (commandType) {
        	
        	case SET :
	            feedback = setSaveFileDirectory(arguments);
	            break;
	            
            case MOVE :
                feedback = moveSaveFileDirectory(arguments);
                break;
	        
        	case ADD :
	            saveCurrentState(input);
	            feedback = addTask(arguments);
	            switchDisplayToSearch = false;
	            break; 
	        
        	case DELETE :
	            saveCurrentState(input);
	            feedback = deleteTask(arguments);
	            break;
	        
        	case EDIT :
	            saveCurrentState(input);
	            feedback = editTask(arguments);
	            break;
	        
        	case DISPLAY :
                feedback = displayTask(arguments);
	            break;
	        
        	case COMPLETE :
	            saveCurrentState(input);
	            feedback = completeTask(arguments);
	            break;
	        
        	case INCOMPLETE :
	            saveCurrentState(input);
                feedback = incompleteTask(arguments);
	            break;
	        
        	case UNDO :
	            feedback = undo();
	            break;
	        
        	case SEARCH :
	            search(arguments);
                searchArgument = arguments;
	            switchDisplayToSearch = true;
	            break;
	        
        	case CLEAR :
	        	saveCurrentState(input);
	        	feedback = clear();
	        	break;
	        
        	case INVALID :
	            feedback = invalid();
	            break;
	        
        	case HELP :
	        	helpUser = true;
	        	break;
	        
        	case EXIT :
	        	exit();
                stage.hide();
	            break;
        }
        showAppropriateDisplay(helpUser);
        displayController.setFeedback(feedback);
        return feedback;
    }

    // ================================================================
    // Initialization methods
    // ================================================================

    public void setStage(Stage stage) {
	    this.stage = stage;
	}
    
    // Fixes the delay when adding first task upon start up
	private void warmUpParser() {
		parser.parse("foo today");
	}

	// Load the incomplete tasks into displayedTasks
	private void loadIncompleteTasks() {
		for (Task task : getIncompleteTasks(allTasks)) {
            displayedTasks.add(task);
        }
	}

	// ================================================================
	// Getters
	// ================================================================
	
	public ObservableList<Task> getDisplayedTasks() {
	    return displayedTasks;
	}

    private ArrayList<Task> getIncompleteTasks(ArrayList<Task> allTasks) {
        List<Task> incompleteTasks = allTasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
        return (ArrayList<Task>) incompleteTasks;
    }

    private List<Task> getCompletedTasks(ArrayList<Task> allTasks) {
        List<Task> completedTasks = allTasks.stream()
                .filter(task -> task.isCompleted())
                .collect(Collectors.toList());
        return completedTasks;
    }

    // ================================================================
    // Logic methods
    // ================================================================
    private String addTask(String input) {
        if (input.isEmpty()) {
            return "Invalid command.";
        }
        try {
            parser.parse(input);
        } catch (DateTimeException e) {
            return String.format("Task was not created as %s", e.getMessage());
        }
        Task task;
        ArrayList<LocalDateTime> parsedDates = parser.getDates();
        String parsedWords = parser.getParsedWords();
        String notParsedWords = parser.getNotParsedWords();
        ArrayList<Task> newTask = new ArrayList<Task>();

        // Instantiate a new Task object
        try {
            newTask = taskCreator.create(input,
                                         parsedDates,
                                         parsedWords,
                                         notParsedWords);
            task = newTask.get(0);
        } catch (IndexOutOfBoundsException e) {
            return "Invalid command.";
        }
        allTasks.addAll(newTask);
        updateStorageWithAllTasks();
        return String.format("Task has been successfully added: %s", task);
    }

    /**
     *
     * Deletes the task with the selected index. Replaces it with a new task by calling addTask
     * with the extracted arguments.
     *
     */
    private String editTask(String input) {
        String[] inputArray;
        int editIndex;
        boolean editAll = false;
        Task editTask;

        // Check if it's an edit all
        if (input.toLowerCase().contains("all")) {
            input = input.replace("all", "").trim();
            editAll = true;
            logger.log(Level.INFO, "Contains 'all' in edit");
        }

        try {
            inputArray = input.split(" ");
            // ArrayList is 0-indexed, but Tasks are displayed to users as 1-indexed
            editIndex = Integer.parseInt(inputArray[0]) - 1;
            editTask = displayedTasks.get(editIndex);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return "The task you specified could not be found.";
        }

        // Creates an input to addTask
        String[] addArgumentArray =  new String[inputArray.length - 1];
        System.arraycopy(inputArray, 1, addArgumentArray, 0, inputArray.length - 1);
        String addArgument = String.join(" ", addArgumentArray);

        if (addArgument.isEmpty()) {
            return "Invalid command.";
        }

        if (editAll && editTask.getId() != null) {
            deleteAllTasks(editTask);
            addTask(addArgument);
            return String.format("All recurring task has been successfully edited: %s", editTask);
        } else {
            deleteIndividualTask(editTask);
            addTask(addArgument);
        }
        
        checkPreviousDisplay();

        return String.format("Task has been successfully edited: %s", editTask);
    }

    private String deleteTask(String input) {
        boolean deleteAll = false;
        Task removeTask;

        if (input.toLowerCase().contains("all")) {
            // Remove the "all" keyword so the try-catch can parse it properly
            input = input.toLowerCase().replace("all", "").trim();
            deleteAll = true;
        }

        try {
            // ArrayList is 0-indexed, but Tasks are displayed to users as 1-indexed
            int removalIndex = Integer.parseInt(input) - 1;
            removeTask = displayedTasks.get(removalIndex);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return "The task you specified could not be found.";
        }

        if (deleteAll && removeTask.getId() != null) {
            deleteAllTasks(removeTask);
            return String.format("All recurring task has been successfully deleted: %s", removeTask.getDescription());
        } else {
            deleteIndividualTask(removeTask);
            return String.format("Task has been successfully deleted: %s", removeTask);
        }
    }

    private void deleteIndividualTask(Task taskToDelete) {
        if (taskToDelete.isRecurring()) {
            for (Task task : allTasks) {
                if (task.getId() != null && task.getId().equals(taskToDelete.getId())) {
                    task.addException(taskToDelete.getDate());
                }
            }
        }
        displayedTasks.remove(taskToDelete);
        allTasks.remove(taskToDelete);
        updateStorageWithAllTasks();
        logger.log(Level.INFO, "displayedTasks after individual deletion: " + displayedTasks);
    }

    private void deleteAllTasks(Task taskToDelete) {
        String recurringId = taskToDelete.getId();
        ArrayList<Task> tasksToDelete = new ArrayList<Task>();

        for (Task task : allTasks) {
            if (task.getId() != null && task.getId().equals(recurringId)) {
                tasksToDelete.add(task);
            }
        }
        displayedTasks.removeAll(tasksToDelete);
        allTasks.removeAll(tasksToDelete);
        updateStorageWithAllTasks();
        logger.log(Level.INFO, "displayedTasks after all deletion: " + displayedTasks);
    }

    private String completeTask(String input) {
        try {
            int index = Integer.parseInt(input.trim()) - 1;
            Task task = displayedTasks.get(index);
            
            if (task.isCompleted()) {
                return String.format("\"%s\" already completed.", task.getDescription());
            }
            
            task.markAsCompleted();
            logger.log(Level.INFO, "the completed task: " + task.toString());

            updateStorageWithAllTasks();
            checkPreviousDisplay();

            logger.log(Level.INFO, "completed tasks after complete: " + getCompletedTasks(allTasks));
            return String.format("\"%s\" completed.", task.getDescription());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return "The task you specified could not be found.";
        }
    }

    private String incompleteTask(String input) {
        try {
            int index = Integer.parseInt(input.trim()) - 1;
            Task task = displayedTasks.get(index);
            task.markAsIncomplete();
            logger.log(Level.INFO, "the incompleted task: " + task.toString());

            updateStorageWithAllTasks();
            checkPreviousDisplay();
            
            logger.log(Level.INFO, "incomplete tasks after incomplete: " + getIncompleteTasks(allTasks));
            return String.format("\"%s\" marked as incomplete.", task.getDescription());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return "The task you specified could not be found.";
        }
    }

    // The previous state of the ArrayList and the ObservableList are restored
    private String undo() {
    	assert previousStates != null;
        if (previousStates.isEmpty()) {
            return "Already at oldest change, unable to undo.";
        }
        previousStates.getPreviousState();
        restorePreviousState(); 
        updateStorageWithAllTasks(); 
        checkPreviousDisplay();
        return String.format("Previous command has been undone: \"%s\"", previousStates.getPreviousCommand());
    }
    
    // Execute search if the previous display is on search display
	private void checkPreviousDisplay() {
		if (switchDisplayToSearch) {
			search(searchArgument);
		}
	}

	// Assign the allTasks and displayedTasks field to its previous state
	private void restorePreviousState() {
		allTasks = previousStates.getAllTasks();
		displayedTasks = previousStates.getDisplayedTasks();
	}

    private void search(String input) {
        if (input.equals("completed")) {
        	updateDisplayWithCompleted();
        } else {
        	displayedTasks.clear();
	        parser.parse(input);
	        ArrayList<LocalDateTime> searchDate = parser.getDates();
	        for (Task task : allTasks) {
	            String taskInfo = task.getDescription().toLowerCase();
	            if (taskInfo.contains(input.toLowerCase())) {
	                displayedTasks.add(task);
	            } else if (searchDate.size() > 0
	                    && searchDate.get(0).toLocalDate().equals(task.getDate())) {
	                displayedTasks.add(task);
	            }
	        }
        }
    }

    private String displayTask(String input) {
        displayedTasks.clear();

        if (input.equals("completed")) {
            switchDisplayToSearch = true;
            searchArgument = input;
            updateDisplayWithCompleted();
            return "Displaying all completed tasks.";
        } else if (input.equals("")) {
            switchDisplayToSearch = false;
            searchArgument = null;
            return "Displaying all incomplete tasks.";
        } else {
            return "Invalid command.";
        }
    }

    private String invalid() {
        return "Invalid command.";
    }
    
    private String moveSaveFileDirectory(String input) {
        if (storage.moveSaveFileDirectory(input)) {
            return "Save file has been moved.";
        } else {
            return "Moving save file failed.";
        }
    }
    
    private String setSaveFileDirectory(String input) {
        if (storage.setSaveFileDirectory(input)) {
            allTasks = storage.readFile();
            return "File save destination has been confirmed.";
        } else {
            return "File save destination failed.";
        }
    }
    
    private String clear() {
        allTasks = new ArrayList<Task>();
        displayedTasks = FXCollections.observableArrayList();;
        storage.updateFiles(allTasks);
        displayController.resetScrollIndex();
        return "All tasks have been deleted!";
    }

    private void exit() {
        updateStorageWithAllTasks();
    }

    // ================================================================
    // Utility methods
    // ================================================================
    // Based on what input the user has typed, 
    // this method will determine the appropriate screen to display
    private void showAppropriateDisplay(boolean helpUser) {
    	if (helpUser) {
        	updateHelpDisplay();
        } else if (switchDisplayToSearch) {
            updateDisplaySearch();
        } else {
            updateDisplayWithDefault();
        }
    }
    
    private void updateDisplayWithDefault() {
        displayedTasks.setAll(getIncompleteTasks(allTasks));
        displayController.updateOverviewDisplay(displayedTasks);
        logger.log(Level.INFO, "Displayed tasks: " + displayedTasks);
    }

    private void updateDisplayWithCompleted() {
        displayedTasks.setAll(getCompletedTasks(allTasks));
        displayController.updateSearchDisplay(displayedTasks, "completed");
        logger.log(Level.INFO, "Displayed tasks: " + displayedTasks);
    }
    
    // Call the display object to show the "search" display
    private void updateDisplaySearch() {
    	assert displayedTasks != null;
        displayController.updateSearchDisplay(displayedTasks, searchArgument);
    }
    
    // Call the display object to show the "help" display
    private void updateHelpDisplay() {
    	displayController.showHelpDisplay();
    }

    // Save the current state of allTasks and displayedTasks field before execution of command
    private void saveCurrentState(String input) {
    	assert allTasks != null;
        assert displayedTasks != null;
        previousStates.storeCurrentState(allTasks, displayedTasks);
        previousStates.storeCommand(input);
    }

    private void updateStorageWithAllTasks() {
        storage.updateFiles(allTasks);
    }
}

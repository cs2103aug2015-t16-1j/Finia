package main;

import java.util.ArrayList;

public class Command {
    public static enum Type {
        ADD, DISPLAY, DELETE, EDIT, COMPLETE, INCOMPLETE, UNDO, SEARCH, EXIT,
        SET, CLEAR, HELP, INVALID, MOVE
    };

    private static final int PARAM_POSITION_COMMAND = 0;
    private static final int PARAM_START_POSITION_ARGUMENT = 1;

    private static final String STRING_ONE_SPACING = " ";

    private Type commandType;
    private String userCommand;
    private String arguments;

    public Command(String input) {
        String[] parameters = splitUserInput(input);

        userCommand = getUserCommand(parameters);
        arguments = getUserArguments(parameters);

        commandType = determineCommandType(userCommand);
    }

    
    // ================================================================
    // Public getters
    // ================================================================
    /**
     * Get all command types as an ArrayList of Strings.
     * 
     * @return ArrayList of String representations of command types
     */
    public static ArrayList<String> getAllCommandTypes() {
        ArrayList<String> allCommands = new ArrayList<String>();
        for (Type command : Type.values()) {
            allCommands.add(command.toString());
        }
        return allCommands;
    }

    public Type getCommandType() {
        return commandType;
    }

    public String getArguments() {
        return arguments;
    }


    // ================================================================
    // Private methods
    // ================================================================
    private Type determineCommandType(String userCommand) {
        switch (userCommand.toLowerCase()) {
            case "add" :
                return Type.ADD;
            case "delete" :
                return Type.DELETE;
            case "edit" :
                return Type.EDIT;
            case "display" :
                return Type.DISPLAY;
            case "complete" :
                return Type.COMPLETE;
            case "incomplete" :
                return Type.INCOMPLETE;
            case "undo" :
                return Type.UNDO;
            case "search" :
                return Type.SEARCH;
            case "help" :
                return Type.HELP;
            case "exit" :
                return Type.EXIT;
            case "set" :
                return Type.SET;
            case "move" :
                return Type.MOVE;
            case "clear" :
                return Type.CLEAR;
            default :
                return Type.INVALID;
        }
    }

    private String getUserCommand(String[] parameters) {
        return parameters[PARAM_POSITION_COMMAND];
    }

    private String getUserArguments(String[] parameters) {
        StringBuilder builder = new StringBuilder();
        for (int i = PARAM_START_POSITION_ARGUMENT; i < parameters.length; i++) {
            builder.append(parameters[i]);
            builder.append(STRING_ONE_SPACING);
        }
        return builder.toString().trim();
    }

    private String[] splitUserInput(String input) {
        return input.trim().split(STRING_ONE_SPACING);
    }
}

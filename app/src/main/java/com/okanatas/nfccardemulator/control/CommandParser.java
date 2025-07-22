package com.okanatas.nfccardemulator.control;

public class CommandParser {

    /**
     * Parses a command string and returns the corresponding Command object.
     *
     * @param input The command string to parse.
     * @return The Command object corresponding to the command string.
     */
    public Command parseCommand(String input) {
        String[] lines = input.split("\\r?\\n");
        String command = null;
        StringBuilder dataBuilder = new StringBuilder();
        boolean inData = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()) continue;

            if (line.toLowerCase().startsWith("command:")) {
                command = line.substring("command:".length()).trim();
            } else if (line.toLowerCase().startsWith("data:")) {
                inData = true;
            } else if (inData) {
                dataBuilder.append(line).append("\n");
            } else if (command == null) {
                // maybe the command is on the next line
                command = line;
            }
        }

        if (command == null) {
            return null; // or throw an exception
        }

        String data = dataBuilder.length() > 0 ? dataBuilder.toString().trim() : null;
        switch (command) {
            case "StartService":
                return new StartServiceCommand();
            case "StopService":
                return new StopServiceCommand();
            case "SetActiveFile":
                return createSetActiveFileCommand(data);
            default:
                return null;
        }
    }

    private Command createSetActiveFileCommand(String data) {
        SetActiveFileCommand activeFileCommand = new SetActiveFileCommand();
        if (data != null && !data.isEmpty()) {
            activeFileCommand.setContent(data);
        } else {
            activeFileCommand.setContent(""); // or handle as needed
        }
        return activeFileCommand;
    }
}

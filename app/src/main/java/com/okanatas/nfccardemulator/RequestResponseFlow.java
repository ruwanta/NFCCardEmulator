package com.okanatas.nfccardemulator;

import android.util.Log;

import com.okanatas.nfccardemulator.collections.WildcardMap;


/**
 * Structure holding a graph of request and its response.
 * The responses may optionaly have a delay in milliseconds.
 *
 */
public class RequestResponseFlow {

    private WildcardMap<RequestResponse> requestResponseMap = new WildcardMap<>();

    /** Command keyword for the txt file */
    private static final String COMMAND_KEYWORD = InformationTransferManager.getStringResource(R.string.c_apdu_keyword);
    /** Response keyword for the txt file */
    private static final String RESPONSE_KEYWORD = InformationTransferManager.getStringResource(R.string.r_apdu_keyword);
    private static final String DELAY_KEYWORD = InformationTransferManager.getStringResource(R.string.delay_keyword);

    /**
     * This method was created to save commands and responses to the ArrayList.
     * @param fileContentInText file content in String format.
     */
    public void setCommandsAndResponses(String fileContentInText){
        // clear ArrayList elements for a new file initialization
        WildcardMap<RequestResponse> newMap = new WildcardMap<>();


        // store the organized content line by line
        String[] lines = fileContentInText.split("\\r?\\n");

        // determine which line is command and which is response and store it to ArrayList.

        ParserContext parserContext = new ParserContext();
        parserContext.lines = lines;
        parserContext.index = 0;
        readCommandsAndResponses(parserContext, newMap);

        requestResponseMap = newMap;
    }

    /**
     * Reads commands and responses from the parser context and populates the request-response map.
     * @param parserContext
     * @param newMap
     */
    private void readCommandsAndResponses(ParserContext parserContext, WildcardMap<RequestResponse> newMap) {
        String line = parserContext.lines[parserContext.index].trim();
        String[] parsedLine = line.split(":");
        String command = null;

        String keyword = parsedLine[0];
        parserContext.command = command;
        if (keyword.equalsIgnoreCase(COMMAND_KEYWORD)) {
            if((parsedLine[1].length() % 2 == 0) || (parsedLine[1].length() >= ISOProtocol.MIN_APDU_LENGTH)){
                command = parsedLine[1];
            }
        }
        if(command != null) {
            int index = parserContext.index;
            parserContext.command = command.trim();
            if (index + 1 < parserContext.lines.length) {
                parserContext.index = index + 1;
                readDelayAndResponses(parserContext, newMap);
            } else {
                Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_2), false);
            }
        } else {
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_2), false);
        }
    }

    /**
     * Reads delay and responses from the parser context and populates the request-response map.
     * @param parserContext
     * @param newMap
     */
    private void readDelayAndResponses(ParserContext parserContext, WildcardMap<RequestResponse> newMap) {
        String line = parserContext.lines[parserContext.index].trim();
        String[] parsedLine = line.split(":");

        long delay = 0;
        String keyword = parsedLine[0];
        parserContext.delay = 0;
        if (keyword.equalsIgnoreCase(DELAY_KEYWORD)) {
            String ln = parsedLine[1];
            if (ln == null || ln.isEmpty()) {
                Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_1), false);
                return;
            }
            ln = ln.trim();
            try{
                delay = Long.parseLong(ln);
                parserContext.delay = delay;
                parserContext.index++;
            } catch (NumberFormatException e) {
                Log.e("RequestResponseFlow", "Invalid delay value: " + ln, e);
            }
            readResponse(parserContext, newMap);
        } else if (keyword.equalsIgnoreCase(RESPONSE_KEYWORD)) {
            readResponse(parserContext, newMap);
        } else {
            readResponse(parserContext, newMap);
        }
    }

    /**
     * Reads a response from the parser context and adds it to the request-response map.
     * @param parserContext
     * @param newMap
     */
    private void readResponse(ParserContext parserContext, WildcardMap<RequestResponse> newMap) {

        String line = parserContext.lines[parserContext.index].trim();
        String[] parsedLine = line.split(":");
        String response = null;
        String keyword = parsedLine[0];
        if (keyword.equalsIgnoreCase(RESPONSE_KEYWORD)) {
            response = parsedLine[1].trim();
            RequestResponse requestResponse = new RequestResponse(parserContext.command, response, parserContext.delay);
            newMap.put(parserContext.command, requestResponse);
        } else {
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_1), false);
        }
        parserContext.index++;
        if( parserContext.index < parserContext.lines.length) {
            readCommandsAndResponses(parserContext, newMap);
        } else {
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_1), false);
        }
    }

    /**
     * Clears the current request-response flow.
     */
    public void clear() {
        requestResponseMap.clear();
    }

    public RequestResponse getRequestResponse(String hexCommandApdu) {
        if (hexCommandApdu == null || hexCommandApdu.isEmpty()) {
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_2), false);
            return null;
        }

        RequestResponse requestResponse = requestResponseMap.get(hexCommandApdu);
        if (requestResponse == null) {
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), hexCommandApdu, false);
        }
        return requestResponse;
    }

    /**
     * ParserContext is a helper class to hold the state of the parsing process.
     */
    private static class ParserContext{
        private String command;
        private String response;
        private long delay;
        private String[] lines;
        private int index;
    }
}

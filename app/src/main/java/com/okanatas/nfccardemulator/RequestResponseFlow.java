package com.okanatas.nfccardemulator;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure holding a graph of request and its response.
 * The responses may optionaly have a delay in milliseconds.
 *
 */
public class RequestResponseFlow {

    private Map<String, RequestResponse> requestResponseMap = new HashMap<>();

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
        Map<String, RequestResponse> newMap = new HashMap<>();

        // remove all white spaces for organization purpose
        fileContentInText = fileContentInText.replaceAll("\\s","");

        // store the organized content line by line
        String[] lines = fileContentInText.split("\\r?\\n");

        // determine which line is command and which is response and store it to ArrayList.
        for (String line : lines) {
            String[] parsedLine = line.split(":");
            String command = null;
            String response = null;
            long delay = 0;
            String keyword = parsedLine[0];

            if (keyword.equalsIgnoreCase(COMMAND_KEYWORD)) {
                if((parsedLine[1].length() % 2 == 0) || (parsedLine[1].length() >= ISOProtocol.MIN_APDU_LENGTH)){
                    command = parsedLine[1];
                }
            } else if (keyword.equalsIgnoreCase(RESPONSE_KEYWORD)) {
                if((parsedLine[1].length() % 2) == 0){
                    response = parsedLine[1];
                }
            } else if (keyword.equalsIgnoreCase(DELAY_KEYWORD)) {
                if((parsedLine[1].length() % 2) == 0){
                    String ln = parsedLine[1];
                    try{
                        delay = Long.parseLong(ln);
                    } catch (NumberFormatException e) {
                        Log.e("RequestResponseFlow", "Invalid delay value: " + ln, e);
                    }
                }
            }

            if (command != null && response != null) {
                RequestResponse requestResponse = new RequestResponse(command, response, delay);
                newMap.put(command, requestResponse);
            } else {
                Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_2), false);
            }
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
}

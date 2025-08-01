package com.okanatas.nfccardemulator;

/**
 * Structure representing a PDU Request and is response along with the delay where the response shluld be sent.
 */
public class RequestResponse {

    private String command;
    private String response;
    private long delay = 0;


    public RequestResponse(String command, String response) {
        this.command = command;
        this.response = response;
    }

    public RequestResponse(String command, String response, long delay) {
        this.command = command;
        this.response = response;
        this.delay = delay;
    }

    public byte[] getResponseApdu() {
        return Utils.hexStringToByteArray(response);
    }

    public long getDelay() {
        return delay;
    }
}

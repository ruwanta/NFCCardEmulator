package com.okanatas.nfccardemulator.control;


/**
 * Command to signal the service to stop listening for NFC.
 */
public class StopServiceCommand implements Command {

    /**
     * Command to stop the listening service.
     */
    public StopServiceCommand() {
        // Constructor can be empty or can include initialization logic if needed
    }

    @Override
    public String toString() {
        return "StopServiceCommand{}";
    }
}

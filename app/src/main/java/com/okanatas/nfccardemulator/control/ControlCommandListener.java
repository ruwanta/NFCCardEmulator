package com.okanatas.nfccardemulator.control;

/**
 * Control commands listener interface.
 */
public interface ControlCommandListener {
    void onCommandReceived(Command command);
}

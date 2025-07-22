package com.okanatas.nfccardemulator.control;

/**
 * Command to set the active file in the NFC card emulator.
 */
public class SetActiveFileCommand implements Command{

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

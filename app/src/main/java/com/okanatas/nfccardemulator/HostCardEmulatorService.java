package com.okanatas.nfccardemulator;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * This class was created for the Host Based Card Emulator Service.
 * @author Okan Atas,
 * @version 1.0,
 * created on June 30, 2021
 */
public class HostCardEmulatorService extends HostApduService {

    private static final String C_TAG = InformationTransferManager.getStringResource(R.string.command_tag);
    private static final String R_TAG = InformationTransferManager.getStringResource(R.string.response_tag);
    private static final String COM_TAG = "\n" + InformationTransferManager.getStringResource(R.string.communication_tag);
    private static boolean isNewCommunication = true;

    private ResponseHandler2 responseHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        responseHandler = new ResponseHandler2(EmulatorApplication.getInstance().getRequestResponseFlow());
    }

    /**
     * This method was created to take that command the terminal sends,
     * and then send back a response in the format that the terminal requires.
     * @param commandApdu The APDU that was received from the remote device.
     * @param extras a bundle containing extra data (may be null).
     * @return a byte-array containing the response APDU, or null if no response APDU can be sent at this point.
     */
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte[] responseApdu = null;

        RequestResponse requestResponse = null;

        if(commandApdu != null){

            String hexCommandApdu = Utils.toHexString(commandApdu);

            if((hexCommandApdu.length() < ISOProtocol.MIN_APDU_LENGTH) || (hexCommandApdu.length() % 2 != 0)){
                responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
                ResponseHandler2.setSelectedInsDescription(InformationTransferManager.getStringResource(R.string.command_aborted_with_reason));
            }else {
                /* Switch for INS : this is the index 1 for commandApdu byte array */
                switch (hexCommandApdu.substring(2, 4)) {
                    case ISOProtocol.INS_SELECT:
                        requestResponse = responseHandler.selectCase(hexCommandApdu);
                        responseApdu = requestResponse.getResponseApdu();
                        break;
                    case ISOProtocol.INS_READ_BINARY:
                        responseApdu = ResponseHandler2.readBinaryCase();
                        break;
                    case ISOProtocol.INS_WRITE_BINARY:
                        responseApdu = ResponseHandler2.writeBinaryCase();
                        break;
                    case ISOProtocol.INS_UPDATE_BINARY:
                        responseApdu = ResponseHandler2.updateBinaryCase();
                        break;
                    case ISOProtocol.INS_READ_RECORD:
                        requestResponse = responseHandler.readRecordCase(hexCommandApdu);
                        break;
                    case ISOProtocol.INS_READ_NDEF:
                        responseApdu = ResponseHandler2.readNdefCase();
                        break;
                    case ISOProtocol.INS_PERFORM_SECURITY_OPERATION:
                        responseApdu = ResponseHandler2.performSecurityOperationCase();
                        break;
                    case ISOProtocol.INS_GET_PROCESSING_OPTIONS:
                        requestResponse = responseHandler.getProcessingOptionCase(hexCommandApdu);
                        responseApdu = requestResponse.getResponseApdu();
                        break;
                    case ISOProtocol.INS_GENERATE_APPLICATION_CRYPTOGRAM:
                        requestResponse = responseHandler.getProcessingOptionCase(hexCommandApdu);
                        break;
                    case ISOProtocol.INS_GET_DATA:
                        responseApdu = ResponseHandler2.getDataCase();
                        break;
                    default:
                        responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_INS_NOT_SUPPORTED_OR_INVALID);
                }
            }

        }else{
            responseApdu =  Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
        }

        String commandApduInHex = (commandApdu != null) ? (Utils.toHexString(commandApdu)) : (InformationTransferManager.getStringResource(R.string.null_command));

        if(requestResponse != null) {
            //Handled response with delay
            responseApdu = requestResponse.getResponseApdu(); // Get the response bytes
            long delay = requestResponse.getDelay();

            if (delay > 0) {
                try {
                    // WARNING: Blocking the processCommandApdu thread.
                    // This is to emulate response delays, to simulate error cases.
                    Log.d("HCE_DELAY", "Intentionally delaying APDU response by " + delay + "ms");
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    Log.w("HCE_DELAY", "APDU delay interrupted");
                    // Potentially return an error APDU here
                    return Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
                }
            }

            final String responseApduInHex = Utils.toHexString(responseApdu);
            // Log immediately or after delay, but the response is now delayed by Thread.sleep()
            handleCommunicationMessage(commandApduInHex, responseApduInHex); // Or post this if it needs main thread


        } else {
            handleCommunicationMessage(commandApduInHex, Utils.toHexString(responseApdu));
        }

        /* return the response APDU */
        return responseApdu;
    }

    /**
     * This method will be called in two possible scenarios:
     * 1) The NFC link has been deactivated or lost
     * 2) A different AID has been selected and was resolved to a different service component
     * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
     */
    @Override
    public void onDeactivated(int reason) {
        Utils.showLogDMessage(COM_TAG, "\n" + InformationTransferManager.getStringResource(R.string.communication_ended) + "\n", true);
        isNewCommunication = true;
        MainScreenFragment.getInstance().showCommunicationMessages();
    }

    /**
     * This method handles the communication between the terminal and the emulated card.
     * @param commandApduInHex command APDU in hexadecimal format.
     * @param responseApduInHex response APDU in hexadecimal format.
     */
    private void handleCommunicationMessage(String commandApduInHex, String responseApduInHex){

        // check if it is a new communication
        if(isNewCommunication){
            Utils.showLogDMessage(COM_TAG, InformationTransferManager.getStringResource(R.string.communication_started) + "\n", true);
            isNewCommunication = false;
        }

        Utils.showLogDMessage(C_TAG, commandApduInHex, true);
        Utils.showLogDMessage(C_TAG, ResponseHandler2.getSelectedInsDescription(), false);
        Utils.showLogDMessage(R_TAG, responseApduInHex, true);

        InformationTransferManager.appendCardCommunicationMessage(InformationTransferManager.getStringResource(R.string.apdu_c) + commandApduInHex);
        InformationTransferManager.appendCardCommunicationMessage(InformationTransferManager.getStringResource(R.string.apdu_r) + responseApduInHex + "\n");

    }

}

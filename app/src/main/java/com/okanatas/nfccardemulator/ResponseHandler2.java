package com.okanatas.nfccardemulator;

/**
 * This class was created to select responses for command APDU.
 * @author Okan Atas,
 * @version 1.0,
 * created on June 30, 2021
 */
public class ResponseHandler2 {

    private static String selectedInsDescription;

    private RequestResponseFlow requestResponseFlow;

    /**
     * Constructor for ResponseHandler2.
     * @param requestResponseFlow RequestResponseFlow object to handle request and response flow.
     */
    public ResponseHandler2(RequestResponseFlow requestResponseFlow) {
        this.requestResponseFlow = requestResponseFlow;
    }

    /**
     * Command APDU - Select Case.
     * @param hexCommandApdu command APDU in hexadecimal format.
     * @return response APDU.
     */
    public RequestResponse selectCase(String hexCommandApdu){
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_select_case);

        RequestResponse requestResponse = requestResponseFlow.getRequestResponse(hexCommandApdu);
        if(requestResponse == null) {
            requestResponse = createDefaultResponse(hexCommandApdu, ISOProtocol.SW_FILE_NOT_FOUND);
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_1), false);
        }
        return requestResponse;
    }

    private RequestResponse createDefaultResponse(String hexCommandApdu, String responseApdu) {
        return new RequestResponse(hexCommandApdu, responseApdu, 0);
    }

    /**
     * Command APDU - Read Binary Case.
     * @return response APDU.
     */
    static byte[] readBinaryCase(){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_RECORD_NOT_FOUND);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_read_binary);

        for(int i = 0; i < FileHandler.commands.size(); i++){
            if(ISOProtocol.INS_READ_BINARY.equals(FileHandler.commands.get(i).substring(2,4))){
                responseApdu = Utils.hexStringToByteArray(FileHandler.responses.get(i));
                break;
            }
        }
        return responseApdu;
    }

    /**
     * Command APDU - Get Processing Option Case.
     * @param hexCommandApdu command APDU in hexadecimal format.
     * @return response APDU.
     */
    public RequestResponse getProcessingOptionCase(String hexCommandApdu){
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_get_processing_option);

        RequestResponse requestResponse = requestResponseFlow.getRequestResponse(hexCommandApdu);
        if(requestResponse == null) {
            requestResponse = createDefaultResponse(hexCommandApdu, ISOProtocol.SW_COMMAND_ABORTED);
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_2), false);
        }
        return requestResponse;
    }

    /**
     * Command APDU - Read Record Case.
     * @param hexCommandApdu command APDU in hexadecimal format.
     * @return response APDU.
     */
    public RequestResponse readRecordCase(String hexCommandApdu){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_RECORD_NOT_FOUND);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_read_record);

        RequestResponse requestResponse = requestResponseFlow.getRequestResponse(hexCommandApdu);
        if(requestResponse == null) {
            requestResponse = createDefaultResponse(hexCommandApdu, ISOProtocol.SW_RECORD_NOT_FOUND);
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_2), false);
        }
        return requestResponse;
    }

     /**
     * Command APDU - Perform Security Operation Case.
     * @return response APDU.
     */
    static byte[] performSecurityOperationCase(){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_perform_security);

        for(int i = 0; i < FileHandler.commands.size(); i++){
            if(ISOProtocol.INS_PERFORM_SECURITY_OPERATION.equals(FileHandler.commands.get(i).substring(2,4))){
                responseApdu = Utils.hexStringToByteArray(FileHandler.responses.get(i));
                break;
            }
        }
        return responseApdu;
    }

    /**
     * Command APDU - Read NDEF Case.
     * @return response APDU.
     */
    static byte[] readNdefCase(){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_read_ndef);

        for(int i = 0; i < FileHandler.commands.size(); i++){
            if(ISOProtocol.INS_READ_NDEF.equals(FileHandler.commands.get(i).substring(2,4))){
                responseApdu = Utils.hexStringToByteArray(FileHandler.responses.get(i));
                break;
            }
        }
        return responseApdu;
    }

    /**
     * Command APDU - Write Binary Case.
     * @return response APDU.
     */
    static byte[] writeBinaryCase(){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_write_binary);

        for(int i = 0; i < FileHandler.commands.size(); i++){
            if(ISOProtocol.INS_WRITE_BINARY.equals(FileHandler.commands.get(i).substring(2,4))){
                responseApdu = Utils.hexStringToByteArray(FileHandler.responses.get(i));
                break;
            }
        }
        return responseApdu;
    }

    /**
     * Command APDU - Update Binary Case.
     * @return response APDU.
     */
    static byte[] updateBinaryCase(){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_update_binary);

        for(int i = 0; i < FileHandler.commands.size(); i++){
            if(ISOProtocol.INS_UPDATE_BINARY.equals(FileHandler.commands.get(i).substring(2,4))){
                responseApdu = Utils.hexStringToByteArray(FileHandler.responses.get(i));
                break;
            }
        }
        return responseApdu;
    }

    /**
     * Command APDU - Generate Application Cryptogram Case.
     * @return response APDU.
     */
    public RequestResponse generateApplicationCryptogramCase(String hexCommandApdu){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_generate_app_cryptogram);

        RequestResponse requestResponse = requestResponseFlow.getRequestResponse(hexCommandApdu);
        if(requestResponse == null) {
            requestResponse = createDefaultResponse(hexCommandApdu, ISOProtocol.SW_RECORD_NOT_FOUND);
            Utils.showLogDMessage(InformationTransferManager.getStringResource(R.string.invalid_message_1), InformationTransferManager.getStringResource(R.string.invalid_message_2), false);
        }
        return requestResponse;
    }

    /**
     * Command APDU - Get Data Case.
     * @return response APDU.
     */
    static byte[] getDataCase(){
        byte[] responseApdu = Utils.hexStringToByteArray(ISOProtocol.SW_COMMAND_ABORTED);
        selectedInsDescription = InformationTransferManager.getStringResource(R.string.ins_get_data);

        for(int i = 0; i < FileHandler.commands.size(); i++){
            if(ISOProtocol.INS_GET_DATA.equals(FileHandler.commands.get(i).substring(2,4))){
                responseApdu = Utils.hexStringToByteArray(FileHandler.responses.get(i));
                break;
            }
        }
        return responseApdu;
    }

    /**
     * To get selected INS description.
     * @return selected INS description.
     */
    static String getSelectedInsDescription(){
        return selectedInsDescription;
    }

    /**
     * To set selected INS description.
     * @param description INS description.
     */
    static void setSelectedInsDescription(String description){
        selectedInsDescription = description;
    }

}

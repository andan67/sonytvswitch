package org.andan.av.sony.network;

import com.google.gson.JsonElement;

public class SonyJsonRpcResponse {
    /*
    REST API Response Example (Success Case)
        {"result": [{"name": "Alice"}], "id": 2}
    REST API Response Example (Failure Case)
        {"error": [401, "Unauthorized"], "id": 2}

    */
    private int id;
    private JsonElement result;
    private SonyJsonRpcError error;
    private int responseCode=0;
    private String responseMessage="";
    private String setCookie;

    public SonyJsonRpcResponse(int id, JsonElement result) {
        this.id = id;
        this.result = result;
        this.error = null;
    }

    public SonyJsonRpcResponse(int id, SonyJsonRpcError error) {
        this.id = id;
        this.error = error;
        this.result = null;
    }

    public SonyJsonRpcResponse(int id) {
        this.id = id;
        this.error = null;
        this.result = null;
    }

    public int getId() {
        return id;
    }

    public JsonElement getResult() {
        return result;
    }

    public SonyJsonRpcError getError() {
        return error;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMEssage) {
        this.responseMessage = responseMEssage;
    }

    public String getResponseStatusMessage(){
            return "Http response:" +
                    "status=" + responseCode +
                    ", message='" + responseMessage+ '\'';
    }

    public String getSetCookie() {
        return setCookie;
    }

    public void setSetCookie(String setCookie) {
        this.setCookie = setCookie;
    }

    public boolean isJsonRpcResponse() {
        return result!=null || error != null;
    }

    public boolean hasError() {
        return (error!=null || result==null);
    }


    public String getResponseErrorOrStatusMessage() {
        if(error!=null) {
            return error.toString();
        }
        else return getResponseStatusMessage();
    }

    public int getResponseErrorOrStatusCode() {
        if(error!=null) {
            return error.getCode();
        }
        else return responseCode;
    }
}

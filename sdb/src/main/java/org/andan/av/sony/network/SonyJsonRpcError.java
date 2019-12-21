package org.andan.av.sony.network;

public class SonyJsonRpcError {
    private int code;
    private String message;

    public SonyJsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "JSON-RPC error:" +
                "code=" + code +
                ", message='" + message + '\'';
    }
}

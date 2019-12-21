package org.andan.av.sony.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.iharder.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SonyJsonRpc {
    public static final Gson GSON =
            new GsonBuilder().create();

    public static SonyJsonRpcResponse decodeResponse(String response) {
        JsonResponse jsonResponse= GSON.fromJson(response, JsonResponse.class);
        if(jsonResponse.result!=null) {
            return new SonyJsonRpcResponse(jsonResponse.id, GSON.toJsonTree(jsonResponse.result));
        }
        else {
            return new SonyJsonRpcResponse(jsonResponse.id,
                    new SonyJsonRpcError(((Double) jsonResponse.error.get(0)).intValue(),
                    jsonResponse.error.get(1).toString()));
        }
    }

    class JsonResponse {
        int id;
        List<Object> result;
        List<Object> error;
    }

    private static SonyJsonRpcResponse execute(SonyJsonRpcRequest request, String urlString, String cookieString) {
        return execute(request, urlString, cookieString, null);
    }

    private static SonyJsonRpcResponse execute(SonyJsonRpcRequest request, String urlString, String cookieString, String userPassword) {

        HttpURLConnection connection = null;
        OutputStream os = null;
        int responseCode=-1;
        String responseMessage=null;
        String response = null;
        String setCookie = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            if(cookieString!=null && !cookieString.isEmpty()) {
                connection.setRequestProperty("Cookie", cookieString);
            }
            if(userPassword!=null && !userPassword.isEmpty()) {
                connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(userPassword.getBytes()));
            }
            connection.setDoOutput(true);
            os = connection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            String requestJson = GSON.toJson(request);
            osw.write(requestJson);
            osw.flush();

            responseCode = connection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_CREATED:
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + System.getProperty("line.separator"));
                    }
                    br.close();
                    response = sb.toString();
                    responseMessage = connection.getResponseMessage();
                    setCookie = connection.getHeaderField("Set-Cookie");
            }
        } catch (IOException e) {
            try {
                responseCode = connection.getResponseCode();
                responseMessage = connection.getResponseMessage();
            } catch (IOException e2) {
                responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
                responseMessage = e2.getMessage();
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            try {
                if (os != null) {
                    os.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e2) {
                responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
                responseMessage = e2.getMessage();
            }
        }
        SonyJsonRpcResponse sonyJsonRpcResponse = null;
        if(response!=null) {
            sonyJsonRpcResponse = decodeResponse(response);
        } else {
            sonyJsonRpcResponse = new SonyJsonRpcResponse(request.getId());
        }
        sonyJsonRpcResponse.setResponseCode(responseCode);
        sonyJsonRpcResponse.setResponseMessage(responseMessage);
        sonyJsonRpcResponse.setSetCookie(setCookie);
        return sonyJsonRpcResponse;
    }

    public static SonyJsonRpcResponse actRegister(
            String baseUrl, String clientd, String nickname, String cookieString, String userPassword) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("actRegister", 8)
                .addParam("clientid", clientd)
                .addParam("nickname", nickname)
                .addParam("level", "private")
                .addParamListItem()
                .addParamToNewList("value", "yes")
                .addParamToNewList("function", "WOL");
        return execute(request, baseUrl + "/accessControl", cookieString, userPassword);
    }

    public static SonyJsonRpcResponse getRemoteControllerInfo(
            String baseUrl, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getRemoteControllerInfo", 10);
        return execute(request, baseUrl + "/system", cookieString);
    }

    public static SonyJsonRpcResponse getSourceList(
            String baseUrl, String scheme, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getSourceList", 2)
                .addParam("scheme", scheme);
        return execute(request, baseUrl + "/avContent", cookieString);
    }

    public static SonyJsonRpcResponse setPlayContent(
            String baseUrl, String uri, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("setPlayContent", 101)
                .addParam("uri", uri);
        return execute(request, baseUrl + "/avContent", cookieString);
    }

    public static SonyJsonRpcResponse getPlayingContentInfo(
            String baseUrl, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getPlayingContentInfo", 103);
        return execute(request, baseUrl + "/avContent", cookieString);
    }

    public static SonyJsonRpcResponse getSystemInformation(
            String baseUrl, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getSystemInformation", 33);
        return execute(request, baseUrl + "/system", cookieString);
    }

    public static SonyJsonRpcResponse getContentList(
            String baseUrl, String source, int stIdx, int cnt, String type, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getContentList", 103)
                .addParam("source", source)
                .addParam("stIdx", stIdx)
                .addParam("cnt", cnt)
                .addParam("type", type);
        return execute(request, baseUrl + "/avContent", cookieString);
    }

    public static SonyJsonRpcResponse setWolMode(
            String baseUrl, boolean enabled, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("setWolMode", 55)
                .addParam("enabled", enabled);
        return execute(request, baseUrl + "/system", cookieString);
    }

    public static SonyJsonRpcResponse getWolMode(
            String baseUrl, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getWolMode", 50);
        return execute(request, baseUrl + "/system", cookieString);
    }

    public static SonyJsonRpcResponse setPowerStatus(
            String baseUrl, boolean status, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("setPowerStatus", 55)
                .addParam("status", status);
        return execute(request, baseUrl + "/system", cookieString);
    }

    public static SonyJsonRpcResponse getPowerStatus(
            String baseUrl, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getPowerStatus", 50);
        return execute(request, baseUrl + "/system", cookieString);
    }

    public static SonyJsonRpcResponse setPowerSavingMode(
            String baseUrl, String mode, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("setPowerSavingMode", 52)
                .addParam("mode", mode);
        return execute(request, baseUrl + "/system", cookieString);
    }

    public static SonyJsonRpcResponse getPowerSavingMode(
            String baseUrl, String cookieString) {

        SonyJsonRpcRequest request = new SonyJsonRpcRequest("getPowerSavingMode", 51);
        return execute(request, baseUrl + "/system", cookieString);
    }

}

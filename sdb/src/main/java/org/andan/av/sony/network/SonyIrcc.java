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

public class SonyIrcc {
    private static final String XML_REQUEST_TEMPLATE =
                    "<?xml version=\"1.0\"?>\n" +
                    "<s:Envelope\n" +
                    "    xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                    "    <s:Body>\n" +
                    "        <u:X_SendIRCC xmlns:u=\"urn:schemas-sony-com:service:IRCC:1\">\n" +
                    "            <IRCCCode>${code}</IRCCCode>\n" +
                    "        </u:X_SendIRCC>\n" +
                    "    </s:Body>\n" +
                    "</s:Envelope>";
    private static final String SONY_IRCC_NAMESPACE = "urn:schemas-sony-com:service:IRCC:1";
    private static final String SONY_SOAPACTION_SEND_IRCC = SONY_IRCC_NAMESPACE + "#" + "X_SendIRCC";
    public static final int RESULT_UNKNOWN = -1;

    public static int sendIRCC(String code, String baseUrl, String cookieString) {
        int returnCode = RESULT_UNKNOWN;
        if (code != null) {
            HttpURLConnection connection = null;
            OutputStream os = null;
            String response = null;
            try {
                URL url = new URL(baseUrl + "/IRCC");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
                connection.setRequestProperty("SOAPAction", SONY_SOAPACTION_SEND_IRCC);
                connection.setRequestProperty("Cookie", cookieString);
                connection.setDoOutput(true);
                os = connection.getOutputStream();
                String request = XML_REQUEST_TEMPLATE.replace("${code}", code);
                os.write(request.getBytes());
                returnCode = connection.getResponseCode();
            } catch (IOException e) {
                try {
                    returnCode = connection.getResponseCode();
                } catch (IOException e2) {
                    returnCode = HttpURLConnection.HTTP_UNAVAILABLE;
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
                    returnCode = HttpURLConnection.HTTP_UNAVAILABLE;
                }
            }
        }
        return returnCode;
    }
}

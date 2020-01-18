package org.andan.av.sony;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.andan.av.sony.model.SonyPlayingContentInfo;
import org.andan.av.sony.model.SonyProgram;
import org.andan.av.sony.network.SonyIrcc;
import org.andan.av.sony.network.SonyJsonRpc;
import org.andan.av.sony.network.SonyJsonRpcResponse;
import org.andan.av.sony.network.WakeOnLan;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by andan on 5/14/17.
 */

public class SonyIPControl {

    private static final int PAGE_SIZE = 25;
    //private static final long MAX_TIME_UNTIL_COOKIE_EXPIRES_IN_MILLIS = 86400 * 1000; // 1 day
    //private static final long MAX_TIME_UNTIL_COOKIE_EXPIRES_IN_MILLIS = (14*86400-120)*1000;
    private static final long MAX_TIME_UNTIL_COOKIE_EXPIRES_IN_MILLIS = (14*86400-5*60)*1000;

    private String ip;
    private String cookie;
    private long cookieExprireTime;
    private String nickname;
    private String devicename;
    private String uuid = null;
    private String systemModel = "";
    private String systemName = "";
    private String systemProduct = "";
    private String systemMacAddr = "";
    private boolean systemWolMode = true;

    private SonyPlayingContentInfo playingContentInfo = null;

    private List<String> sourceList;
    private List<SonyProgram> programList;
    private LinkedHashMap<String, SonyProgram> programUriMap;

    private final Type SonyProgramListType = new TypeToken<ArrayList<SonyProgram>>() {
    }.getType();

    private final Type ChannelMapType = new TypeToken<LinkedHashMap<String, ChannelMapEntryValue>>() {
    }.getType();

    private final Type ChannelProgramMapType = new TypeToken<LinkedHashMap<String, String>>() {
    }.getType();

    private final Type CommandMapType = new TypeToken<LinkedHashMap<String, String>>() {
    }.getType();

    private final Type SourceListType = new TypeToken<ArrayList<String>>() {
    }.getType();


    private Map<String, String> channelProgramUriMap = null;

    private static final Gson gson = new Gson();

    private LinkedHashMap<String, String> codeMap;
    // this type is only used for old version
    private final Type CodeMapListType = new TypeToken<ArrayList<Map<String, String>>>() {
    }.getType();

    private final JsonObject remoteControllerInfo = null;

    public static Gson getGson() {
        return gson;
    }

    public SonyIPControl(String ip, String nickname, String devicename) {
        this.ip = ip;
        this.nickname = nickname;
        this.devicename = devicename;
        this.uuid = UUID.randomUUID().toString();
    }

    private SonyIPControl(String ip, String nickname, String devicename, String uuid) {
        this.ip = ip;
        this.nickname = nickname;
        this.devicename = devicename;
        this.uuid = uuid;
    }

    private SonyIPControl() {
    }

    public SonyIPControl(JsonObject controlJSON) {
        if (controlJSON != null) {
            //ToDo: Add source and program list
            if (controlJSON.has("ip")) this.ip = controlJSON.get("ip").getAsString();
            if (controlJSON.has("nickname"))
                this.nickname = controlJSON.get("nickname").getAsString();
            if (controlJSON.has("devicename"))
                this.devicename = controlJSON.get("devicename").getAsString();
            if (controlJSON.has("uuid")) this.uuid = controlJSON.get("uuid").getAsString();
            if (controlJSON.has("cookie")) this.cookie = controlJSON.get("cookie").getAsString();
            if (controlJSON.has("cookieExpire"))
                this.cookieExprireTime = controlJSON.get("cookieExpire").getAsLong();
            if (controlJSON.has("systemModel"))
                this.systemModel = controlJSON.get("systemModel").getAsString();
            if (controlJSON.has("systemProduct"))
                this.systemProduct = controlJSON.get("systemProduct").getAsString();
            if (controlJSON.has("systemName"))
                this.systemName = controlJSON.get("systemName").getAsString();
            if (controlJSON.has("systemMacAddr"))
                this.systemMacAddr = controlJSON.get("systemMacAddr").getAsString();
            if (controlJSON.has("systemWolMode"))
                this.systemWolMode = controlJSON.get("systemWolMode").getAsBoolean();
            // if any of the system variables is not in json, get these infomation though request

            if (controlJSON.has("commandList")) {
                try {
                    this.codeMap = gson.fromJson(controlJSON.get("commandList"), CommandMapType);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    // old json version
                    try {
                        List<Map<String, String>> codeMapOld = gson.fromJson(controlJSON.get("commandList"), CodeMapListType);
                        if (codeMapOld != null) {
                            codeMap = new LinkedHashMap<>();
                        }
                        for (Map<String, String> map : codeMapOld) {
                            for (Map.Entry<String, String> entry : map.entrySet()) {
                                codeMap.put(entry.getKey(), entry.getValue());
                            }
                        }
                    } catch (Exception ex2) {
                    }
                }
            }
            if (controlJSON.has("sourceList"))
                this.sourceList = gson.fromJson(controlJSON.get("sourceList"), SourceListType);
            if (controlJSON.has("programList")) {
                this.programList = gson.fromJson(controlJSON.get("programList"), SonyProgramListType);
                createProgramUriMap();
            }
            if (controlJSON.has("channelMap")) {
                Map<String, ChannelMapEntryValue> channelMap = gson.fromJson(controlJSON.get("channelMap"), ChannelMapType);
                // create new map with only uri from old map using complex type
                channelProgramUriMap = new LinkedHashMap<>();
                for (Map.Entry<String, ChannelMapEntryValue> channelMapEntry : channelMap.entrySet()) {
                    int programId = channelMapEntry.getValue().programId;
                    if (programId >= 0 && programId < programList.size()) {
                        channelProgramUriMap.put(channelMapEntry.getKey(), programList.get(programId).uri);
                    }
                }
                channelMap = null;
            } else if (controlJSON.has("channelProgramMap")) {
                this.channelProgramUriMap = gson.fromJson(controlJSON.get("channelProgramMap"), ChannelProgramMapType);
            }

            // convert into new format where
        }
    }

    public SonyIPControl(String controlJSONString) {
        this(gson.fromJson(controlJSONString, JsonObject.class));
    }

    private SonyIPControl(String ip, String nickname, String devicename, String uuid, String cookie,
                          long cookieExprireTime, LinkedHashMap<String, String> codeMap) {
        this.ip = ip;
        this.nickname = nickname;
        this.devicename = devicename;
        this.uuid = uuid;
        this.cookie = cookie;
        this.cookieExprireTime = cookieExprireTime;
        this.codeMap = codeMap;
    }


    public static SonyIPControl createSample(String ip, String nickname, String devicename) {
        try (InputStreamReader fr = new InputStreamReader(SonyIPControl.class.getResourceAsStream("/SonyIPControl_sample.json"))) {
            JsonObject jsonObject = gson.fromJson(fr, JsonObject.class);
            SonyIPControl sonyIPControlSample = new SonyIPControl(jsonObject);
            sonyIPControlSample.setIp(ip);
            sonyIPControlSample.setNickname(nickname);
            sonyIPControlSample.setDevicename(devicename);
            return sonyIPControlSample;
        } catch (Exception e) {
            return null;
        }
    }

    private String getBaseUrl()
    {
        return "http://" + ip + "/sony";
    }

    public int sendIRCCByName(String name) {
        if (codeMap != null) {
            return sendIRCC(codeMap.get(name));
        }
        return -1;
    }

    public int sendIRCC(String code) {
        return SonyIrcc.sendIRCC(code, getBaseUrl(), cookie);
    }

    public SonyJsonRpcResponse registerRemoteControl(String challenge) {
        if (uuid == null || uuid.length() == 0) {
            uuid = UUID.randomUUID().toString();
        }
        SonyJsonRpcResponse response = SonyJsonRpc.actRegister(getBaseUrl(),
                nickname + ":" + uuid,
                nickname + " (" + devicename + ")",
                cookie,
                challenge == null || challenge.isEmpty() ? null : ":" + challenge
                );
        setCookieFromResponse(response);
        if(response.getResult() != null) {
            getSystemInformation();
            setWolMode(true);
            getWolMode();
        }
        return response;
    }


    public boolean checkAndRenewCookie() {
        if (cookie != null &&
                (cookieExprireTime - System.currentTimeMillis() < MAX_TIME_UNTIL_COOKIE_EXPIRES_IN_MILLIS)) {
            // reauthenticate
            System.out.println("Cookie expired:" +cookie);
            SonyJsonRpcResponse response = SonyJsonRpc.actRegister(getBaseUrl(),
                    nickname + ":" + uuid,
                    nickname + " (" + devicename + ")",
                    cookie, null);
            setCookieFromResponse(response);
            System.out.println("New cookie:" +cookie + " " + response.getResponseCode() +
                    " " + response.getSetCookie());
            return (response.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        return false;
    }

    private void setCookieFromResponse(SonyJsonRpcResponse response) {
        // get auth cookie from repsonse
        if (response.getSetCookie() != null) {
            Pattern pattern = Pattern.compile("auth=([A-Za-z0-9]+)");
            Matcher matcher = pattern.matcher(response.getSetCookie());
            if (matcher.find()) {
                cookie = "auth=" + matcher.group(1);
                pattern = Pattern.compile("max-age=([0-9]+)");
                matcher = pattern.matcher(response.getSetCookie());
                if (matcher.find()) {
                    cookieExprireTime = System.currentTimeMillis() + 1000 * Long.parseLong(matcher.group(1));
                }
            }
        }
    }

    public void setCodeMapFromRemoteControllerInfo() {
        SonyJsonRpcResponse response = SonyJsonRpc.getRemoteControllerInfo(getBaseUrl(), cookie);
        if (response.getResult() != null) {
            if (codeMap != null) {
                codeMap.clear();
            }
            // coe map is in second item of result array
            JsonArray jsonCodeMap = response.getResult().getAsJsonArray().get(1).getAsJsonArray();
            codeMap = new LinkedHashMap<>();
            for (Object obj : jsonCodeMap) {
                JsonObject e = (JsonObject) obj;
                codeMap.put(e.get("name").getAsString(), e.get("value").getAsString());
            }
        }
    }

    private void setSourceListFromTV() {
        SonyJsonRpcResponse response = SonyJsonRpc.getSourceList(getBaseUrl(), "tv", cookie);
        if (response.getResult() != null) {
            JsonArray jsonSourceList = response.getResult().getAsJsonArray().get(0).getAsJsonArray();
            sourceList = new ArrayList<>();
            for (int i = 0; i < jsonSourceList.size(); i++) {
                JsonObject sourceItem = jsonSourceList.get(i).getAsJsonObject();
                String source = sourceItem.get("source").getAsString();
                if (source.equals("tv:dvbs")) {
                    sourceList.add(source + "#general");
                    sourceList.add(source + "#preferred");
                } else {
                    sourceList.add(source);
                }
            }
        }
    }

    public SonyJsonRpcResponse setPlayContent(SonyProgram sonyProgram) {
        return setPlayContent(sonyProgram.uri);
    }

    public SonyJsonRpcResponse setPlayContent(String uri) {
        return SonyJsonRpc.setPlayContent(getBaseUrl(), uri, cookie);
    }

    public SonyPlayingContentInfo getPlayingContentInfo() {
        SonyJsonRpcResponse response = SonyJsonRpc.getPlayingContentInfo(getBaseUrl(), cookie);
        if (response.getResult() != null) {
            JsonObject resultItem = response.getResult().getAsJsonArray().get(0).getAsJsonObject();
            return new SonyPlayingContentInfo(
                    resultItem.has("source") ? resultItem.get("source").getAsString() : "",
                    resultItem.has("dispNum") ? resultItem.get("dispNum").getAsString() : "",
                    resultItem.has("programMediaType") ? resultItem.get("programMediaType").getAsString() : "",
                    resultItem.has("title") ? resultItem.get("title").getAsString() : "",
                    resultItem.has("uri") ? resultItem.get("uri").getAsString() : "",
                    resultItem.has("startDateTime") ? resultItem.get("startDateTime").getAsString() : "",
                    resultItem.has("durationSec") ? resultItem.get("durationSec").getAsInt() : 0,
                    resultItem.has("programTitle") ? resultItem.get("programTitle").getAsString() : ""
            );
        }
        return null;
    }


    private SonyJsonRpcResponse getSystemInformation() {
        SonyJsonRpcResponse response = SonyJsonRpc.getSystemInformation(getBaseUrl(), cookie);
        if (response.getResult() != null) {
            JsonObject resultItem = response.getResult().getAsJsonArray().get(0).getAsJsonObject();
            systemProduct = resultItem.has("product") ? resultItem.get("product").getAsString() : "";
            systemName = resultItem.has("name") ? resultItem.get("name").getAsString() : "";
            systemModel = resultItem.has("model") ? resultItem.get("model").getAsString() : "";
            systemMacAddr = resultItem.has("macAddr") ? resultItem.get("macAddr").getAsString() : "";
        }
        return response;
    }

    public SonyJsonRpcResponse setWolMode(boolean enabled) {
        SonyJsonRpcResponse response = SonyJsonRpc.setWolMode(getBaseUrl(), enabled, cookie);
        if (response.getResult() != null) {
            systemWolMode = enabled;
        }
        return response;
    }

    private SonyJsonRpcResponse getWolMode() {
        SonyJsonRpcResponse response = SonyJsonRpc.getWolMode(getBaseUrl(), cookie);
        if (response.getResult() != null) {
            JsonObject resultItem = response.getResult().getAsJsonArray().get(0).getAsJsonObject();
            systemWolMode = resultItem.get("enabled").getAsBoolean();
        }
        return response;
    }

    public int wakeOnLan() {
        return WakeOnLan.wakeOnLan(ip, systemMacAddr);
    }

    public SonyJsonRpcResponse setPowerSavingMode(String mode) {
        return SonyJsonRpc.setPowerSavingMode(getBaseUrl(), mode, cookie);
    }


    private List<SonyProgram> getProgramListForSources(List<String> sList) {
        List<SonyProgram> pList = new ArrayList<>();
        if (sList != null) {
            for (String sonySource : sList) {
                // get programs in pages
                SonyJsonRpcResponse response;
                int stidx = 0;
                do {
                    //resultCode = getTvContentList(103, sonySource, stidx, PAGE_SIZE, "1.0", sonyProgramList);
                    response = getTvContentList(sonySource, stidx, PAGE_SIZE, pList);
                    stidx += PAGE_SIZE;
                } while (response.getResult()!=null && response.getResult().getAsJsonArray().get(0).getAsJsonArray().size()>0);
            }
        }
        return pList;
    }

    public List<SonyProgram> getProgramListFromTV() {
        if (sourceList == null) {
            setSourceListFromTV();
        }
        if (sourceList != null) {
            programList = getProgramListForSources(sourceList);
            createProgramUriMap();
        }
        return programList;
    }

    public List<SonyProgram> getProgramListFromTV(List<String> sList) {
        if (sList != null) {
            programList = getProgramListForSources(sList);
            createProgramUriMap();
        }
        return programList;
    }

    public void setProgramListFromTV() {
        if (sourceList == null) {
            setSourceListFromTV();
        }
        if (sourceList != null) {
            programList = getProgramListForSources(sourceList);
            createProgramUriMap();
        }
    }

    private void createProgramUriMap() {
        programUriMap = new LinkedHashMap<>();
        if (programList != null) {
            for (SonyProgram program : programList) {
                programUriMap.put(program.uri, program);
            }
        }
    }

    public Map<String, SonyProgram> getProgramUriMap() {
        return programUriMap;
    }

    private SonyJsonRpcResponse getTvContentList(String sourceType, int stIdx, int cnt, List<SonyProgram> pList) {

        String[] sourceSplit = sourceType.split("#");
        String source = sourceSplit[0];
        String type = "";
        if (sourceSplit.length > 1) type = sourceSplit[1];
        SonyJsonRpcResponse response = SonyJsonRpc.getContentList(getBaseUrl(),
                source, stIdx, cnt, type, cookie);
        if (response.getResult() != null) {
            JsonArray jsonContentList = response.getResult().getAsJsonArray().get(0).getAsJsonArray();
            for (int i = 0; i < jsonContentList.size(); i++) {
                JsonObject programItem = jsonContentList.get(i).getAsJsonObject();
                String mediaType = programItem.get("programMediaType").getAsString();
                String title = programItem.get("title").getAsString();

                // ignore non tv programs and empty titles
                if (mediaType.equalsIgnoreCase("tv") && !title.equals(".") && !title.isEmpty() && !title.contains("TEST")) {
                    pList.add(new SonyProgram(
                            sourceType,
                            programItem.get("dispNum").getAsString(),
                            programItem.get("index").getAsInt(),
                            mediaType,
                            title,
                            programItem.get("uri").getAsString()));
                }
            }
        }
        return response;
    }

    //ToDo: Change to use gson standard serde
    public JsonObject toJSON() {
        JsonObject controlJSON = new JsonObject();
        //ToDo: Add source and program list
        try {
            controlJSON.addProperty("ip", this.getIp());
            controlJSON.addProperty("nickname", this.getNickname());
            controlJSON.addProperty("devicename", this.getDevicename());
            controlJSON.addProperty("uuid", this.getUuid());
            controlJSON.addProperty("cookie", this.getCookie());
            controlJSON.addProperty("cookieExpire", this.getCookieExprireTime());
            controlJSON.addProperty("systemModel", this.getSystemModel());
            controlJSON.addProperty("systemProduct", this.getSystemProduct());
            controlJSON.addProperty("systemName", this.getSystemName());
            controlJSON.addProperty("systemMacAddr", this.getSystemMacAddr());
            controlJSON.addProperty("systemWolMode", this.getSystemWolMode());

            controlJSON.add("commandList", gson.toJsonTree(this.codeMap));
            controlJSON.add("sourceList", gson.toJsonTree(this.sourceList));
            controlJSON.add("programList", gson.toJsonTree(this.programList));
            //controlJSON.add("channelMap", gson.toJsonTree(this.channelMap));
            controlJSON.add("channelProgramMap", gson.toJsonTree(this.channelProgramUriMap));

        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        return controlJSON;
    }

    @Override
    public String toString() {
        return this.nickname + " (" + this.devicename + ")";
    }


    public ArrayList<String> getCodeList() {
        if (codeMap != null) {
            return new ArrayList<>(codeMap.keySet());
        } else return new ArrayList<>();
    }


    public LinkedHashMap<String, String> getCodeMap() {
        return codeMap;
    }

    public void setCodeMap(LinkedHashMap<String, String> codeMap) {
        this.codeMap = codeMap;
    }

    public String getIp() {
        return ip;
    }

    private void setIp(String ip) {
        this.ip = ip;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public long getCookieExprireTime() {
        return cookieExprireTime;
    }

    public void setCookieExprireTime(long cookieExprireTime) {
        this.cookieExprireTime = cookieExprireTime;
    }

    public String getNickname() {
        return nickname;
    }

    private void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDevicename() {
        return devicename;
    }

    private void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private String getSystemModel() {
        return systemModel;
    }

    public void setSystemModel(String systemModel) {
        this.systemModel = systemModel;
    }

    private String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    private String getSystemProduct() {
        return systemProduct;
    }

    public void setSystemProduct(String systemProduct) {
        this.systemProduct = systemProduct;
    }

    public String getSystemMacAddr() {
        return systemMacAddr;
    }

    public void setSystemMacAddr(String systemMacAddr) {
        this.systemMacAddr = systemMacAddr;
    }

    public boolean getSystemWolMode() {
        return systemWolMode;
    }

    public void setSystemWolMode(boolean systemWolMode) {
        this.systemWolMode = systemWolMode;
    }

    public String getSystemProductInformation() {
        return (systemProduct != null && !systemProduct.isEmpty() ? systemProduct + " " : "")
                + (systemName != null && !systemName.isEmpty() ? systemName + " " : "")
                + (systemModel != null && !systemModel.isEmpty() ? systemModel + " " : "");

    }

    public JsonObject getRemoteControllerInfo() {
        return remoteControllerInfo;
    }

    public List<String> getSourceList() {
        return sourceList;
    }

    public List<SonyProgram> getProgramList() {
        return programList;
    }

    public void setSourceList(List<String> sourceList) {
        this.sourceList = sourceList;
    }

    public void setProgramList(List<SonyProgram> programList) {
        this.programList = programList;
        createProgramUriMap();
    }

    public void setChannelProgramUriMap(Map<String, String> channelProgramUriMap) {
        this.channelProgramUriMap = channelProgramUriMap;
    }

    public Map<String, String> getChannelProgramUriMap() {
        return channelProgramUriMap;
    }

    private class ChannelMapEntryValue {
        int code;
        int programId;

        public ChannelMapEntryValue(int code, int programId) {
            this.code = code;
            this.programId = programId;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public int getProgramId() {
            return programId;
        }

        public void setProgramId(int programId) {
            this.programId = programId;
        }
    }

}

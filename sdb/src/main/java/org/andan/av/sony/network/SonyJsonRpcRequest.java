package org.andan.av.sony.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SonyJsonRpcRequest {
    /*
    {
        "method": "getContentList",
            "id": 103,
            "params": [
        {
            "source": "tv:dvbs",
                "stIdx": 400,
                "cnt": 100,
                "type": "general"
        }
	        ],
        "version": "1.0"
    }
    {"method":"getContentList","id":103,"version":"1.0","params":[{"source":"tv:dvbs","stIdx":0,"cnt":25,"type":"general"}]}
    {"method":"getContentList","id":103,"params":[{"stIdx":0,"cnt":25,"source":"tv:dvbs","type":"general"}],"version":"1.0"}
    {"method":"getContentList","id":103,"params":[{"source":"tv:dvbs","stIdx":0,"cnt":25,"type":"general"}],"version":"1.0"}
    {"method":"actRegister","params":[{"clientid":"TVSideView:4e6b4a7a-aa52-416e-bfad-6aac6f560f9d","nickname":"Nexus 5 (TV SideView)","level":"private"},[{"value":"yes","function":"WOL"}]],"id":8,"version":"1.0"}'    */
    private String method;
    private int id;
    private List<Object> params;
    private String version;

    public SonyJsonRpcRequest(String method, int id, String version) {
        this.method = method;
        this.id = id;
        this.version = version;
        params = new ArrayList<>();
    }

    public SonyJsonRpcRequest(String method, int id) {
        this.method = method;
        this.id = id;
        this.version = "1.0";
        params = new ArrayList<>();
    }

    public SonyJsonRpcRequest addParam(String key, Object value) {
        if(params.size()==0) {
            params.add(new HashMap<String, Object>());
        }
        ((HashMap<String, Object>) params.get(0)).put(key, value);
        return this;
    }

    public SonyJsonRpcRequest addParamListItem() {
        if (params == null) {
            params = new ArrayList<>();
        }
        List<HashMap<String, Object>> addedParamList = new ArrayList<>();
        HashMap<String, Object> addedHashMap = new HashMap<String, Object>();
        addedParamList.add(addedHashMap);
        params.add(addedParamList);
        return this;
    }

    public SonyJsonRpcRequest addParamToNewList(String key, Object value) {
        if(params.get(params.size()-1) instanceof List) {
            ((HashMap<String, Object>) ((List) params.get(params.size()-1)).get(0)).put(key, value);
        }
        return this;
    }

    public String getMethod() {
        return method;
    }

    public int getId() {
        return id;
    }

    public List<Object> getParams() {
        return params;
    }

    public String getVersion() {
        return version;
    }
}

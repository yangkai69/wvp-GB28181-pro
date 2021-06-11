package com.genersoft.iot.vmp.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.genersoft.iot.vmp.storager.IVideoManagerStorager;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

@Service
public class HttpUtil {

    @Autowired
    private IVideoManagerStorager storager;

    public JSONObject sendToWebsocket() {

        List<Integer> list = storager.queryOnline();

        int onLineNum = 0;
        for (Integer i : list) {
            if (i == 1) {
                onLineNum++;
            }
        }

        JSONObject myJsonObject = new JSONObject();
        myJsonObject.put("allNum", list.size());
        myJsonObject.put("onLineNum", onLineNum);
        myJsonObject.put("offLineNum", list.size() - onLineNum);


        OkHttpClient client = new OkHttpClient();
        JSONObject responseJSON = null;
        String url = "http://192.168.1.44:8081/sllmanager/wvp/websocket/updateChannelNumAction";

        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        final RequestBody requestBody = RequestBody.create(myJsonObject.toJSONString(), mediaType);
        Request request = new Request.Builder()
                .post(requestBody)
                .addHeader("token","111")
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseStr = response.body().string();
            }
        } catch (ConnectException e) {
        } catch (IOException e) {
        }
        return responseJSON;
    }

    public void sendRestart() {

        OkHttpClient client = new OkHttpClient();
        String url = "http://192.168.1.44:8081/sllmanager/wvp/websocket/updateWvpStartAction";

        Request request = new Request.Builder()
                .get()
                .addHeader("token","111")
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseStr = response.body().string();
            }
        } catch (ConnectException e) {
        } catch (IOException e) {
        }
    }


}

package com.genersoft.iot.vmp.media.zlm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.genersoft.iot.vmp.common.StreamInfo;
import com.genersoft.iot.vmp.conf.MediaConfig;
import com.genersoft.iot.vmp.storager.IRedisCatchStorage;
import okhttp3.*;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ZLMRESTfulUtils {

    private final static Logger logger = LoggerFactory.getLogger(ZLMRESTfulUtils.class);

    @Autowired
    private MediaConfig mediaConfig;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    public interface RequestCallback{
        void run(JSONObject response);
    }

    public JSONObject sendPost(String mediaServerIp, String api, Map<String, Object> param, RequestCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("http://%s:%s/index/api/%s",  mediaServerIp, mediaConfig.getHttpPort(), api);
        JSONObject responseJSON = null;
        logger.debug(url);

        FormBody.Builder builder = new FormBody.Builder();
        builder.add("secret",mediaConfig.getSecret());
        if (param != null && param.keySet().size() > 0) {
            for (String key : param.keySet()){
                if (param.get(key) != null) {
                    builder.add(key, param.get(key).toString());
                }
            }
        }

        FormBody body = builder.build();

        Request request = new Request.Builder()
                .post(body)
                .url(url)
                .build();
            if (callback == null) {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        if (responseStr != null) {
                            responseJSON = JSON.parseObject(responseStr);
                        }
                    }
                } catch (ConnectException e) {
                    logger.error(String.format("连接ZLM失败: %s, %s", e.getCause().getMessage(), e.getMessage()));
                    logger.info("请检查media配置并确认ZLM已启动...");
                }catch (IOException e) {
                    logger.error(String.format("[ %s ]请求失败: %s", url, e.getMessage()));
                }
            }else {
                client.newCall(request).enqueue(new Callback(){

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response){
                        if (response.isSuccessful()) {
                            try {
                                String responseStr = Objects.requireNonNull(response.body()).string();
                                callback.run(JSON.parseObject(responseStr));
                            } catch (IOException e) {
                                logger.error(String.format("[ %s ]请求失败: %s", url, e.getMessage()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        logger.error(String.format("连接ZLM失败: %s, %s", e.getCause().getMessage(), e.getMessage()));
                        logger.info("请检查media配置并确认ZLM已启动...");
                    }
                });
            }



        return responseJSON;
    }

    public JSONObject getMediaList(String app, String stream, String schema, RequestCallback callback){
        Map<String, Object> param = new HashMap<>();
        if (app != null) param.put("app",app);
        if (stream != null) param.put("stream",stream);
        if (schema != null) param.put("schema",schema);
        param.put("vhost","__defaultVhost__");
        String[] s = stream.split("_");
        if (s.length != 4) {
            return null;
        }
        String channelId = s[3];
        StreamInfo streamInfo = redisCatchStorage.queryPlayByStreamId(channelId,stream);

        return sendPost(streamInfo.getMediaServerIp(),"getMediaList",param, callback);
    }

    public JSONObject getMediaList(String app, String stream){
        return getMediaList(app, stream,null,  null);
    }

    public JSONObject getMediaList(String mediaServerIp,RequestCallback callback){
        return sendPost(mediaServerIp,"getMediaList",null, callback);
    }

    public JSONObject getMediaInfo(String app, String schema, String stream){
        Map<String, Object> param = new HashMap<>();
        param.put("app",app);
        param.put("schema",schema);
        param.put("stream",stream);
        param.put("vhost","__defaultVhost__");
        String mediaServerIp = getIpByStream(stream);
        return sendPost(mediaServerIp,"getMediaInfo",param, null);
    }

    public JSONObject getRtpInfo(String stream_id){
        Map<String, Object> param = new HashMap<>();
        param.put("stream_id",stream_id);
        String mediaServerIp = getIpByStream(stream_id);
        return sendPost(mediaServerIp,"getRtpInfo",param, null);
    }

    public JSONObject addFFmpegSource(String mediaServerIp, String src_url, String dst_url, String timeout_ms){
        logger.info(src_url);
        logger.info(dst_url);
        Map<String, Object> param = new HashMap<>();
        param.put("src_url", src_url);
        param.put("dst_url", dst_url);
        param.put("timeout_ms", timeout_ms);
        return sendPost(mediaServerIp,"addFFmpegSource",param, null);
    }

    public JSONObject delFFmpegSource(String mediaServerIp, String key){
        Map<String, Object> param = new HashMap<>();
        param.put("key", key);
        return sendPost(mediaServerIp, "delFFmpegSource",param, null);
    }

    public JSONObject getMediaServerConfig(String mediaServerIp){
        return sendPost(mediaServerIp,"getServerConfig",null, null);
    }

    public JSONObject setServerConfig(String mediaServerIp, Map<String, Object> param){
        return sendPost(mediaServerIp,"setServerConfig",param, null);
    }

    public JSONObject openRtpServer(String mediaServerIp, Map<String, Object> param){
        return sendPost(mediaServerIp,"openRtpServer",param, null);
    }

    public JSONObject closeRtpServer(String mediaServerIp, Map<String, Object> param) {
        return sendPost(mediaServerIp,"closeRtpServer",param, null);
    }

    public JSONObject listRtpServer(String mediaServerIp) {
        return sendPost(mediaServerIp,"listRtpServer",null, null);
    }

    public JSONObject startSendRtp(String mediaServerIp, Map<String, Object> param) {
        return sendPost(mediaServerIp,"startSendRtp",param, null);
    }

    public JSONObject stopSendRtp(String mediaServerIp, Map<String, Object> param) {
        return sendPost(mediaServerIp,"stopSendRtp",param, null);
    }

    public JSONObject addStreamProxy(String app, String stream, String url, boolean enable_hls, boolean enable_mp4, String rtp_type) {
        Map<String, Object> param = new HashMap<>();
        param.put("vhost", "__defaultVhost__");
        param.put("app", app);
        param.put("stream", stream);
        param.put("url", url);
        param.put("enable_hls", enable_hls?1:0);
        param.put("enable_mp4", enable_mp4?1:0);
        param.put("rtp_type", rtp_type);
        return sendPost(getIpByStream(stream),"addStreamProxy",param, null);
    }

    public JSONObject closeStreams(String app, String stream) {
        Map<String, Object> param = new HashMap<>();
        param.put("vhost", "__defaultVhost__");
        param.put("app", app);
        param.put("stream", stream);
        param.put("force", 1);
        return sendPost(getIpByStream(stream),"close_streams",param, null);
    }

//    public JSONObject getAllSession() {
//        return sendPost("getAllSession",null, null);
//    }
//
//    public void kickSessions(String localPortSStr) {
//        Map<String, Object> param = new HashMap<>();
//        param.put("local_port", localPortSStr);
//        sendPost("kick_sessions",param, null);
//    }

    private String getIpByStream(String streamId){
        String[] s = streamId.split("_");
        if (s.length != 4) {
            return "";
        }
        String channelId = s[3];
        StreamInfo streamInfo = redisCatchStorage.queryPlayByStreamId(channelId,streamId);
        String mediaServerIp = streamInfo.getMediaServerIp();
        return mediaServerIp;
    }

}

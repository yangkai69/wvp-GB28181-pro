package com.genersoft.iot.vmp.media.zlm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.genersoft.iot.vmp.conf.MediaConfig;
import com.genersoft.iot.vmp.conf.SipConfig;
import com.genersoft.iot.vmp.media.zlm.dto.StreamProxyItem;
import com.genersoft.iot.vmp.storager.IRedisCatchStorage;
//import com.genersoft.iot.vmp.storager.IVideoManagerStorager;
import com.genersoft.iot.vmp.storager.IVideoManagerStorager;
import com.genersoft.iot.vmp.service.IStreamProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(value=1)
public class ZLMRunner implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(ZLMRunner.class);

     @Autowired
     private IVideoManagerStorager storager;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private MediaConfig mediaConfig;

    @Autowired
    private SipConfig sipConfig;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    private boolean startGetMedia = false;

    @Autowired
    private ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    private ZLMMediaListManager zlmMediaListManager;

    @Autowired
    private ZLMHttpHookSubscribe hookSubscribe;

    @Autowired
    private ZLMServerManger zlmServerManger;

    @Autowired
    private IStreamProxyService streamProxyService;

    @Override
    public void run(String... strings) throws Exception {

        String[] mediaIpArr = mediaConfig.getMediaIpArr();

        if(mediaIpArr == null || mediaIpArr.length == 0){
            String mediaIp = mediaConfig.getIp();
            mediaIpArr = mediaIp.split(",");
            mediaConfig.setMediaIpArr(mediaIpArr);
        }

        for(String mediaIp : mediaIpArr){
            if(StringUtils.isEmpty(mediaIp)){
                continue;
            }
            // 订阅 zlm启动事件
            hookSubscribe.addSubscribe(ZLMHttpHookSubscribe.HookType.on_server_started,null,(response)->{
                ZLMServerConfig ZLMServerConfig = JSONObject.toJavaObject(response, ZLMServerConfig.class);
                zLmRunning(mediaIp,ZLMServerConfig);
            });
            // 获取zlm信息
            logger.info("等待zlm接入...");
            startGetMedia = true;
            ZLMServerConfig ZLMServerConfig = getMediaServerConfig(mediaIp);

            if (ZLMServerConfig != null) {
                zLmRunning(mediaIp,ZLMServerConfig);
            }
        }
    }

    public ZLMServerConfig getMediaServerConfig(String mediaIp) {
        if (!startGetMedia) return null;
        JSONObject responseJSON = zlmresTfulUtils.getMediaServerConfig(mediaIp);
        ZLMServerConfig ZLMServerConfig = null;
        if (responseJSON != null) {
            JSONArray data = responseJSON.getJSONArray("data");
            if (data != null && data.size() > 0) {
                ZLMServerConfig = JSON.parseObject(JSON.toJSONString(data.get(0)), ZLMServerConfig.class);

            }
        } else {
            logger.error("getMediaServerConfig失败, 1s后重试");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ZLMServerConfig = getMediaServerConfig(mediaIp);
        }
        return ZLMServerConfig;
    }

    private void saveZLMConfig(String mediaIp) {
        logger.info("设置zlm...");
        if (StringUtils.isEmpty(mediaConfig.getHookIp())) mediaConfig.setHookIp(sipConfig.getSipIp());
        String protocol = sslEnabled ? "https" : "http";
        String hookPrex = String.format("%s://%s:%s/index/hook", protocol, mediaConfig.getHookIp(), serverPort);
        Map<String, Object> param = new HashMap<>();
        param.put("api.secret",mediaConfig.getSecret()); // -profile:v Baseline
        param.put("ffmpeg.cmd","%s -fflags nobuffer -rtsp_transport tcp -i %s -c:a aac -strict -2 -ar 44100 -ab 48k -c:v libx264  -f flv %s");
        param.put("hook.enable","1");
        param.put("hook.on_flow_report","");
        param.put("hook.on_play",String.format("%s/on_play", hookPrex));
        param.put("hook.on_http_access","");
        param.put("hook.on_publish",String.format("%s/on_publish", hookPrex));
        param.put("hook.on_record_mp4","");
        param.put("hook.on_record_ts","");
        param.put("hook.on_rtsp_auth","");
        param.put("hook.on_rtsp_realm","");
        param.put("hook.on_server_started",String.format("%s/on_server_started", hookPrex));
        param.put("hook.on_shell_login",String.format("%s/on_shell_login", hookPrex));
        param.put("hook.on_stream_changed",String.format("%s/on_stream_changed", hookPrex));
        param.put("hook.on_stream_none_reader",String.format("%s/on_stream_none_reader", hookPrex));
        param.put("hook.on_stream_not_found",String.format("%s/on_stream_not_found", hookPrex));
        param.put("hook.timeoutSec","20");
        param.put("general.streamNoneReaderDelayMS",mediaConfig.getStreamNoneReaderDelayMS());

        JSONObject responseJSON = zlmresTfulUtils.setServerConfig(mediaIp,param);

        if (responseJSON != null && responseJSON.getInteger("code") == 0) {
            logger.info("设置zlm成功");
        }else {
            logger.info("设置zlm失败: " + responseJSON.getString("msg"));
        }
    }

    /**
     * zlm 连接成功或者zlm重启后
     */
    private void zLmRunning(String mediaIp,ZLMServerConfig zlmServerConfig){
        logger.info( "[ id: " + zlmServerConfig.getGeneralMediaServerId() + "] zlm接入成功...");
        logger.info( "[ id: " + mediaIp + "] zlm接入成功...");
        // 关闭循环获取zlm配置
        startGetMedia = false;
        if (mediaConfig.getAutoConfig()) saveZLMConfig(mediaIp);
        zlmServerManger.updateServerCatch(zlmServerConfig);

        // 清空所有session
//        zlmMediaListManager.clearAllSessions();

        // 更新流列表
        zlmMediaListManager.updateMediaList(mediaIp);
        // 恢复流代理
        List<StreamProxyItem> streamProxyListForEnable = storager.getStreamProxyListForEnable(true);
        for (StreamProxyItem streamProxyDto : streamProxyListForEnable) {
            logger.info("恢复流代理，" + streamProxyDto.getApp() + "/" + streamProxyDto.getStream());
            JSONObject jsonObject = streamProxyService.addStreamProxyToZlm(streamProxyDto);
            if (jsonObject == null) {
                // 设置为未启用
                logger.info("恢复流代理失败，请检查流地址后重新启用" + streamProxyDto.getApp() + "/" + streamProxyDto.getStream());
                streamProxyService.stop(streamProxyDto.getApp(), streamProxyDto.getStream());
            }
        }
    }
}

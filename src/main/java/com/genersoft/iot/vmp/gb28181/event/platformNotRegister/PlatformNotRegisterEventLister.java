package com.genersoft.iot.vmp.gb28181.event.platformNotRegister;

import com.genersoft.iot.vmp.common.StreamInfo;
import com.genersoft.iot.vmp.gb28181.bean.ParentPlatform;
import com.genersoft.iot.vmp.gb28181.bean.SendRtpItem;
import com.genersoft.iot.vmp.gb28181.event.SipSubscribe;
import com.genersoft.iot.vmp.gb28181.transmit.cmd.impl.SIPCommanderFroPlatform;
import com.genersoft.iot.vmp.media.zlm.ZLMRTPServerFactory;
import com.genersoft.iot.vmp.storager.IRedisCatchStorage;
import com.genersoft.iot.vmp.storager.IVideoManagerStorager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Description: 平台未注册事件,来源有二:
 *               1、平台新添加
 *               2、平台心跳超时
 * @author: panll
 * @date: 2020年11月24日 10:00
 */
@Component
public class PlatformNotRegisterEventLister implements ApplicationListener<PlatformNotRegisterEvent> {

    private final static Logger logger = LoggerFactory.getLogger(PlatformNotRegisterEventLister.class);

    @Autowired
    private IVideoManagerStorager storager;
    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private SIPCommanderFroPlatform sipCommanderFroPlatform;

    @Autowired
    private ZLMRTPServerFactory zlmrtpServerFactory;

    // @Autowired
    // private RedisUtil redis;

    @Override
    public void onApplicationEvent(PlatformNotRegisterEvent event) {

        logger.info("平台未注册事件触发，平台国标ID：" + event.getPlatformGbID());

        ParentPlatform parentPlatform = storager.queryParentPlatByServerGBId(event.getPlatformGbID());
        if (parentPlatform == null) {
            logger.info("平台未注册事件触发，但平台已经删除!!! 平台国标ID：" + event.getPlatformGbID());
            return;
        }
        // 查询是否有推流， 如果有则都停止
        List<SendRtpItem> sendRtpItems = redisCatchStorage.querySendRTPServer(event.getPlatformGbID());
        logger.info("停止[ {} ]的所有推流size", sendRtpItems.size());
        if (sendRtpItems != null && sendRtpItems.size() > 0) {
            logger.info("停止[ {} ]的所有推流", event.getPlatformGbID());
            StringBuilder app = new StringBuilder();
            StringBuilder stream = new StringBuilder();
            for (int i = 0; i < sendRtpItems.size(); i++) {
                if (app.length() != 0) {
                    app.append(",");
                }
                app.append(sendRtpItems.get(i).getApp());
                if (stream.length() != 0) {
                    stream.append(",");
                }
                stream.append(sendRtpItems.get(i).getStreamId());
                redisCatchStorage.deleteSendRTPServer(event.getPlatformGbID(), sendRtpItems.get(i).getChannelId());
            }
            Map<String, Object> param = new HashMap<>();
            param.put("vhost","__defaultVhost__");
            param.put("app", app.toString());
            param.put("stream", stream.toString());

            String mediaServerIp = "";
            if(sendRtpItems.size() > 0){
                String streamId = sendRtpItems.get(0).getStreamId();
                String[] s = streamId.split("_");
                if (s.length != 4) {
                    return;
                }
                String channelId = s[3];
                StreamInfo streamInfo = redisCatchStorage.queryPlayByStreamId(channelId,streamId);
                mediaServerIp = streamInfo.getMediaServerIp();
            }

            zlmrtpServerFactory.stopSendRtpStream(mediaServerIp,param);

        }

        Timer timer = new Timer();
        SipSubscribe.Event okEvent = (responseEvent)->{
            timer.cancel();
        };
        logger.info("向平台注册，平台国标ID：" + event.getPlatformGbID());
        sipCommanderFroPlatform.register(parentPlatform, null, okEvent);
        // 设置注册失败则每隔15秒发起一次注册
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("再次向平台注册，平台国标ID：" + event.getPlatformGbID());
                sipCommanderFroPlatform.register(parentPlatform, null, okEvent);
            }
        }, 15000, 15000);//十五秒后再次发起注册
    }
}

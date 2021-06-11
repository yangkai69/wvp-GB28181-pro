package com.genersoft.iot.vmp.storager.impl;

import com.genersoft.iot.vmp.common.StreamInfo;
import com.genersoft.iot.vmp.common.VideoManagerConstants;
import com.genersoft.iot.vmp.media.zlm.ZLMServerConfig;
import com.genersoft.iot.vmp.gb28181.bean.*;
import com.genersoft.iot.vmp.storager.IRedisCatchStorage;
import com.genersoft.iot.vmp.storager.dao.DeviceChannelMapper;
import com.genersoft.iot.vmp.utils.HttpUtil;
import com.genersoft.iot.vmp.utils.redis.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * redis的key与主分支不同
 */
@SuppressWarnings("rawtypes")
@Component
public class RedisCatchStorageImpl implements IRedisCatchStorage {

    @Autowired
	private RedisUtil redis;

    @Autowired
    private DeviceChannelMapper deviceChannelMapper;


    /**
     * 开始播放时将流存入redis
     *
     * @return
     */
    @Override
    public boolean startPlay(StreamInfo stream) {
        String key = getKey(VideoManagerConstants.PLAYER_PREFIX,
                stream.getStreamId(),
                stream.getChannelId(),
                stream.getDeviceID()
        );
        return redis.set(key, stream);
    }

    public static String getKey(String prefix, String streamId, String channelId, String deviceId) {
        if (StringUtils.isBlank(streamId)) {
            streamId = "*";
        }
        if (StringUtils.isBlank(channelId)) {
            channelId = "*";
        }
        if (StringUtils.isBlank(deviceId)) {
            deviceId = "*";
        }
        return String.format("%S%s_%s_%s",
                prefix,
                streamId,
                channelId,
                deviceId);
    }


    /**
     * 停止播放时从redis删除
     *
     * @return
     */
    @Override
    public boolean stopPlay(StreamInfo streamInfo) {
        if (streamInfo == null) {
            return false;
        }
        String key = getKey(VideoManagerConstants.PLAYER_PREFIX,
                streamInfo.getStreamId(),
                streamInfo.getChannelId(),
                streamInfo.getDeviceID()
        );
        return redis.del(key);
    }

    /**
     * 查询播放列表
     * @return
     */
//    @Override
//    public StreamInfo queryPlay(StreamInfo streamInfo) {
//        return (StreamInfo)redis.get(String.format("%S_%s_%s_%s",
//                VideoManagerConstants.PLAYER_PREFIX,
//                streamInfo.getStreamId(),
//                streamInfo.getDeviceID(),
//                streamInfo.getChannelId()));
//    }

    @Override
    public StreamInfo queryPlayByStreamId(String channelId,String steamId) {
        String key = getKey(VideoManagerConstants.PLAYER_PREFIX,
                steamId,
                channelId,
                null
        );
        return scanOne(key);
    }

    @Override
    public StreamInfo queryPlayByChannel(String channelId) {
        String key = getKey(VideoManagerConstants.PLAYER_PREFIX,
                null,
                channelId,
                null
        );
        return scanOne(key);
    }

    @Override
    public StreamInfo queryPlaybackByStreamId(String channelId, String steamId) {
        String key = getKey(VideoManagerConstants.PLAY_BLACK_PREFIX,
                steamId,
                channelId,
                null
        );
        return scanOne(key);
    }

    @Override
    public StreamInfo queryPlaybackByChannel(String channelId) {

        String key = getKey(VideoManagerConstants.PLAY_BLACK_PREFIX,
                null,
                channelId,
                null
        );
        return scanOne(key);
    }

    @Override
    public List<StreamInfo> queryPlayBackByDeviceId(String deviceId) {
        String key = getKey(VideoManagerConstants.PLAY_BLACK_PREFIX,
                null,
                null,
                deviceId
        );
        List<Object> players = redis.scan(key);
        if (players.size() == 0) {
            return new ArrayList<>();
        }
        List<StreamInfo> streamInfos = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            String redisKey = (String) players.get(i);
            StreamInfo streamInfo = (StreamInfo) redis.get(redisKey);
            streamInfos.add(streamInfo);
        }
        return streamInfos;
    }

    private StreamInfo scanOne(String key) {
        List<Object> playLeys = redis.scan(key);
        if (playLeys == null || playLeys.size() == 0) {
            return null;
        }
        return (StreamInfo) redis.get(playLeys.get(0).toString());
    }

    @Override
    public StreamInfo queryPlayByDevice(String deviceId, String channelId) {
//		List<Object> playLeys = redis.keys(String.format("%S_*_%s_%s", VideoManagerConstants.PLAYER_PREFIX,
//        List<Object> playLeys = redis.scan(String.format("%S_*_%s_%s", VideoManagerConstants.PLAYER_PREFIX,
//                deviceId,
//                channelId));
//        if (playLeys == null || playLeys.size() == 0) return null;
//        return (StreamInfo)redis.get(playLeys.get(0).toString());
        String key = getKey(VideoManagerConstants.PLAYER_PREFIX,
                null,
                channelId,
                deviceId
        );
        return scanOne(key);
    }

    /**
     * 更新流媒体信息
     * @param ZLMServerConfig
     * @return
     */
    @Override
    public boolean updateMediaInfo(ZLMServerConfig ZLMServerConfig) {
        ZLMServerConfig.setUpdateTime(System.currentTimeMillis());
        return redis.set(VideoManagerConstants.MEDIA_SERVER_PREFIX, ZLMServerConfig);
    }

    /**
     * 获取流媒体信息
     * @return
     */
    @Override
    public ZLMServerConfig getMediaInfo() {
        return (ZLMServerConfig)redis.get(VideoManagerConstants.MEDIA_SERVER_PREFIX);
    }

    @Override
    public Map<String, StreamInfo> queryPlayByDeviceId(String deviceId) {
        Map<String, StreamInfo> streamInfos = new HashMap<>();
//		List<Object> playLeys = redis.keys(String.format("%S_*_%S_*", VideoManagerConstants.PLAYER_PREFIX, deviceId));
//        List<Object> players = redis.scan(String.format("%S_*_%S_*", VideoManagerConstants.PLAYER_PREFIX, deviceId));
//        if (players.size() == 0) return streamInfos;
//        for (int i = 0; i < players.size(); i++) {
//            String key = (String) players.get(i);
//            StreamInfo streamInfo = (StreamInfo)redis.get(key);
//            streamInfos.put(streamInfo.getDeviceID() + "_" + streamInfo.getChannelId(), streamInfo);
//        }
        return streamInfos;
    }

    @Override
    public boolean startPlayback(StreamInfo stream) {
        String key = getKey(VideoManagerConstants.PLAY_BLACK_PREFIX,
                stream.getStreamId(),
                stream.getChannelId(),
                stream.getDeviceID()
        );
        return redis.set(key, stream);
    }

    @Autowired
    HttpUtil httpUtil;

    @Override
    public boolean stopPlayback(StreamInfo streamInfo) {
        if (streamInfo == null) return false;
        DeviceChannel deviceChannel = deviceChannelMapper.queryChannel(streamInfo.getDeviceID(), streamInfo.getChannelId());
        if (deviceChannel != null) {
            deviceChannel.setStreamId(null);
            deviceChannel.setDeviceId(streamInfo.getDeviceID());
            deviceChannelMapper.update(deviceChannel);
            httpUtil.sendToWebsocket();
        }
        String key = getKey(VideoManagerConstants.PLAY_BLACK_PREFIX,
                streamInfo.getStreamId(),
                streamInfo.getChannelId(),
                streamInfo.getDeviceID()
        );

        return redis.del(key);
    }

    @Override
    public StreamInfo queryPlaybackByDevice(String deviceId, String channelId) {
        // String format = String.format("%S_*_%s_%s", VideoManagerConstants.PLAY_BLACK_PREFIX,
        //         deviceId,
        //         code);
//        List<Object> playLeys = redis.scan(String.format("%S_*_%s_%s", VideoManagerConstants.PLAY_BLACK_PREFIX,
//                deviceId,
//                code));
//        if (playLeys == null || playLeys.size() == 0) {
//            playLeys = redis.scan(String.format("%S_*_*_%s", VideoManagerConstants.PLAY_BLACK_PREFIX,
//                    deviceId));
//        }
//        if (playLeys == null || playLeys.size() == 0) return null;
//        return (StreamInfo)redis.get(playLeys.get(0).toString());
        String key = getKey(VideoManagerConstants.PLAY_BLACK_PREFIX,
                null,
                channelId,
                deviceId
        );
        return scanOne(key);
    }

    @Override
    public void updatePlatformCatchInfo(ParentPlatformCatch parentPlatformCatch) {
        String key = VideoManagerConstants.PLATFORM_CATCH_PREFIX + parentPlatformCatch.getId();
        redis.set(key, parentPlatformCatch);
    }

    @Override
    public void updatePlatformKeepalive(ParentPlatform parentPlatform) {
        String key = VideoManagerConstants.PLATFORM_KEEPLIVEKEY_PREFIX + parentPlatform.getServerGBId();
        redis.set(key, "", Integer.parseInt(parentPlatform.getKeepTimeout()));
    }

    @Override
    public void updatePlatformRegister(ParentPlatform parentPlatform) {
        String key = VideoManagerConstants.PLATFORM_REGISTER_PREFIX + parentPlatform.getServerGBId();
        redis.set(key, "", Integer.parseInt(parentPlatform.getExpires()));
    }

    @Override
    public ParentPlatformCatch queryPlatformCatchInfo(String platformGbId) {
        return (ParentPlatformCatch)redis.get(VideoManagerConstants.PLATFORM_CATCH_PREFIX + platformGbId);
    }

    @Override
    public void delPlatformCatchInfo(String platformGbId) {
        redis.del(VideoManagerConstants.PLATFORM_CATCH_PREFIX + platformGbId);
    }

    @Override
    public void delPlatformKeepalive(String platformGbId) {
        redis.del(VideoManagerConstants.PLATFORM_KEEPLIVEKEY_PREFIX + platformGbId);
    }

    @Override
    public void delPlatformRegister(String platformGbId) {
        redis.del(VideoManagerConstants.PLATFORM_REGISTER_PREFIX + platformGbId);
    }


    @Override
    public void updatePlatformRegisterInfo(String callId, String platformGbId) {
        String key = VideoManagerConstants.PLATFORM_REGISTER_INFO_PREFIX + callId;
        redis.set(key, platformGbId);
    }


    @Override
    public String queryPlatformRegisterInfo(String callId) {
        return (String)redis.get(VideoManagerConstants.PLATFORM_REGISTER_INFO_PREFIX + callId);
    }

    @Override
    public void delPlatformRegisterInfo(String callId) {
        redis.del(VideoManagerConstants.PLATFORM_REGISTER_INFO_PREFIX + callId);
    }

    @Override
    public void cleanPlatformRegisterInfos() {
        List regInfos = redis.scan(VideoManagerConstants.PLATFORM_REGISTER_INFO_PREFIX + "*");
        for (Object key : regInfos) {
            redis.del(key.toString());
        }
    }

    @Override
    public void updateSendRTPSever(SendRtpItem sendRtpItem) {
        String key = VideoManagerConstants.PLATFORM_SEND_RTP_INFO_PREFIX + sendRtpItem.getPlatformId() + "_" + sendRtpItem.getChannelId();
        redis.set(key, sendRtpItem);
    }

    @Override
    public SendRtpItem querySendRTPServer(String platformGbId, String channelId) {
        String key = VideoManagerConstants.PLATFORM_SEND_RTP_INFO_PREFIX + platformGbId + "_" + channelId;
        return (SendRtpItem)redis.get(key);
    }

    @Override
    public List<SendRtpItem> querySendRTPServer(String platformGbId) {
        String key = VideoManagerConstants.PLATFORM_SEND_RTP_INFO_PREFIX + platformGbId + "_*";
        List<Object> queryResult = redis.scan(key);
        List<SendRtpItem> result= new ArrayList<>();

        for (int i = 0; i < queryResult.size(); i++) {
            String keyItem = (String) queryResult.get(i);
            result.add((SendRtpItem)redis.get(keyItem));
        }

        return result;
    }

    /**
     * 删除RTP推送信息缓存
     * @param platformGbId
     * @param channelId
     */
    @Override
    public void deleteSendRTPServer(String platformGbId, String channelId) {
        String key = VideoManagerConstants.PLATFORM_SEND_RTP_INFO_PREFIX + platformGbId + "_" + channelId;
        redis.del(key);
    }

    /**
     * 查询某个通道是否存在上级点播（RTP推送）
     * @param channelId
     */
    @Override
    public boolean isChannelSendingRTP(String channelId) {
        String key = VideoManagerConstants.PLATFORM_SEND_RTP_INFO_PREFIX + "*_" + channelId;
        List<Object> RtpStreams = redis.scan(key);
        if (RtpStreams.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clearCatchByDeviceId(String deviceId) {
        List<Object> playLeys = redis.scan(String.format("%S_*_%s_*", VideoManagerConstants.PLAYER_PREFIX,
                deviceId));
        if (playLeys.size() > 0) {
            for (Object key : playLeys) {
                redis.del(key.toString());
            }
        }

        List<Object> playBackers = redis.scan(String.format("%S_*_%s_*", VideoManagerConstants.PLAY_BLACK_PREFIX,
                deviceId));
        if (playBackers.size() > 0) {
            for (Object key : playBackers) {
                redis.del(key.toString());
            }
        }
    }
}

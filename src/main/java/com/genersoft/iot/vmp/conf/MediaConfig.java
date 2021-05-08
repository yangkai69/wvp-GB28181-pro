package com.genersoft.iot.vmp.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration("mediaConfig")
public class MediaConfig {

    @Value("${media.ip}")
    private String ip;

    private String[] mediaIpArr;

    public String[] getMediaIpArr() {
        return mediaIpArr;
    }

    public void setMediaIpArr(String[] mediaIpArr) {
        this.mediaIpArr = mediaIpArr;
    }

    @Value("${media.wanIp}")
    private String wanIp;

    @Value("${media.hookIp}")
    private String hookIp;

    @Value("${media.httpPort}")
    private String httpPort;

    @Value("${media.httpSSlPort}")
    private String httpSSlPort;

    @Value("${media.rtmpPort}")
    private String rtmpPort;

    @Value("${media.rtmpSSlPort}")
    private String rtmpSSlPort;

    @Value("${media.rtpProxyPort}")
    private String rtpProxyPort;

    @Value("${media.rtspPort}")
    private String rtspPort;

    @Value("${media.rtspSSLPort}")
    private String rtspSSLPort;

    @Value("${media.autoConfig}")
    private boolean autoConfig;

    @Value("${media.secret}")
    private String secret;

    @Value("${media.streamNoneReaderDelayMS}")
    private String streamNoneReaderDelayMS;

    @Value("${media.rtp.enable}")
    private boolean rtpEnable;

    @Value("${media.rtp.portRange}")
    private String rtpPortRange;

    /**
     * 每一台ZLM都有一套独立的SSRC列表
     * 在ApplicationCheckRunner里对mediaServerSsrcMap进行初始化
     */
    private HashMap<String, SsrcConfig> mediaServerSsrcMap;

    public HashMap<String, SsrcConfig> getMediaServerSsrcMap() {
        return mediaServerSsrcMap;
    }

    public void setMediaServerSsrcMap(HashMap<String, SsrcConfig> mediaServerSsrcMap) {
        this.mediaServerSsrcMap = mediaServerSsrcMap;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getWanIp() {
        return wanIp;
    }

    public void setWanIp(String wanIp) {
        this.wanIp = wanIp;
    }

    public String getHookIp() {
        return hookIp;
    }

    public void setHookIp(String hookIp) {
        this.hookIp = hookIp;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isAutoConfig() {
        return autoConfig;
    }

    public boolean getAutoConfig() {
        return autoConfig;
    }

    public void setAutoConfig(boolean autoConfig) {
        this.autoConfig = autoConfig;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getStreamNoneReaderDelayMS() {
        return streamNoneReaderDelayMS;
    }

    public void setStreamNoneReaderDelayMS(String streamNoneReaderDelayMS) {
        this.streamNoneReaderDelayMS = streamNoneReaderDelayMS;
    }

    public boolean isRtpEnable() {
        return rtpEnable;
    }

    public void setRtpEnable(boolean rtpEnable) {
        this.rtpEnable = rtpEnable;
    }

    public String getRtpPortRange() {
        return rtpPortRange;
    }

    public void setRtpPortRange(String rtpPortRange) {
        this.rtpPortRange = rtpPortRange;
    }

    public String getHttpSSlPort() {
        return httpSSlPort;
    }

    public void setHttpSSlPort(String httpSSlPort) {
        this.httpSSlPort = httpSSlPort;
    }

    public String getRtmpPort() {
        return rtmpPort;
    }

    public void setRtmpPort(String rtmpPort) {
        this.rtmpPort = rtmpPort;
    }

    public String getRtmpSSlPort() {
        return rtmpSSlPort;
    }

    public void setRtmpSSlPort(String rtmpSSlPort) {
        this.rtmpSSlPort = rtmpSSlPort;
    }

    public String getRtpProxyPort() {
        return rtpProxyPort;
    }

    public void setRtpProxyPort(String rtpProxyPort) {
        this.rtpProxyPort = rtpProxyPort;
    }

    public String getRtspPort() {
        return rtspPort;
    }

    public void setRtspPort(String rtspPort) {
        this.rtspPort = rtspPort;
    }

    public String getRtspSSLPort() {
        return rtspSSLPort;
    }

    public void setRtspSSLPort(String rtspSSLPort) {
        this.rtspSSLPort = rtspSSLPort;
    }
}

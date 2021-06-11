package com.genersoft.iot.vmp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.genersoft.iot.vmp.utils.SpringBeanFactory;
import gov.nist.javax.sip.SipStackImpl;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.SipProvider;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/config")
public class ConfigController {

    @RequestMapping("/sipConfigListAction")
    public JSONArray sipConfigList() throws IOException {
        File file = new File("../application.yml");
//        File directory = new File("");
//        String filePath = directory.getAbsolutePath() + "/src/main/resources/application.yml";
        Map<String, Object> obj = null;

        //解析yml文件的关键类
        Yaml yml = null;
        try (FileInputStream in = new FileInputStream(file)) {

            yml = new Yaml();
            obj = (Map) yml.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("config", "spring_redis_host");
        jsonObject.put("config_name", "REDIS服务器Ip地址");
        jsonObject.put("config_value", ((Map) ((Map) ((Map) obj).get("spring")).get("redis")).get("host"));
        jsonArray.add(jsonObject);

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("config", "spring_redis_port");
        jsonObject2.put("config_name", "REDIS服务器端口");
        jsonObject2.put("config_value", ((Map) ((Map) ((Map) obj).get("spring")).get("redis")).get("port"));
        jsonArray.add(jsonObject2);

        JSONObject jsonObject3 = new JSONObject();
        jsonObject3.put("config", "spring_redis_database");
        jsonObject3.put("config_name", "REDIS数据库");
        jsonObject3.put("config_value", ((Map) ((Map) ((Map) obj).get("spring")).get("redis")).get("database"));
        jsonArray.add(jsonObject3);

        JSONObject jsonObject4 = new JSONObject();
        jsonObject4.put("config", "spring_redis_password");
        jsonObject4.put("config_name", "REDIS数据库密码");
        jsonObject4.put("config_value", ((Map) ((Map) ((Map) obj).get("spring")).get("redis")).get("password"));
        jsonArray.add(jsonObject4);

        JSONObject jsonObject5 = new JSONObject();
        jsonObject5.put("config", "server_port");
        jsonObject5.put("config_name", "服务器端口");
        jsonObject5.put("config_value", ((Map) ((Map) obj).get("server")).get("port"));
        jsonArray.add(jsonObject5);

//        JSONObject jsonObject6 = new JSONObject();
//        jsonObject6.put("config", "sip_ip");
//        jsonObject6.put("config_name", "SIP服务器IP地址");
//        jsonObject6.put("config_value", ((Map) ((Map) obj).get("sip")).get("ip"));
//        jsonArray.add(jsonObject6);

        JSONObject jsonObject7 = new JSONObject();
        jsonObject7.put("config", "sip_port");
        jsonObject7.put("config_name", "28181服务监听的端口");
        jsonObject7.put("config_value", ((Map) ((Map) obj).get("sip")).get("port"));
        jsonArray.add(jsonObject7);

        JSONObject jsonObject8 = new JSONObject();
        jsonObject8.put("config", "sip_domain");
        jsonObject8.put("config_name", "SIP_DOMAIN");
        jsonObject8.put("config_value", ((Map) ((Map) obj).get("sip")).get("domain"));
        jsonArray.add(jsonObject8);

        JSONObject jsonObject9 = new JSONObject();
        jsonObject9.put("config", "sip_id");
        jsonObject9.put("config_name", "SIP_ID");
        jsonObject9.put("config_value", ((Map) ((Map) obj).get("sip")).get("id"));
        jsonArray.add(jsonObject9);

        JSONObject jsonObject10 = new JSONObject();
        jsonObject10.put("config", "sip_password");
        jsonObject10.put("config_name", "SIP密码");
        jsonObject10.put("config_value", ((Map) ((Map) obj).get("sip")).get("password"));
        jsonArray.add(jsonObject10);

        JSONObject jsonObject11 = new JSONObject();
        jsonObject11.put("config", "media_ip");
        jsonObject11.put("config_name", "ZLM服务器地址");
        jsonObject11.put("config_value", ((Map) ((Map) obj).get("media")).get("ip"));
        jsonArray.add(jsonObject11);

        JSONObject jsonObject12 = new JSONObject();
        jsonObject12.put("config", "media_httpPort");
        jsonObject12.put("config_name", "zlm服务器的httpPort");
        jsonObject12.put("config_value", ((Map) ((Map) obj).get("media")).get("httpPort"));
        jsonArray.add(jsonObject12);

        JSONObject jsonObject13 = new JSONObject();
        jsonObject13.put("config", "media_autoconfig");
        jsonObject13.put("config_name", "是否自动配置ZLM");
        jsonObject13.put("config_value", ((Map) ((Map) obj).get("media")).get("autoConfig"));
        jsonArray.add(jsonObject13);

        JSONObject jsonObject14 = new JSONObject();
        jsonObject14.put("config", "media_streamNoneReaderDelayMS");
        jsonObject14.put("config_name", "无人观看多久自动关闭流");
        jsonObject14.put("config_value", ((Map) ((Map) obj).get("media")).get("streamNoneReaderDelayMS"));
        jsonArray.add(jsonObject14);


        return jsonArray;
    }


    @RequestMapping("/updateSipConfigListAction")
    public JSONObject updateSipConfigList(@RequestBody JSONArray jsonArray) throws Exception {

//        JSONArray jsonArray = new JSONArray(Collections.singletonList(jsonArrayString));
        if (jsonArray.size() == 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", "jsonArray为空");
            jsonObject.put("code", "-1");
            jsonObject.put("result", null);
            return jsonObject;
        }

        updateYml(jsonArray);

        Thread restartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    SipProvider up = (SipProvider) SpringBeanFactory.getBean("udpSipProvider");
                    SipStackImpl stack = (SipStackImpl) up.getSipStack();
                    stack.stop();
                    Iterator listener = stack.getListeningPoints();
                    while (listener.hasNext()) {
                        stack.deleteListeningPoint((ListeningPoint) listener.next());
                    }
                    Iterator providers = stack.getSipProviders();
                    while (providers.hasNext()) {
                        stack.deleteSipProvider((SipProvider) providers.next());
                    }
                    VManageBootstrap.restart();
                } catch (InterruptedException ignored) {
                } catch (ObjectInUseException e) {
                    e.printStackTrace();
                }
            }
        });

        restartThread.setDaemon(false);
        restartThread.start();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "ok");
        jsonObject.put("code", "200");
        jsonObject.put("result", null);

        return jsonObject;
    }


    private void updateYml(JSONArray jsonArray) throws Exception {
        File file = new File("../application.yml");
//        File directory = new File("");
//        String file = directory.getAbsolutePath() + "/src/main/resources/application.yml";
        Map<String, Object> obj = null;

        //解析yml文件的关键类
        Yaml yml = null;
        try (FileInputStream in = new FileInputStream(file)) {

            yml = new Yaml();
            obj = (Map) yml.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Object jsonObject : jsonArray) {
            String config = (String) ((Map) jsonObject).get("config");
            String value = ((Map) jsonObject).get("config_value").toString();

            String[] arrs = config.trim().split("_");

            Map<String, Object> map = new HashMap<>();
            if (arrs.length == 1) {
                map = (Map) obj;
            } else if (arrs.length == 2) {
                map = (Map) obj.get(arrs[0]);
            } else if (arrs.length == 3) {
                map = (Map) ((Map) obj.get(arrs[0])).get(arrs[1]);
            } else if (arrs.length == 4) {
                map = (Map) ((Map) ((Map) obj.get(arrs[0])).get(arrs[1])).get(arrs[2]);
            }
            map.put(arrs[arrs.length - 1], value);
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(yml.dumpAsMap(obj));
        } catch (Exception e) {
            throw new Exception();
        } finally {
            writer.close();
        }

    }

}

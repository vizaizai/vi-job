package com.github.vizaizai.worker.registry;

import com.github.vizaizai.worker.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册业务
 * @author liaochongwei
 * @date 2023/5/9 16:26
 */
public class RegistryService {

    /**
     * 注册地址
     */
    private static final String REGISTER_URL = "/worker/register";

    /**
     * worker注册
     * @param address 注册地址
     * @param appName 应用名称
     * @param serverAddr 调度中心地址
     */
    public void register(String address, String appName, String serverAddr) {
        Map<String,String> body = new HashMap<>();
        body.put("appName", appName);
        body.put("address", address);

        String result = HttpUtils.doPost(serverAddr + REGISTER_URL, body);
        if (StringUtils.isBlank(result)) {
            throw new RuntimeException("Worker register error.");
        }


    }

}

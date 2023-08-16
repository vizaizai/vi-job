package com.github.vizaizai.server.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liaochongwei
 * @date 2023/7/10 16:07
 */
public class H2Utils {


    public static void main(String[] args) throws Exception {


        Map<String,String> map = new HashMap();
        map.put("123","oldValue");
        String v = map.computeIfAbsent("123", k-> "newValue");

        System.out.println(v);

    }
}

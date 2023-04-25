package com.github.vizaizai.worker.core.processor.method;

import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.annotation.Job;
import com.github.vizaizai.worker.core.processor.BasicProcessor;
import com.github.vizaizai.worker.core.processor.MethodProcessor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单方法解析器
 * @author liaochongwei
 * @date 2023/4/25 15:28
 */
public class SimpleMethodResolver implements JobMethodResolver{
    private final List<Object> beans;

    public SimpleMethodResolver(List<Object> beans) {
        this.beans = beans;
    }

    @Override
    public Map<String, BasicProcessor> resolve() {
        if (Utils.isEmpty(beans)) {
            return Utils.newHashMap(0);
        }
        Map<String, BasicProcessor> methodProcessors = new HashMap<>();
        for (Object bean : beans) {
            Method[] methods = bean.getClass().getDeclaredMethods();
            if (methods.length == 0) {
                return null;
            }
            for (Method method : methods) {
                Job job = method.getAnnotation(Job.class);
                if (job != null) {
                    method.setAccessible(true);
                    methodProcessors.put(job.value(), new MethodProcessor(job.value(),bean, method));
                }
            }
        }

        return methodProcessors;
    }
}

package com.github.vizaizai.worker.processor;

import com.github.vizaizai.worker.core.processor.BasicProcessor;
import com.github.vizaizai.worker.core.processor.method.JobMethodResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * spring任务方法解析器
 * @author liaochongwei
 * @date 2023/4/25 16:04
 */
public class SpringMethodResolver implements JobMethodResolver, ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    @Override
    public Map<String, BasicProcessor> resolve() {
        return null;
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterSingletonsInstantiated() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}

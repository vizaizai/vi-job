package com.github.vizaizai.worker.processor;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.core.annotation.Job;
import com.github.vizaizai.worker.core.executor.TaskExecutor;
import com.github.vizaizai.worker.core.processor.BasicProcessor;
import com.github.vizaizai.worker.core.processor.MethodProcessor;
import com.github.vizaizai.worker.core.processor.method.JobMethodResolver;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * spring任务方法解析器
 * @author liaochongwei
 * @date 2023/4/25 16:04
 */
public class SpringMethodResolver implements JobMethodResolver, ApplicationContextAware, SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(SpringMethodResolver.class);
    private ApplicationContext applicationContext;
    /**
     * 方法处理器映射
     */
    private final Map<String, BasicProcessor> processorMap = new HashMap<>();

    @Override
    public Map<String, BasicProcessor> resolve() {
        TaskExecutor.register(processorMap);
        logger.info("TaskProcessors[{}]",processorMap.size());
        return processorMap;
    }

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);

        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);
            Map<Method, Job> annotatedMethods = null;
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                        (MethodIntrospector.MetadataLookup<Job>) method -> AnnotatedElementUtils.findMergedAnnotation(method, Job.class));
            } catch (Throwable ex) {
                logger.error("vi-job method-processor resolve error for bean[" + beanDefinitionName + "].", ex);
            }

            if (Utils.isNotEmpty(annotatedMethods)) {
                for (Map.Entry<Method, Job> entry : annotatedMethods.entrySet()) {
                    Method method = entry.getKey();
                    Job job = entry.getValue();
                    processorMap.put(job.value(), new MethodProcessor(job.value(), bean, method));
                }
            }
        }

        this.resolve();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

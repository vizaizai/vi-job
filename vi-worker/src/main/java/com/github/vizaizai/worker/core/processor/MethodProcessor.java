package com.github.vizaizai.worker.core.processor;

import com.github.vizaizai.worker.core.TaskContext;

import java.lang.reflect.Method;

/**
 * 方法处理器
 * @author liaochongwei
 * @date 2023/4/25 14:43
 */
public class MethodProcessor implements BasicProcessor {
    /**
     * 处理器名称
     */
    private String name;
    /**
     * 目标对象
     */
    private final Object bean;
    /**
     * 目标方法
     */
    private final Method method;

    public MethodProcessor(String name, Object bean, Method method) {
        this.name = name;
        this.bean = bean;
        this.method = method;
    }

    @Override
    public void execute(TaskContext taskContext) throws Exception{
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 0) {
            Object[] params = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                // 判断class是否为TaskContext
                if (parameterType == TaskContext.class) {
                    params[i] = taskContext;
                    break;
                }
            }
            this.method.invoke(this.bean, params);
            return;
        }
        this.method.invoke(this.bean);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return name + "[" + bean.getClass() + "." + method.getName() + "(...)]";
    }
}

package com.github.vizaizai.remote.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.vizaizai.common.model.Result;
import com.github.vizaizai.remote.codec.RpcRequest;
import com.github.vizaizai.remote.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author liaochongwei
 * @date 2022/2/18 14:25
 */
public class KryoSerializer implements Serializer {

    //每个线程的 Kryo 实例
    private static final ThreadLocal<Kryo> kryoLocal = ThreadLocal.withInitial(() -> {

        Kryo kryo = new Kryo();
        // 支持对象循环引用（否则会栈溢出），会导致性能些许下降 T_T
        kryo.setReferences(true); //默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置
        // 关闭序列化注册，会导致性能些许下降，但在分布式环境中，注册类生成ID不一致会导致错误
        kryo.setRegistrationRequired(false);
        // 设置类加载器为线程上下文类加载器（如果Processor来源于容器，必须使用容器的类加载器，否则妥妥的CNF）
        kryo.setClassLoader(Thread.currentThread().getContextClassLoader());

        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        Kryo kryo = kryoLocal.get();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output out = new Output(byteArrayOutputStream);
        try {
            kryo.writeObject(out, obj);
            out.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = kryoLocal.get();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input in = new Input(byteArrayInputStream);
        try {
            Object result = kryo.readObject(in, clazz);
            in.close();
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException ignored) {
            }
        }
    }
}

package com.github.vizaizai.server.utils;


import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class BeanUtils {

    /**
     * 转化成Bean
     * @param targetGetter 目标对象获取器（Object::new）
     * @param source 源对象
     * @param <E> 数据源类型
     * @param <V> 数据目标类型
     * @return
     */
    public static <E,V>  V toBean(E source,Supplier<V> targetGetter) {
        if (source == null) {
            return null;
        }
        V target = targetGetter.get();
        org.springframework.beans.BeanUtils.copyProperties(source, target);
        return target;
    }

    /**
     * 转化成Bean
     * @param targetClazz 目标类对象
     * @param source 源对象
     * @param <E> 数据源类型
     * @param <V> 数据目标类型
     * @return
     */
    public static <E,V>  V toBean(E source, Class<V> targetClazz) {
        if (source == null) {
            return null;
        }
        try {
            V target = targetClazz.newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        }catch (Exception e) {
            throw new RuntimeException("数据转换失败");
        }
    }

    /**
     * 转化成bean列表
     * @param sources 源对象列表
     * @param targetClazz 目标类对象
     * @param <E> 数据源类型
     * @param <V> 数据目标类型
     * @return
     */
    public static <E,V> List<V> toBeans(List<E> sources, Class<V> targetClazz) {
        if (sources == null) {
            return null;
        }
        return sources.stream()
                 .map(e -> toBean(e,targetClazz))
                 .collect(Collectors.toList());
    }

    /**
     * 转化成bean列表
     * @param sources 源对象列表
     * @param targetGetter 目标对象获取器（Object::new）
     * @param <E> 数据源类型
     * @param <V> 数据目标类型
     * @return
     */
    public static <E,V> List<V> toBeans(List<E> sources, Supplier<V> targetGetter) {
        if (sources == null) {
            return null;
        }
        return sources.stream()
                      .map(e -> toBean(e,targetGetter))
                      .collect(Collectors.toList());
    }

    /**
     * 转化成bean分页对象
     * @param pageSource 源分页对象
     * @param targetClazz 目标类对象
     * @param <E> 数据源类型
     * @param <V> 数据目标类型
     * @return
     */
    public static <E,V> Page<V> toPageBean(Page<E> pageSource, Class<V> targetClazz) {
        if (pageSource == null) {
            return null;
        }
        return pageSource.map(e -> toBean(e, targetClazz));
    }

    /**
     * 转化成bean分页对象
     * @param pageSource 源分页对象
     * @param targetGetter 目标对象获取器（Object::new）
     * @param <E> 数据源类型
     * @param <V> 数据目标类型
     * @return
     */
    public static <E,V> Page<V> toPageBean(Page<E> pageSource, Supplier<V> targetGetter) {
        if (pageSource == null) {
            return null;
        }
        return pageSource.map(e -> toBean(e, targetGetter));
    }
}

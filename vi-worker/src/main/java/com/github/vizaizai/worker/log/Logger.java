package com.github.vizaizai.worker.log;

/**
 * logger
 * @author liaochongwei
 * @date 2023/7/11 9:32
 */
public interface Logger {
    /**
     * 输出debug级别日志
     * @param format 格式
     * @param arguments 参数
     */
    void debug(String format, Object... arguments);
    /**
     * 输出info级别日志
     * @param format 格式
     * @param arguments 参数
     */
    void info(String format, Object... arguments);
    /**
     * 输出warn级别日志
     * @param format 格式
     * @param arguments 参数
     */
    void warn(String format, Object... arguments);
    /**
     * 输出error级别日志
     * @param format 格式
     * @param arguments 参数
     */
    void error(String format, Object... arguments);
}

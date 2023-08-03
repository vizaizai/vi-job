package com.github.vizaizai.worker.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * 日期工具类
 * @author liaochongwei
 * @date 2023/7/11 10:21
 */
public class DateUtils {

    public static final String NA_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    private DateUtils() {
    }

    /**
     * LocalDateTime格式化
     * @param temporal 日期时间
     * @param pattern 格式
     * @return String
     */
    public static String format(TemporalAccessor temporal, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(temporal);
    }

    /**
     * 解析成LocalDate
     * @param text 字符串
     * @param pattern 格式
     * @return LocalDateTime
     */
    public static LocalDate parseDate(String text, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(text, dateTimeFormatter);
    }
    public static LocalDateTime parse(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}

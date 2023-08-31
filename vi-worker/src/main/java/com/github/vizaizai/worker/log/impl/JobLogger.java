package com.github.vizaizai.worker.log.impl;

import com.github.vizaizai.worker.log.Logger;

import java.time.LocalDate;

/**
 * @author liaochongwei
 * @date 2023/8/31 15:48
 */
public class JobLogger implements Logger {
    /**
     * 日志id
     */
    private final long logId;

    private final JobLoggerHandler jobLoggerHandler;

    public JobLogger(long logId, long jobId, LocalDate date) {
        this.logId = logId;
        this.jobLoggerHandler = JobLoggerHandler.getInstance(jobId, date, true);
    }

    @Override
    public void debug(String format, Object... arguments) {
        jobLoggerHandler.debug(this.logId, format, arguments);
    }

    @Override
    public void info(String format, Object... arguments) {
        jobLoggerHandler.info(this.logId, format, arguments);
    }

    @Override
    public void warn(String format, Object... arguments) {
        jobLoggerHandler.warn(this.logId, format, arguments);
    }

    @Override
    public void error(String format, Object... arguments) {
        jobLoggerHandler.error(this.logId, format, arguments);
    }

    public void close() {
        this.jobLoggerHandler.close();
    }
}

package com.github.vizaizai.worker.runner;

import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.utils.DateUtils;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 日志清理定时运行器
 * @author liaochongwei
 * @date 2023/8/3 16:26
 */
public class LogClearScheduledRunner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LogClearScheduledRunner.class);
    /**
     * 日志基本路径
     */
    private String logBasePath;
    /**
     * 日志最大保留天数
     */
    private Integer maxHistory;
    private ScheduledFuture<?> scheduledFuture;
    private static LogClearScheduledRunner runner;
    private LogClearScheduledRunner() {
    }
    /**
     * 初始化并启动
     * @param logBasePath 日志基础路径
     * @param maxHistory 日志最大保留天数
     */
    public static void initAndStart(String logBasePath, Integer maxHistory, ScheduledExecutorService scheduledExecutorService) {
        if (runner != null) {
            return;
        }
        runner = new LogClearScheduledRunner();
        runner.logBasePath = logBasePath;
        runner.maxHistory = maxHistory;
        runner.scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(runner, 0, 1, TimeUnit.DAYS);
    }
    @Override
    public void run() {
        Path path = Paths.get(this.logBasePath);
        File baseDir = path.toFile();
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return;
        }
        File[] files = baseDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        // 删除最大保存天数意外的文件夹
        LocalDate date = LocalDate.now().minusDays(maxHistory);
        for (File file : files) {
            try {
                String name = file.getName();
                LocalDate fileDate = DateUtils.parseDate(name, "yyyyMMdd");
                if (fileDate.isBefore(date)) {
                    boolean delete = delete(file);
                    if (delete) {
                        logger.debug("Directory[{}] is deleted", file.getPath());
                    }
                }
            }catch (Exception e) {
                logger.error("Delete directory[{}] fail,", file.getName(), e);
            }
        }


    }

    public static void shutdown() {
        try {
            LogClearScheduledRunner runner = LogClearScheduledRunner.runner;
            if (runner != null) {
                runner.scheduledFuture.cancel(true);
            }
        }catch (Exception e) {
            logger.error("Shutdown LogClearRunner error,", e);
        }
    }

    /**
     * 递归删除文件以及文件夹
     * @param root 根文件
     * @return boolean
     */
    private static boolean delete(File root) {
        if (root != null && root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
                if (children != null) {
                    for (File child : children) {
                        delete(child);
                    }
                }
            }
            return root.delete();
        }
        return false;
    }
}

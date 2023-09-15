package com.github.vizaizai.worker.runner;

import com.github.vizaizai.common.model.StatusReportParam;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.remote.utils.Utils;
import com.github.vizaizai.worker.starter.Commons;
import org.slf4j.Logger;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 任务上报重试运行器
 * @author liaochongwei
 * @date 2023/5/31 17:19
 */
public class ReportRetryRunner extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(ReportRetryRunner.class);
    private static volatile ReportRetryRunner runner = null;
    private boolean stop = false;

    private ReportRetryRunner() {
    }

    public static ReportRetryRunner getInstance() {
        if (runner == null) {
            synchronized (ReportRetryRunner.class) {
                if (runner == null) {
                    runner = new ReportRetryRunner();
                    runner.setName("retry-reporter");
                    runner.start();
                }
            }
        }
        return runner;
    }

    /**
     * 写入重试参数
     * @param params 重试参数列表
     */
    public void writeRetryParam(List<StatusReportParam> params) {
        if (this.stop) {
            throw new IllegalStateException("Re-report runner is stopped");
        }

        File retryDir = Paths.get(Commons.getRetryPath()).toFile();
        // mk base dir
        if (!retryDir.exists()) {
            boolean mkdirs = retryDir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("Make dirs error");
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(retryDir.getPath(), Utils.getRequestId() + Commons.RETRY_SUFFIX)))){
            oos.writeObject(params);
        }catch (Exception e) {
            logger.error("Write object error,", e);
        }

    }


    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        while (!stop) {
            if (this.process() == 0) {
                // 空闲120s停止运行器
                if (System.currentTimeMillis() - startTime > Commons.MAX_IDLE) {
                    runner = null;
                    stop = true;
                    logger.debug("Thead[{}] idle 120s", this.getName());
                    this.interrupt();
                }
                this.sleepS(5);
            }else {
                this.sleepS(30);
            }
        }
    }

    private void sleepS(long timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        }catch (Exception ignored) {
        }
    }

    /**
     * 开始处理重试上报
     * @return 数量
     */
    @SuppressWarnings("unchecked")
    private int process() {
        int num = 0;
        File retryDir = Paths.get(Commons.getRetryPath()).toFile();
        if (!retryDir.exists() || !retryDir.isDirectory()) {
            return num;
        }
        File[] files = retryDir.listFiles();
        if (files == null || files.length == 0) {
            return num;
        }

        ReportRunner reportRunner = ReportRunner.getInstance();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(Commons.RETRY_SUFFIX)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))){
                    List<StatusReportParam> params = (List<StatusReportParam>) ois.readObject();
                    // 执行上报
                    reportRunner.doReport(null, params);
                    // 删除文件
                    boolean delete = file.delete();
                    if (!delete) {
                        logger.warn("Delete file[{}] fail", file.getPath());
                    }
                }catch (Exception e) {
                    logger.error("Re-report fail,", e);
                }
                num ++;
            }
        }
        return num;
    }
}

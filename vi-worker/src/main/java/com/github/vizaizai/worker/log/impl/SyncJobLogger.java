package com.github.vizaizai.worker.log.impl;

import com.github.vizaizai.common.model.LogInfo;
import com.github.vizaizai.logging.LoggerFactory;
import com.github.vizaizai.worker.log.Level;
import com.github.vizaizai.worker.log.Logger;
import com.github.vizaizai.worker.starter.Commons;
import com.github.vizaizai.worker.starter.PropsKeys;
import com.github.vizaizai.worker.utils.DateUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地任务日志实现（不支持异步处理）
 * @author liaochongwei
 * @date 2023/7/11 9:40
 */
@Deprecated
public class SyncJobLogger implements Logger {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SyncJobLogger.class);
    /**
     * 任务id
     */
    private final Long jobId;
    /**
     * 日志文件名
     */
    private String logFileName;
    /**
     * 日志索引文件名
     */
    private String logIndexFileName;
    /**
     * 输入通道
     */
    private FileChannel inFileChannel;
    /**
     * 输出通道
     */
    private FileChannel outFileChannel;
    /**
     * 读写索引文件
     */
    private RandomAccessFile indexAccessFile;
    /**
     * 日志id
     */
    private long logId;
    /**
     * 开始位置
     */
    private long startPos;
    /**
     * 结束位置
     */
    private long endPos;
    /**
     * 日期
     */
    private final String date;
    /**
     * 是否单例
     */
    private final boolean singleton;
    /**
     * job-logger映射
     */
    private static final Map<String, SyncJobLogger> jobLoggers = new ConcurrentHashMap<>();
    /**
     * 获取JobLogger实例
     * @param jobId 任务id
     * @param date 日期
     * @param singleton 是否单例
     * @return JobLogger
     */
    public synchronized static SyncJobLogger getInstance(Long jobId, LocalDate date, boolean singleton) {
        String dataStr = DateUtils.format(date, "yyyyMMdd");
        if (singleton) {
            String key = jobId + "_" + dataStr;
            SyncJobLogger jobLogger = jobLoggers.get(key);
            if (jobLogger != null) {
                return jobLogger;
            }
            jobLogger = new SyncJobLogger(jobId, dataStr, true);
            jobLoggers.put(key, jobLogger);
            return jobLogger;
        }
        return new SyncJobLogger(jobId, dataStr, false);
    }

    private SyncJobLogger(Long jobId, String date, boolean singleton) {
        this.jobId = jobId;
        this.date = date;
        this.singleton = singleton;
        this.init();
    }

    @Override
    public void debug(String format, Object... arguments) {
        this.process(Level.DEBUG, format, arguments);
    }

    @Override
    public void info(String format, Object... arguments) {
        this.process(Level.INFO, format, arguments);
    }

    @Override
    public void warn(String format, Object... arguments) {
        this.process(Level.WARN, format, arguments);
    }

    @Override
    public void error(String format, Object... arguments) {
        this.process(Level.ERROR, format, arguments);
    }

    public void init() {
        File logDir = Paths.get(Commons.getLogBasePath(), date).toFile();
        // mk base dir
        if (!logDir.exists()) {
            boolean mkdirs = logDir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("Make dirs error");
            }
        }
        String bindAddr = System.getProperty(PropsKeys.BIND_ADDR, "unknown")
                .replaceAll("[.:]","") + "_";

        this.logFileName = logDir.getPath() + File.separator + bindAddr + this.jobId + ".log";;
        this.logIndexFileName = logDir.getPath() + File.separator + bindAddr + this.jobId + ".idx";;
        try {
            Path path = Paths.get(this.logFileName);
            File file = path.toFile();
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                if (!newFile) {
                    log.warn("Create file error");
                }
            }
            inFileChannel = FileChannel.open(path, StandardOpenOption.READ);
            outFileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            indexAccessFile = new RandomAccessFile(this.logIndexFileName, "rw");
        }catch (Exception e) {
            log.error("Create os error, {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 日志输出处理
     * @param level 级别
     * @param format 格式
     * @param arguments 参数
     */
    private synchronized void process(Level level, String format, Object... arguments) {
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
        String message = formattingTuple.getMessage();
        if (formattingTuple.getThrowable() != null) {
            String stackTrace = ExceptionUtils.getStackTrace(formattingTuple.getThrowable());
            message = message + System.lineSeparator() + stackTrace;
        }

        // 获取调用栈信息
        Thread thread = Thread.currentThread();
        StackTraceElement[] stackTraceElements = thread.getStackTrace();
        StackTraceElement stackTraceElement = stackTraceElements[3];

        String logInfo = DateUtils.format(LocalDateTime.now(), DateUtils.NA_PATTERN) + " "
                + level + " "
                + "[" + thread.getName() + "]" + " "
                + stackTraceElement.getClassName() + ": "
                + message
                + "\n";

        this.writeBytes(logInfo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 重设位置
     * @param logId 日志id
     */
    public void resetPos(long logId) {
        this.logId = logId;
        this.startPos = this.size();
        this.endPos = 0L;
    }

    /**
     * 获取文件长度
     * @return pos
     */
    public long size() {
        try {
            return inFileChannel.size();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeBytes(byte[] bytes) {
        try {
            this.outFileChannel.write(ByteBuffer.wrap(bytes));
            this.endPos = this.outFileChannel.position();
            this.updateIndex(this.logId, Pair.of(this.startPos, this.endPos));
        }catch (Exception e) {
            log.debug("Write job log error,", e);
        }
    }

    public void close() {
        try {
            if (outFileChannel != null) {
                outFileChannel.close();
            }
            if (inFileChannel != null) {
                inFileChannel.close();
            }
            if (indexAccessFile != null) {
                indexAccessFile.close();
            }
            if (this.singleton) {
                jobLoggers.remove(this.jobId + "_" + date);
            }
        }catch (Exception ignored) {
        }
    }

    /**
     * 更新索引
     * @param logId 日志id
     * @param posRange 位置返回
     */
    private void updateIndex(long logId, Pair<Long,Long> posRange) {
        try {
            RandomAccessFile file = new RandomAccessFile(this.logIndexFileName, "rw");
            long length = file.length();
            if (length == 0) {
                String index = logId + "->" + posRange.getLeft() +"," + posRange.getRight() + "\n";
                file.write(index.getBytes(StandardCharsets.UTF_8));
                return;
            }
            long pos = length - 1;
            file.seek(pos);
            byte[] bytes = new byte[1];
            while (true) {
                if (file.read(bytes) != -1) {
                    if (bytes[0] != '\n' || pos == (length -1)) {
                        pos--;
                        if (pos == -1) {
                            file.seek(0);
                            this.appendOrRewriteIndex(file, length, logId, posRange);
                            break;
                        }
                        file.seek(pos);
                    }else {
                        this.appendOrRewriteIndex(file, length, logId, posRange);
                        break;
                    }
                }else {
                    break;
                }
            }

        }catch (Exception e) {
            log.error("Update log index error,", e);
        }
    }
    /**
     * 追加或重写索引数据
     * @param file 文件
     * @param length 长度
     * @param logId 日志id
     * @param posRange 位置范围
     */
    private void appendOrRewriteIndex(RandomAccessFile file, long length, long logId, Pair<Long,Long> posRange) {
        try {
            long currentPos = file.getFilePointer();
            byte[] line = new byte[1024];
            int i;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((i = file.read(line)) != -1) {
                bos.write(line, 0,  i);
            }
            String lastLine = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            String index = logId + "->" + posRange.getLeft() +"," + posRange.getRight() + "\n";
            byte[] indexBytes = index.getBytes();

            String[] lastIndex = lastLine.split("->");
            // 覆盖或者忽略
            if (Objects.equals(String.valueOf(logId),lastIndex[0])) {
                // 覆盖
                file.seek(currentPos);
                file.write(indexBytes);
                file.setLength(length - bos.toByteArray().length + indexBytes.length);
            }else {
                // 追加
                file.seek(length);
                file.write(indexBytes);
            }
            bos.close();
        }catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    /**
     * 获取日志位置范围
     * @param logId 日志id
     * @return pos
     */
    private Pair<Long, Long> getLogRange(long logId) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(this.logIndexFileName),StandardOpenOption.READ)))){
            String line;
            while ((line = br.readLine()) != null) {
                String[] index = line.split("->");
                if (Objects.equals(String.valueOf(logId), index[0])) {
                    String[] posRange = index[1].split(",");
                    return Pair.of(Long.parseLong(posRange[0]), Long.parseLong(posRange[1]));
                }
            }
            return null;
        }catch (Exception e) {
            log.error("Read log index error,", e);
            return null;
        }
    }

    /**
     * 获取日志信息
     * @param logId 日志id
     * @param startPos 起始位置
     * @param maxLines 最大行数
     * @return String
     */
    public LogInfo getLog(long logId, long startPos, int maxLines) {
        Pair<Long, Long> logRange = this.getLogRange(logId);
        if (logRange == null) {
            return null;
        }
        if (startPos < logRange.getLeft()) {
           startPos = logRange.getLeft();
        }
        if (startPos >= logRange.getRight()) {
           return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int lines = 0;
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            // 设置位置起始点
            inFileChannel.position(startPos);
            while (inFileChannel.read(byteBuffer) != -1) {
                byte[] bytes = byteBuffer.array();
                // 剩余字节数
                long remain = logRange.getRight() - inFileChannel.position();
                int len = byteBuffer.position();
                boolean breakFlag = false;
                // 当前日志末端
                if (remain < 0) {
                    len = (int) (Math.min(byteBuffer.capacity(), len) + remain);
                    breakFlag = true;
                }
                // 行数计数，并判断最大行数
                for (int i = 0; i < bytes.length; i++) {
                    byte b = bytes[i];
                    if (b == '\n') {
                        lines ++;
                    }
                    if (lines >= maxLines) {
                        len = i + 1;
                        breakFlag = true;
                        break;
                    }
                }
                bos.write(bytes, 0, len);
                byteBuffer.rewind();
                byteBuffer.clear();
                if (breakFlag) {
                    break;
                }
            }
            byte[] bytes = bos.toByteArray();
            LogInfo logInfo = new LogInfo();
            logInfo.setEndPos(startPos + bytes.length);
            logInfo.setData(new String(bos.toByteArray(), StandardCharsets.UTF_8));
            return logInfo;
        } catch (Exception e) {
            log.error("Read log error,", e);
            return null;
        }
    }

    public Long getJobId() {
        return jobId;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getLogIndexFileName() {
        return logIndexFileName;
    }

    public FileChannel getInFileChannel() {
        return inFileChannel;
    }

    public FileChannel getOutFileChannel() {
        return outFileChannel;
    }

    public RandomAccessFile getIndexAccessFile() {
        return indexAccessFile;
    }

    public long getLogId() {
        return logId;
    }

    public long getStartPos() {
        return startPos;
    }

    public long getEndPos() {
        return endPos;
    }
}

package com.github.vizaizai.common.model;

import java.io.Serializable;

/**
 * 日志信息
 * @author liaochongwei
 * @date 2023/7/14 15:31
 */
public class LogInfo implements Serializable {
    /**
     * 数据
     */
    private String data;
    /**
     * 结尾位置
     */
    private long endPos;
    /**
     * 行数
     */
    private int lines;


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getEndPos() {
        return endPos;
    }

    public void setEndPos(long endPos) {
        this.endPos = endPos;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "LogInfo{" +
                "data='" + data + '\'' +
                ", endPos=" + endPos +
                ", lines=" + lines +
                '}';
    }
}

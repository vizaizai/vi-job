package com.github.vizaizai.server.raft;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 快照文件
 * @author liaochongwei
 * @date 2023/6/26 9:56
 */
public class SnapshotFile {
    private static final Logger LOG = LoggerFactory.getLogger(SnapshotFile.class);

    private final String path;

    public SnapshotFile(String path) {
        super();
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    /**
     * Save value to snapshot file.
     */
    public boolean save(byte[] data) {
        try {
            FileUtils.writeByteArrayToFile(new File(path), data);
            return true;
        } catch (IOException e) {
            LOG.error("Fail to save snapshot", e);
            return false;
        }
    }

    public byte[] load() throws IOException {
        return FileUtils.readFileToByteArray(new File(path));
    }
}

package com.github.bdqfork.server.transaction.backup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.bdqfork.core.serializtion.JdkSerializer;
import com.github.bdqfork.core.util.DateUtils;
import com.github.bdqfork.server.database.DatabaseManager;
import com.github.bdqfork.server.transaction.OperationType;
import com.github.bdqfork.server.transaction.RedoLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bdq
 * @since 2020/09/22
 */
public abstract class AbstractBackupStrategy implements BackupStrategy {
    private static Logger log = LoggerFactory.getLogger(AbstractBackupStrategy.class);
    protected static final byte HEAD = 0x68;
    protected static final byte VERSION = 1;
    protected static final String TEMP_SUFFIX = ".tmp";
    protected static final String LOG_FILE_NAME = "jredis.log";
    protected static final String LOG_DATE_FILE_NAME_FORMATER = "jredis.%s.log";
    protected final String logFilePath;
    private RedoLogSerializer serializer;

    public AbstractBackupStrategy(String logFilePath) {
        this.logFilePath = logFilePath.replace("//", "/");
        File logDir = new File(logFilePath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        File file = new File(getFullLogFilePath());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error("Failed to create log file {} !", getFullLogFilePath());
                throw new IllegalStateException(e);
            }
        }
        this.serializer = new RedoLogSerializer(new JdkSerializer());
    }

    @Override
    public void redo(DatabaseManager databaseManager) {
        List<RedoLog> redoLogs = getRedoLogs();
        for (RedoLog redoLog : redoLogs) {
            int databaseId = redoLog.getDatabaseId();

            OperationType operationType = redoLog.getOperationType();

            String key = redoLog.getKey();

            if (operationType == OperationType.UPDATE) {
                Object value = redoLog.getValue();
                Long expireAt = redoLog.getExpireAt();
                databaseManager.saveOrUpdate(databaseId, key, value, expireAt);
            }

            if (operationType == OperationType.DELETE) {
                databaseManager.delete(databaseId, key);
            }
        }
    }

    private List<RedoLog> getRedoLogs() {
        File logFile = new File(getFullLogFilePath());

        if (logFile.length() == 0) {
            return Collections.emptyList();
        }

        File tmpFile = new File(getFullLogFilePath() + TEMP_SUFFIX);

        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        try {
            tmpFile.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        List<RedoLog> redoLogs = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(logFile);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(tmpFile, true);
                DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)) {
            int head;
            while ((head = dataInputStream.read()) != -1) {
                if (head != HEAD) {
                    throw new IllegalStateException(String.format("illegal head %s in back up file !", head));
                }
                byte version = dataInputStream.readByte();
                if (version != VERSION) {
                    throw new IllegalStateException(String.format("illegal version %s in back up file !", version));
                }

                int dataSize = dataInputStream.readInt();
                byte[] data = new byte[dataSize];
                dataInputStream.read(data);

                RedoLog redoLog = getSerializer().deserialize(data);
                redoLogs.add(redoLog);

                dataOutputStream.writeByte(head);
                dataOutputStream.writeByte(version);
                dataOutputStream.writeInt(dataSize);
                dataOutputStream.write(data);
            }
        } catch (IOException e) {
            log.warn("An error occurs during redo process: {}", e.getMessage());
        }
        logFile.renameTo(new File(getOldLogFilePath()));
        tmpFile.renameTo(new File(getFullLogFilePath()));
        return redoLogs;
    }

    protected String getOldLogFilePath() {
        return logFilePath + "/" + String.format(LOG_DATE_FILE_NAME_FORMATER, DateUtils.getNow("yyyy-MM-dd HH:mm:ss"));
    }

    protected String getFullLogFilePath() {
        return logFilePath + "/" + LOG_FILE_NAME;
    }

    public void setSerializer(RedoLogSerializer serializer) {
        this.serializer = serializer;
    }

    protected RedoLogSerializer getSerializer() {
        return this.serializer;
    }

    @Override
    public void reWrite(DatabaseManager databaseManager) {
        // TODO 扫描所有数据库，并生成RedoLog，同时序列化到磁盘
    }

}

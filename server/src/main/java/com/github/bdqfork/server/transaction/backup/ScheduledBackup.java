package com.github.bdqfork.server.transaction.backup;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.bdqfork.server.transaction.RedoLog;
import com.github.bdqfork.server.transaction.TransactionLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bdq
 * @since 2020/09/22
 */
public class ScheduledBackup extends AbstractBackupStrategy {
    private static Logger log = LoggerFactory.getLogger(ScheduledBackup.class);
    /**
     * RedoLog buffer
     */
    protected final Queue<TransactionLog> transactionLogs;

    public ScheduledBackup(String logFilePath, int bufferSize, long intervals) {
        super(logFilePath);
        this.transactionLogs = new ConcurrentLinkedQueue<>();
        Timer timer = new Timer("scheduled-backup");
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                log.debug("Backing up......");
                List<TransactionLog> buffer = new ArrayList<>(bufferSize);
                for (int i = 0; i < bufferSize && !transactionLogs.isEmpty(); i++) {
                    TransactionLog transactionLog = transactionLogs.poll();
                    buffer.add(transactionLog);
                }
                doBackup(buffer);
            }

        }, intervals, intervals);
        log.info("Scheduled backup started !");
    }

    @Override
    public void backup(TransactionLog transactionLog) {
        transactionLogs.add(transactionLog);
    }

    protected void doBackup(List<TransactionLog> transactionLogs) {
        for (TransactionLog transactionLog : transactionLogs) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    OutputStream outputStream = new FileOutputStream(new File(getFullLogFilePath()), true);
                    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {

                List<RedoLog> redoLogs = transactionLog.getRedoLogs();
                for (RedoLog redoLog : redoLogs) {
                    byte[] data = getSerializer().serialize(redoLog);
                    dataOutputStream.writeByte(HEAD);
                    dataOutputStream.writeByte(VERSION);
                    dataOutputStream.writeInt(data.length);
                    dataOutputStream.write(data);
                }
                outputStream.write(byteArrayOutputStream.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}

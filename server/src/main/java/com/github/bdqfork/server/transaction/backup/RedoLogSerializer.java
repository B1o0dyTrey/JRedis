package com.github.bdqfork.server.transaction.backup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.bdqfork.core.exception.SerializeException;
import com.github.bdqfork.core.serializtion.Serializer;
import com.github.bdqfork.server.transaction.OperationType;
import com.github.bdqfork.server.transaction.RedoLog;

public class RedoLogSerializer {
    private Serializer serializer;

    public RedoLogSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public byte[] serialize(RedoLog redoLog) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream)) {
            int operationType = redoLog.getOperationType().getValue();
            outputStream.writeByte(operationType);

            int databaseId = redoLog.getDatabaseId();
            outputStream.writeInt(databaseId);

            String key = redoLog.getKey();
            byte[] keyBuffer = key.getBytes(StandardCharsets.UTF_8);
            int keySize = keyBuffer.length;
            outputStream.writeInt(keySize);
            outputStream.write(keyBuffer);

            if (operationType == 0) {
                Object value = redoLog.getValue();
                byte[] valueBuffer = serializer.serialize(value);
                int valueSize = valueBuffer.length;
                outputStream.writeInt(valueSize);
                outputStream.write(valueBuffer);

                String valueType = value.getClass().getTypeName();
                byte[] valueTypeBuffer = valueType.getBytes(StandardCharsets.UTF_8);
                int valueTypeNameSize = valueTypeBuffer.length;
                outputStream.writeInt(valueTypeNameSize);
                outputStream.write(valueTypeBuffer);

                long expireAt = redoLog.getExpireAt();
                outputStream.writeLong(expireAt);
            }

            return byteArrayOutputStream.toByteArray();
        } catch (SerializeException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public RedoLog deserialize(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                DataInputStream inputStream = new DataInputStream(byteArrayInputStream)) {
            RedoLog redoLog = new RedoLog();
            byte operationType = inputStream.readByte();
            redoLog.setOperationType(OperationType.getOperationType(operationType));

            int databaseId = inputStream.readInt();
            redoLog.setDatabaseId(databaseId);

            int keySize = inputStream.readInt();
            byte[] keyBuffer = new byte[keySize];
            inputStream.read(keyBuffer);
            redoLog.setKey(new String(keyBuffer, StandardCharsets.UTF_8));

            if (operationType == 0) {
                int valueSize = inputStream.readInt();
                byte[] valueBuffer = new byte[valueSize];
                inputStream.read(valueBuffer);

                int valueTypeNameSize = inputStream.readInt();
                byte[] valueTypeBuffer = new byte[valueTypeNameSize];
                inputStream.read(valueTypeBuffer);
                String valueTypeName = new String(valueTypeBuffer, StandardCharsets.UTF_8);
                Class<?> valueType = null;
                if (!"byte[]".equals(valueTypeName)) {
                    valueType = Class.forName(valueTypeName);
                }
                Object value = serializer.deserialize(valueBuffer, valueType);
                redoLog.setValue(value);

                long expireAt = inputStream.readLong();
                redoLog.setExpireAt(expireAt);
            }
            return redoLog;
        } catch (SerializeException | IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}

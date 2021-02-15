package com.github.bdqfork.core.serializtion;

import com.github.bdqfork.core.exception.SerializeException;

import java.io.*;

/**
 * @author bdq
 * @since 2020/09/23
 */
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object instance) throws SerializeException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(instance);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) throws SerializeException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new SerializeException(e);
        }
    }
}

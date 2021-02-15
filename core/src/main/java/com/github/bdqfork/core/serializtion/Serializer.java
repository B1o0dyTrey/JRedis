package com.github.bdqfork.core.serializtion;

import com.github.bdqfork.core.exception.SerializeException;

/**
 * @author bdq
 * @since 2020/09/23
 */
public interface Serializer {

    byte[] serialize(Object instance) throws SerializeException;

    Object deserialize(byte[] bytes, Class<?> clazz) throws SerializeException;
}

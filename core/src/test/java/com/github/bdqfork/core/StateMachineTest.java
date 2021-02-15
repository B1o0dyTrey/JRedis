package com.github.bdqfork.core;

import com.github.bdqfork.core.protocol.StateMachine;
import com.github.bdqfork.core.protocol.LiteralWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class StateMachineTest {

    @Test
    public void decode() {
        StateMachine stateMachine = new StateMachine();
        String[] lines = {"*3", "*3", "$3", "foo", "$-1", "$3", "bar", "*-1", "-error"};
        LiteralWrapper<?> wrapper = null;
        for (String line : lines) {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(line.getBytes());
            wrapper = stateMachine.decode(byteBuf);
        }

        System.out.println(wrapper.toPlain());

        System.out.println(wrapper.encode());
    }
}
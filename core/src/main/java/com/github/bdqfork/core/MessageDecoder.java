package com.github.bdqfork.core;

import com.github.bdqfork.core.protocol.StateMachine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;

/**
 * RESP协议解码器
 *
 * @author Trey
 * @since 2020/10/22
 */
public class MessageDecoder extends LineBasedFrameDecoder {

    private StateMachine stateMachine;

    public MessageDecoder(int maxLength) {
        super(maxLength);
        stateMachine = new StateMachine();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, buffer);
        if (frame != null) {
            return stateMachine.decode(frame);
        }
        return null;
    }
}

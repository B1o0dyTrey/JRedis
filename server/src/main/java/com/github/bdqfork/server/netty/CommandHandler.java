package com.github.bdqfork.server.netty;

import com.github.bdqfork.core.operation.OperationContext;
import com.github.bdqfork.core.CommandFuture;
import com.github.bdqfork.core.Session;
import com.github.bdqfork.core.SessionHolder;
import com.github.bdqfork.core.protocol.LiteralWrapper;
import com.github.bdqfork.server.Dispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 接收解码后的命令，进行处理的Handler
 *
 * @author Trey
 * @since 2020/10/22
 */

public class CommandHandler extends SimpleChannelInboundHandler<Object> {
    private final Dispatcher dispatcher;

    public CommandHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        LiteralWrapper<?> literalWrapper = (LiteralWrapper<?>) msg;
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        OperationContext context = parse((InetSocketAddress) socketAddress, literalWrapper);
        CommandFuture future = dispatcher.dispatch(context);
        LiteralWrapper<?> result;
        try {
            result = (LiteralWrapper<?>) future.get();
        } catch (ExecutionException e) {
            result = LiteralWrapper.errorWrapper(e.getMessage());
        }
        ctx.writeAndFlush(result.encode());
    }

    private OperationContext parse(InetSocketAddress socketAddress, LiteralWrapper<?> command) {
        @SuppressWarnings("unchecked")
        List<LiteralWrapper<?>> literalWrappers = (List<LiteralWrapper<?>>) command.getData();

        String cmd = (String) literalWrappers.get(0).getData();

        Object[] args = literalWrappers.stream().skip(1).map(LiteralWrapper::getData).toArray();
        Session session = SessionHolder.getSession(socketAddress.getHostName(), socketAddress.getPort());
        return new OperationContext(session.getDatabaseId(), cmd, args);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        Session session = new Session(socketAddress.getHostName(), socketAddress.getPort(), 0);

        SessionHolder.setSession(session);

        LiteralWrapper<?> literalWrapper = LiteralWrapper.singleWrapper();
        literalWrapper.setData("Connect Ok!");
        ctx.writeAndFlush(literalWrapper.encode());
    }

}

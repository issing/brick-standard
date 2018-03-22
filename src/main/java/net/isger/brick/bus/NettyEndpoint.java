package net.isger.brick.bus;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;
import net.isger.brick.Constants;
import net.isger.brick.auth.AuthCommand;
import net.isger.brick.auth.AuthHelper;
import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.auth.BaseToken;
import net.isger.brick.core.Console;
import net.isger.util.Helpers;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * Netty端点
 * 
 * @author issing
 */
@Ignore
public abstract class NettyEndpoint extends SocketEndpoint {

    private static final AttributeKey<AuthIdentity> ATTR_IDENTITY = AttributeKey
            .valueOf("brick.bus.netty.channel.identity");

    private static final AttributeKey<Boolean> ATTR_IDENTITY_LOCAL = AttributeKey
            .valueOf("brick.bus.netty.channel.identity.local");

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    /** 总线 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Bus bus;

    @Ignore(mode = Mode.INCLUDE)
    @Alias("session.create")
    private boolean createSession;

    public NettyEndpoint() {
    }

    public NettyEndpoint(String host, int port) {
        super(host, port);
    }

    protected void open() {
        super.open();
        /* 初始协议 */
        final ChannelOutboundHandler encoder = new NettyEncoder();
        NettyDecoder pendingDecoder = new NettyDecoder();
        final ChannelInboundHandler decoder = CHANNEL_TCP
                .equalsIgnoreCase(getChannel()) ? pendingDecoder
                        : new DatagramPacketDecoder(pendingDecoder);
        /* 初始处理 */
        final ChannelInboundHandler handler = new NettyHandler();

        /* 引导处理器 */
        bootstrap(new ChannelInitializer<Channel>() {
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(decoder, encoder, handler);
            }
        });
    }

    /**
     * 引导
     *
     * @param initializer
     */
    protected abstract void bootstrap(ChannelInitializer<Channel> initializer);

    /**
     * 接收
     *
     * @param identity
     * @param message
     * @return
     */
    protected Object receive(AuthIdentity identity, Object message) {
        return getHandler().handle(this, identity, message);
    }

    /**
     * 发送
     *
     * @param context
     * @param message
     */
    protected void send(ChannelHandlerContext context, Object message) {
        context.channel().writeAndFlush(message);
    }

    protected void setIdentity(ChannelHandlerContext context,
            AuthIdentity identity) {
        context.channel().attr(ATTR_IDENTITY).setIfAbsent(identity);
    }

    protected AuthIdentity getIdentity(ChannelHandlerContext context) {
        return context.channel().attr(ATTR_IDENTITY).get();
    }

    protected void close() {
        super.close();
    }

    @Sharable
    private class NettyEncoder extends MessageToByteEncoder<Object> {
        protected void encode(ChannelHandlerContext ctx, Object message,
                ByteBuf out) throws Exception {
            byte[] value = getProtocol().getEncoder().encode(message);
            if (value != null) {
                out.writeBytes(value);
            }
        }
    }

    @Sharable
    private class NettyDecoder extends MessageToMessageDecoder<ByteBuf> {
        protected void decode(ChannelHandlerContext ctx, ByteBuf in,
                List<Object> out) throws Exception {
            Object message = getProtocol().getDecoder()
                    .decode(new ByteBufInputStream(in));
            if (message != null) {
                out.add(message);
            }
        }
    }

    @Sharable
    private class NettyHandler extends ChannelInboundHandlerAdapter {
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            /* 初始连接会话 */
            if (getIdentity(ctx) == null) {
                AuthCommand cmd = AuthHelper.toCommand(Constants.SYSTEM,
                        new BaseToken(ctx, ctx));
                cmd.setOperate(AuthCommand.OPERATE_LOGIN);
                console.execute(cmd);
                setIdentity(ctx, cmd.getIdentity());
                ctx.channel().attr(ATTR_IDENTITY_LOCAL).set(true);
            }
        }

        public void channelRead(ChannelHandlerContext ctx, Object message)
                throws Exception {
            AuthIdentity identity = getIdentity(ctx);
            identity.active(createSession); // 激活会话
            message = receive(identity, message);
            if (message != null) {
                send(ctx, message);
            }
        }

        public void channelInactive(ChannelHandlerContext ctx)
                throws Exception {
            /* 注销连接会话 */
            if (Helpers
                    .toBoolean(ctx.channel().attr(ATTR_IDENTITY_LOCAL).get())) {
                AuthIdentity identity = getIdentity(ctx);
                if (identity != null) {
                    AuthCommand cmd = AuthHelper.toCommand(Constants.SYSTEM,
                            identity.getToken());
                    cmd.setIdentity(identity);
                    cmd.setOperate(AuthCommand.OPERATE_LOGOUT);
                    console.execute(cmd);
                }
            }
            super.channelInactive(ctx);
        }
    }

}

package net.isger.brick.bus;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.isger.brick.core.Handler;
import net.isger.util.Asserts;

public class NettyInbound extends NettyEndpoint {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(NettyInbound.class);
    }

    private transient AbstractBootstrap<?, ?> bootstrap;

    private transient EventLoopGroup loop;

    private transient Channel service;

    private transient Handler channeler;

    protected final void open() {
        super.open();
        /* 绑定服务端口 */
        InetSocketAddress address = getAddress();
        try {
            LOG.info("Listening [{}://{}]", getProtocolName(), address);
            channeler.handle(service = bootstrap.bind().sync().channel());
        } catch (Exception e) {
            throw Asserts.state("Failure to bind [%s]", address, e);
        }
    }

    protected final void bootstrap(ChannelInitializer<Channel> initializer) {
        loop = new NioEventLoopGroup();
        if (CHANNEL_UDP.equalsIgnoreCase(getChannel())) {
            bootstrap = new Bootstrap().group(loop).channelFactory(new ChannelFactory<Channel>() {
                public Channel newChannel() {
                    return newDatagramChannel();
                }
            }).handler(initializer);
            bootstrap.localAddress(getAddress().getPort());
            bootstrap.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, false);
            bootstrap.option(ChannelOption.SO_REUSEADDR, true);
            channeler = new Handler() {
                public Object handle(Object message) {
                    open((DatagramChannel) message);
                    return null;
                }
            };
        } else {
            bootstrap = new ServerBootstrap().group(loop, new NioEventLoopGroup()).channelFactory(new ChannelFactory<ServerChannel>() {
                public ServerChannel newChannel() {
                    return newServerChannel();
                }
            }).childHandler(initializer);
            bootstrap.localAddress(getAddress());
            channeler = new Handler() {
                public Object handle(Object message) {
                    open((ServerChannel) message);
                    return null;
                }
            };
        }
    }

    protected DatagramChannel newDatagramChannel() {
        return new NioDatagramChannel(InternetProtocolFamily.IPv4);
    }

    protected void open(DatagramChannel channel) {
        InetSocketAddress address = getAddress();
        address.getHostName();
        try {
            channel.joinGroup(getAddress(), getNetworkInterface()).sync();
        } catch (Exception e) {
            throw Asserts.state(e.getMessage(), e.getCause());
        }
    }

    protected ServerChannel newServerChannel() {
        return new NioServerSocketChannel();
    }

    protected void open(ServerChannel channel) {
    }

    protected void send(ChannelHandlerContext context, Object message) {
    }

    protected void close() {
        if (service != null) {
            service.close();
            service = null;
        }
        loop.shutdownGracefully();
        super.close();
    }

}

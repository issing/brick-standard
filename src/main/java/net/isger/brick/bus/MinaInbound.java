package net.isger.brick.bus;

import java.net.SocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.Constants;
import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.auth.AuthToken;
import net.isger.brick.core.Console;
import net.isger.util.Asserts;
import net.isger.util.Helpers;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * MINA入端
 * 
 * @author issing
 */
public class MinaInbound extends MinaEndpoint {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(MinaInbound.class);
    }

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    /**
     * 打开服务端口
     */
    protected void open() {
        super.open();
        IoAcceptor acceptor = (IoAcceptor) getService();
        /* 绑定服务端口 */
        SocketAddress address = getAddress();
        try {
            /* 等待控制台就绪 */
            while (!console.hasReady()) {
                Helpers.sleep(200l);
            }
            acceptor.bind(address);
            LOG.info("Listening [{}://{}]", getProtocolName(), address);
        } catch (Exception e) {
            throw Asserts.state("Failure to bind [%s]", address, e);
        }
    }

    /**
     * 创建入端服务
     */
    protected IoAcceptor createService() {
        if ("udp".equalsIgnoreCase(getChannel())) {
            return new NioDatagramAcceptor();
        }
        return new NioSocketAcceptor();
    }

    /**
     * 获取服务会话
     *
     * @param cmd
     * @return
     */
    protected IoSession getSession(BusCommand cmd) {
        AuthIdentity identity = cmd.getIdentity();
        AuthToken<?> token = identity.getToken();
        if (token != null) {
            Object credentitals = token.getCredentials();
            if (credentitals instanceof IoSession) {
                return (IoSession) credentitals;
            }
        }
        return null;
    }

    /**
     * 发送服务报文
     *
     * @param cmd
     */
    public void send(BusCommand cmd) {
        Object payload = cmd.getPayload();
        IoSession session;
        if (payload != null && (session = getSession(cmd)) != null) {
            session.write(payload);
        }
    }

    /**
     * 关闭服务端口
     */
    protected void close() {
        ((SocketAcceptor) getService()).unbind();
        super.close();
    }

}

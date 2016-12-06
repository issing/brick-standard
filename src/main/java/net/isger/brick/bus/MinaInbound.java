package net.isger.brick.bus;

import java.io.IOException;
import java.net.SocketAddress;

import org.apache.mina.core.service.IoService;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaInbound extends MinaEndpoint {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(MinaInbound.class);
    }

    protected void open() {
        super.open();
        SocketAcceptor acceptor = (SocketAcceptor) getService();
        /* 绑定服务端口 */
        SocketAddress address = getAddress();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.info("Listening [{}://{}]", getProtocolName(), address);
            }
            acceptor.bind(address);
        } catch (IOException e) {
            throw new IllegalStateException("(X) Failure to bind [" + address
                    + "]", e);
        }
    }

    protected IoService createService() {
        return new NioSocketAcceptor();
    }

    protected void close() {
        ((SocketAcceptor) getService()).unbind();
        super.close();
    }

}

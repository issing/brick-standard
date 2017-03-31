package net.isger.brick.bus;

import java.net.SocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaOutbound extends MinaEndpoint {

    private static final Logger LOG;

    private transient IoSession session;

    static {
        LOG = LoggerFactory.getLogger(MinaOutbound.class);
    }

    protected IoService createService() {
        return new NioSocketConnector();
    }

    protected IoSession getSession() {
        IoSession session = this.session;
        SocketAddress address = getAddress();
        if (session == null || session.isClosing()) {
            ConnectFuture future = ((SocketConnector) getService())
                    .connect(address);
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                session = future.getSession();
                if (session != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.info("Connected to [{}://{}]", getProtocolName(),
                                address);
                    }
                    this.session = session;
                    break;
                }
            } while (!Thread.interrupted());
        }
        return session;
    }

    public void send(BusCommand cmd) {
        Object payload = cmd.getPayload();
        if (payload != null) {
            getSession().write(payload);
        }
    }

    protected void close() {
        if (session != null && session.isConnected()) {
            session.close(true);
            session = null;
        }
        super.close();
    }
}

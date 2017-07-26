package net.isger.brick.bus;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import net.isger.brick.auth.AuthIdentity;
import net.isger.util.anno.Ignore;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaOutbound extends MinaEndpoint {

    private static final Logger LOG;

    @Ignore
    private Map<String, IoSession> sessions;

    static {
        LOG = LoggerFactory.getLogger(MinaOutbound.class);
    }

    public MinaOutbound() {
        this.sessions = new HashMap<String, IoSession>();
    }

    protected IoService createService() {
        return new NioSocketConnector();
    }

    protected IoSession getSession(BusCommand cmd) {
        AuthIdentity identity = cmd.getIdentity();
        IoSession session = sessions.get(identity.getId());
        if (session == null || session.isClosing()) {
            SocketAddress address = getAddress();
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
                    sessions.put(identity.getId(), session);
                    setIdentity(session, identity);
                    break;
                }
            } while (!Thread.interrupted());
        }
        return session;
    }

    public void send(BusCommand cmd) {
        Object payload = cmd.getPayload();
        if (payload != null) {
            getSession(cmd).write(payload);
        }
    }

    protected void close() {
        for (IoSession session : sessions.values()) {
            if (session != null && session.isConnected()) {
                session.close(true);
            }
        }
        sessions.clear();
        super.close();
    }
}

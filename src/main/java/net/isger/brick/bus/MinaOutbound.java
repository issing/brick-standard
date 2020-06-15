package net.isger.brick.bus;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.auth.AuthIdentity;
import net.isger.util.Helpers;
import net.isger.util.anno.Ignore;

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
        String identityId = identity.getId();
        IoSession session = sessions.get(identityId);
        if (session == null || session.isClosing()) {
            SocketAddress address = getAddress();
            ConnectFuture future = ((SocketConnector) getService()).connect(address);
            do {
                Helpers.sleep(100l);
                session = future.getSession();
                if (session != null) {
                    LOG.info("Connected to [{}://{}]", getProtocolName(), address);
                    sessions.put(identityId, session);
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

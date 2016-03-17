package net.isger.brick.bus;

import java.io.IOException;

import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
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
        try {
            if (LOG.isDebugEnabled()) {
                LOG.info("Listening [{}://{}]", this.protocol, address);
            }
            acceptor.bind(address);
        } catch (IOException e) {
            throw new IllegalStateException("(X) Failure to bind [" + address
                    + "]", e);
        }
    }

    protected IoService createService() {
        IoService service = new NioSocketAcceptor();
        service.addListener(new IoServiceListener() {
            public void serviceActivated(IoService service) throws Exception {
                status = Status.ACTIVATED;
            }

            public void serviceIdle(IoService service, IdleStatus idleStatus)
                    throws Exception {
            }

            public void serviceDeactivated(IoService service) throws Exception {
                status = Status.DEACTIVATED;
            }

            public void sessionCreated(IoSession session) throws Exception {
            }

            public void sessionDestroyed(IoSession session) throws Exception {
            }
        });
        return service;
    }

    protected void close() {
        ((SocketAcceptor) getService()).unbind();
        super.close();
    }

}

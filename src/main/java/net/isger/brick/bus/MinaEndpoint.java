package net.isger.brick.bus;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Ignore
public abstract class MinaEndpoint extends SocketEndpoint {

    private static final String ATTR_IDENTITY = "brick.bus.mina.session.identity";

    private static final String ATTR_LOCAL = "brick.bus.mina.session.local";

    private static final Logger LOG;

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Bus bus;

    @Ignore(mode = Mode.INCLUDE)
    private boolean autoSession;

    private IoService service;

    static {
        LOG = LoggerFactory.getLogger(MinaEndpoint.class);
    }

    private ExecutorService executor;

    public MinaEndpoint() {
        executor = Executors.newCachedThreadPool();
    }

    /**
     * 获取服务
     * 
     * @return
     */
    protected IoService getService() {
        return service;
    }

    protected void open() {
        super.open();
        service = createService();
        /* 添加协议 */
        final ProtocolEncoder encoder = new ProtocolEncoderAdapter() {
            public void encode(IoSession session, Object message,
                    ProtocolEncoderOutput out) throws Exception {
                byte[] value = getProtocol().getEncoder().encode(message);
                if (value != null) {
                    IoBuffer buf = IoBuffer.allocate(value.length)
                            .setAutoExpand(true);
                    buf.put(value);
                    buf.flip();
                    out.write(buf);
                }
            }
        };
        final ProtocolDecoder decoder = new CumulativeProtocolDecoder() {
            protected boolean doDecode(IoSession session, IoBuffer in,
                    ProtocolDecoderOutput out) throws Exception {
                Object message = getProtocol().getDecoder()
                        .decode(in.asInputStream());
                boolean result = message != null;
                if (result) {
                    out.write(message);
                }
                return result;
            }
        };
        DefaultIoFilterChainBuilder chain = service.getFilterChain();
        chain.addLast(getProtocolName(),
                new ProtocolCodecFilter(new ProtocolCodecFactory() {
                    public ProtocolEncoder getEncoder(IoSession session)
                            throws Exception {
                        return encoder;
                    }

                    public ProtocolDecoder getDecoder(IoSession session)
                            throws Exception {
                        return decoder;
                    }
                }));
        /* 设置处理器 */
        service.setHandler(new IoHandlerAdapter() {
            public void sessionOpened(IoSession session) throws Exception {
                AuthIdentity identity = getIdentity(session);
                /* 建立连接会话 */
                if (identity == null) {
                    AuthCommand cmd = AuthHelper.toCommand(Constants.SYSTEM,
                            new BaseToken(session.getId(), session));
                    cmd.setOperate(AuthCommand.OPERATE_LOGIN);
                    console.execute(cmd);
                    setIdentity(session, identity = cmd.getIdentity());
                    session.setAttribute(ATTR_LOCAL, true);
                }
                String clientIP = ((InetSocketAddress) session
                        .getRemoteAddress()).getAddress().getHostAddress();
                identity.setAttribute(ATTR_CLIENT_IP, clientIP);
                getHandler().open(MinaEndpoint.this, identity);
                LOG.info("Session opened [{}]", session.getId());
            }

            public void messageReceived(IoSession session, Object message)
                    throws Exception {
                AuthIdentity identity = getIdentity(session);
                identity.active(autoSession); // 激活会话
                message = getHandler().handle(MinaEndpoint.this, identity,
                        message);
                if (message != null) {
                    session.write(message);
                }
            }

            public void sessionClosed(final IoSession session)
                    throws Exception {
                executor.execute(new Runnable() {
                    public void run() {
                        AuthIdentity identity = getIdentity(session);
                        try {
                            getHandler().close(MinaEndpoint.this, identity);
                        } catch (Exception e) {
                        }
                        /* 注销连接会话 */
                        if (Helpers
                                .toBoolean(session.getAttribute(ATTR_LOCAL))) {
                            if (identity != null) {
                                AuthCommand cmd = AuthHelper.toCommand(
                                        Constants.SYSTEM, identity.getToken());
                                cmd.setIdentity(identity);
                                cmd.setOperate(AuthCommand.OPERATE_LOGOUT);
                                console.execute(cmd);
                            }
                        }
                        LOG.info("Session Closed [{}]", session.getId());
                    }
                });
            }
        });
    }

    /**
     * 设置会话身份
     *
     * @param session
     * @param identity
     */
    protected void setIdentity(IoSession session, AuthIdentity identity) {
        session.setAttribute(ATTR_IDENTITY, identity);
    }

    /**
     * 获取会话身份
     *
     * @param session
     * @return
     */
    protected AuthIdentity getIdentity(IoSession session) {
        return (AuthIdentity) session.getAttribute(ATTR_IDENTITY);
    }

    /**
     * 创建服务
     * 
     * @return
     */
    protected abstract IoService createService();

    protected void close() {
        if (service != null) {
            service.dispose(true);
            service = null;
        }
    }

}

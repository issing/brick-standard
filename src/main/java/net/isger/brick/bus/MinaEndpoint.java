package net.isger.brick.bus;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import net.isger.util.Strings;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public abstract class MinaEndpoint extends SocketEndpoint {

    private static final byte[] MAGIC = "BRICK".getBytes();

    private static final int DATA_MIN_LIMIT = MAGIC.length + Integer.SIZE / 8;

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

    @Ignore(mode = Mode.INCLUDE)
    private Integer timeout;

    private IoService service;

    static {
        LOG = LoggerFactory.getLogger(MinaEndpoint.class);
    }

    private ExecutorService executor;

    public MinaEndpoint() {
        executor = Executors.newCachedThreadPool();
    }

    protected void open() {
        super.open();
        /* 初始数据 */
        service = createService();
        if (timeout == null || timeout < 2) {
            timeout = 2; // 默认会话2分钟超时
        }
        timeout += 5; // 超时延迟5分钟作为过渡
        /* 添加协议 */
        final ProtocolEncoder encoder = new ProtocolEncoderAdapter() {
            public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
                byte[] value = getProtocol().getEncoder().encode(message);
                if (value != null && value.length > 0) {
                    IoBuffer buffer = IoBuffer.allocate(value.length + DATA_MIN_LIMIT).setAutoExpand(true);
                    buffer.put(MAGIC);
                    buffer.putInt(value.length);
                    buffer.put(value);
                    buffer.flip();
                    out.write(buffer);
                    out.flush();
                }
            }
        };
        final ProtocolDecoder decoder = new CumulativeProtocolDecoder() {
            protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
                in.mark();
                int size = MinaEndpoint.this.correct(in);
                if (size < 0) {
                    in.reset();
                    return false;
                } else if (size == 0) {
                    return true;
                }
                byte[] content = new byte[size];
                in.get(content);
                Object message = getProtocol().getDecoder().decode(new ByteArrayInputStream(content));
                boolean result = message != null;
                if (result) {
                    out.write(message);
                }
                return result;
            }
        };
        DefaultIoFilterChainBuilder chain = service.getFilterChain();
        chain.addLast(getProtocolName(), new ProtocolCodecFilter(new ProtocolCodecFactory() {
            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
                return encoder;
            }

            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
                return decoder;
            }
        }));
        /* 设置处理器 */
        service.setHandler(new IoHandlerAdapter() {
            public void sessionOpened(IoSession session) throws Exception {
                AuthIdentity identity = getIdentity(session);
                getHandler().open(MinaEndpoint.this, identity);
                LOG.info("Session opened [{}] of [{}]", session.getId(), identity.getAttribute(ATTR_CLIENT_IP));
            }

            public void messageReceived(IoSession session, Object message) throws Exception {
                AuthIdentity identity = getIdentity(session); // 会话失效时，身份为新建实例（空数据）
                message = getHandler().handle(MinaEndpoint.this, identity, message);
                if (message != null) {
                    session.write(message);
                }
            }

            public void sessionClosed(final IoSession session) throws Exception {
                executor.execute(new Runnable() {
                    public void run() {
                        AuthIdentity identity = getIdentity(session);
                        String clientIP = Strings.empty(identity.getAttribute(ATTR_CLIENT_IP), "?");
                        try {
                            getHandler().close(MinaEndpoint.this, identity);
                        } catch (Exception e) {
                        }
                        /* 注销连接会话 */
                        if (Helpers.toBoolean(session.getAttribute(ATTR_LOCAL))) {
                            if (identity != null) {
                                AuthCommand cmd = AuthHelper.toCommand(Constants.SYSTEM, identity.getToken());
                                cmd.setIdentity(identity);
                                cmd.setOperate(AuthCommand.OPERATE_LOGOUT);
                                console.execute(cmd);
                            }
                        }
                        LOG.info("Session Closed [{}] of [{}]", session.getId(), clientIP);
                    }
                });
            }
        });
    }

    /**
     * 数据纠正
     *
     * @param in
     * @return
     */
    private int correct(IoBuffer in) {
        byte value;
        int index = 0;
        for (;;) {
            if (in.remaining() < DATA_MIN_LIMIT - index) {
                return -1;
            }
            value = in.get();
            if (value == MAGIC[index++]) {
                if (index == MAGIC.length) {
                    break;
                }
            } else {
                in.mark();
                index = 0;
            }
        }
        int size = in.getInt();
        if (size == 0) {
            in.mark();
        }
        return in.remaining() >= size ? size : -1;
    }

    /**
     * 创建服务
     * 
     * @return
     */
    protected abstract IoService createService();

    /**
     * 获取服务
     * 
     * @return
     */
    protected IoService getService() {
        return service;
    }

    /**
     * 获取身份
     *
     * @param session
     * @return
     */
    protected AuthIdentity getIdentity(IoSession session) {
        synchronized (session) {
            AuthIdentity identity = (AuthIdentity) session.getAttribute(ATTR_IDENTITY);
            /* 激活会话身份 */
            active: if (identity == null) {
                AuthCommand cmd = AuthHelper.toCommand(Constants.SYSTEM, new BaseToken(session.getId(), session)); // 默认系统会话身份（令牌为通信链路会话）
                cmd.setOperate(AuthCommand.OPERATE_LOGIN);
                console.execute(cmd);
                setIdentity(session, identity = cmd.getIdentity()); // 保存身份
                session.setAttribute(ATTR_LOCAL, true);
                identity.setAttribute(ATTR_CLIENT_IP, ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress());
                identity.setTimeout((int) TimeUnit.MINUTES.toMillis(timeout)); // 设置超时
                getHandler().reload(this, identity);
            } else {
                try {
                    identity.active(autoSession); // 激活身份
                    break active;
                } catch (Exception e) {
                    LOG.warn("(!) Failure to active session identity - {}", e.getMessage(), e.getCause());
                }
                try {
                    getHandler().unload(this, identity); // 卸载身份
                } catch (Exception e) {
                    LOG.warn("(!) Failure to unload session identity - {}", e.getMessage(), e.getCause());
                }
                setIdentity(session, null); // 删除身份
                identity = getIdentity(session); // 重新获取
            }
            return identity;
        }
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

    protected void close() {
        if (service != null) {
            service.dispose(true);
            service = null;
        }
    }

}

package net.isger.brick.bus;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.buffer.IoBuffer;
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
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

/**
 * 通信端点
 * 
 * @author issing
 *
 */
@Ignore
public abstract class MinaEndpoint extends SocketEndpoint {

    /** 通信魔数 */
    private static final byte[] MAGIC = "BRICK".getBytes();

    /** 最小报文 */
    private static final int DATA_MIN_LIMIT = MAGIC.length + Integer.SIZE / 8;

    private static final Logger LOG;

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    protected Console console;

    /** 总线 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Bus bus;

    @Ignore(mode = Mode.INCLUDE)
    private boolean createable;

    @Ignore(mode = Mode.INCLUDE)
    private Integer timeout;

    /** 任务执行器 */
    private ExecutorService executor;

    /** 会话管理器 */
    private Map<Long, AuthIdentity> manager;

    private IoService service;

    static {
        LOG = LoggerFactory.getLogger(MinaEndpoint.class);
    }

    public MinaEndpoint() {
        executor = Executors.newCachedThreadPool();
        manager = new HashMap<Long, AuthIdentity>();
    }

    /**
     * 开启服务
     */
    protected void open() {
        synchronized (this) {
            if (service != null) {
                return;
            }
        }
        super.open();
        /* 初始数据 */
        service = createService(); // 创建服务
        if (timeout == null || timeout < 1) {
            timeout = 1; // 默认会话1分钟超时
        }
        /* 添加协议 */
        final ProtocolEncoder encoder = createEncoder();
        final ProtocolDecoder decoder = createDecoder();
        service.getFilterChain().addLast(getProtocolName(), new ProtocolCodecFilter(new ProtocolCodecFactory() {
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
                AuthIdentity identity = getIdentity(session); // 会话重连时，身份为历史实例
                getHandler().open(MinaEndpoint.this, identity);
                LOG.info("Session [{}] opened of [{}]", session.getId(), identity.getAttribute(ATTR_CLIENT_IP));
            }

            public void messageReceived(IoSession session, Object message) throws Exception {
                AuthIdentity identity = getIdentity(session); // 会话失效时，身份为新建实例（空数据）
                LOG.debug("Session [{}] received message: \r\n{}", session.getId(), message);
                message = getHandler().handle(MinaEndpoint.this, identity, message);
                if (message != null) {
                    LOG.debug("Session [{}] send message: \r\n{}", session.getId(), message);
                    session.write(message);
                }
            }

            public void sessionClosed(final IoSession session) throws Exception {
                executor.execute(new Runnable() {
                    public void run() {
                        long sessionId = session.getId();
                        try {
                            AuthIdentity identity = getIdentity(session);
                            getHandler().close(MinaEndpoint.this, identity);
                            /* 注销连接会话 */
                            AuthCommand cmd = AuthHelper.makeCommand(Constants.SYSTEM, identity.getToken());
                            cmd.setIdentity(identity);
                            cmd.setOperate(AuthCommand.OPERATE_LOGOUT);
                            console.execute(cmd);
                        } catch (Exception e) {
                            LOG.warn("(!) Disconnect session exception - {}", e.getMessage(), e.getCause());
                        } finally {
                            manager.remove(sessionId);
                        }
                        LOG.info("Session [{}] closed", sessionId);
                    }
                });
            }
        });
    }

    /**
     * 创建服务
     * 
     * @return
     */
    protected abstract IoService createService();

    /**
     * 创建编码器
     * 
     * @return
     */
    protected ProtocolEncoder createEncoder() {
        return new ProtocolEncoderAdapter() {
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
    }

    /**
     * 创建解码器
     * 
     * @return
     */
    protected ProtocolDecoder createDecoder() {
        return new CumulativeProtocolDecoder() {
            protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
                in.mark();
                int size = MinaEndpoint.this.correct(in);
                if (size < 0) {
                    in.reset();
                    return false;
                } else if (size == 0) {
                    in.mark();
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
        return in.remaining() >= size ? size : -1;
    }

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
        long sessionId = session.getId();
        AuthIdentity identity = manager.get(sessionId); // 通信身份
        active: synchronized (session) {
            if (session.isConnected()) {
                /* 激活会话身份 */
                String clientIp = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
                if (identity == null) {
                    AuthCommand cmd = AuthHelper.makeCommand(Constants.SYSTEM, new BaseToken(sessionId, session)); // 默认系统会话身份（令牌为通信链路会话）
                    cmd.setOperate(AuthCommand.OPERATE_LOGIN);
                    console.execute(cmd);
                    manager.put(sessionId, identity = cmd.getIdentity()); // 保存身份
                    /* 设置通信身份信息 */
                    identity.setAttribute(ATTR_CLIENT_IP, clientIp);
                    identity.setTimeout((int) TimeUnit.MINUTES.toMillis(timeout + 5)); // 设置身份超时
                    session.getConfig().setBothIdleTime((int) TimeUnit.MINUTES.toSeconds(timeout + 5)); // 设置会话超时
                    getHandler().reload(this, identity); // 重载身份
                } else {
                    try {
                        identity.active(createable); // 激活身份
                        break active;
                    } catch (Exception e) {
                        LOG.warn("(!) Failure to active session [{}] identity - {}", sessionId, e.getMessage(), e.getCause());
                    }
                    try {
                        getHandler().unload(this, identity); // 卸载身份
                    } catch (Exception e) {
                        LOG.warn("(!) Failure to unload session [{}] identity - {}", sessionId, e.getMessage(), e.getCause());
                    }
                    manager.remove(sessionId); // 删除身份
                    identity = getIdentity(session); // 重新获取
                }
                /* 恢复通信身份信息（内部会话被切换时，属性可能会被清除） */
                identity.setAttribute(ATTR_CLIENT_IP, clientIp);
            }
        }
        return identity;
    }

    /**
     * 关闭服务
     */
    protected void close() {
        synchronized (this) {
            if (service != null) {
                service.dispose(true);
                service = null;
            }
        }
    }

}

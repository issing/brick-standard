package net.isger.brick.bus;

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
import net.isger.brick.core.CoreHelper;
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

    private static final String ATTR_IDENTITY = "brick.bus.identity";

    private static final Logger LOG;

    @Ignore(mode = Mode.INCLUDE)
    private boolean createable;

    /** 超时时长（分钟） */
    @Ignore(mode = Mode.INCLUDE)
    private int timeout;

    /** 任务执行器 */
    private transient ExecutorService executor;

    private transient IoService service;

    static {
        LOG = LoggerFactory.getLogger(MinaEndpoint.class);
    }

    public MinaEndpoint() {
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * 开启服务
     */
    protected void open() {
        synchronized (this) {
            if (this.service != null) return;
            super.open();
            this.service = createService(); // 创建服务
        }
        /* 初始数据 */
        this.timeout = (int) TimeUnit.MINUTES.toMillis(Math.max(this.timeout, 5)); // 默认会话5分钟超时
        /* 添加协议 */
        final ProtocolEncoder encoder = createEncoder();
        final ProtocolDecoder decoder = createDecoder();
        DefaultIoFilterChainBuilder filterChain = this.service.getFilterChain();
        filterChain.addLast(getProtocolName(), new ProtocolCodecFilter(new ProtocolCodecFactory() {
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
                if (identity != null) {
                    /* 打开会话处理 */
                    LOG.info("Session [{}] opened of [{}]", session.getId(), identity.getToken().getPrincipal());
                    try {
                        MinaEndpoint.this.getHandler().open(MinaEndpoint.this, identity);
                        return;
                    } catch (Throwable cause) {
                        if (LOG.isDebugEnabled()) LOG.warn("(!) Session [{}] open post-processing failed", session.getId(), cause);
                    }
                }
                session.closeNow(); // 关闭会话
            }

            public void messageReceived(IoSession session, Object message) throws Exception {
                AuthIdentity identity = getIdentity(session); // 会话失效时，身份为新建实例（空数据）
                if (identity != null) {
                    /* 接受消息处理 */
                    LOG.debug("Session [{}] received message: \r\n{}", session.getId(), message);
                    message = MinaEndpoint.this.getHandler().handle(MinaEndpoint.this, identity, message);
                    if (message != null) {
                        LOG.debug("Session [{}] response message: \r\n{}", session.getId(), message);
                        session.write(message); // 请求响应
                    }
                }
            }

            public void sessionClosed(final IoSession session) throws Exception {
                MinaEndpoint.this.executor.execute(new Runnable() {
                    public void run() {
                        try {
                            AuthIdentity identity = getIdentity(session);
                            /* 关闭会话处理 */
                            MinaEndpoint.this.getHandler().close(MinaEndpoint.this, identity);
                            /* 注销连接会话 */
                            AuthCommand cmd = AuthHelper.makeCommand(Constants.SYSTEM, identity.getToken());
                            cmd.setIdentity(identity);
                            cmd.setOperate(AuthCommand.OPERATE_LOGOUT);
                            MinaEndpoint.this.console.execute(cmd);
                        } catch (Exception e) {
                            LOG.warn("(!) Disconnect session exception", e);
                        }
                        LOG.info("Session [{}] closed", session.getId());
                    }
                });
            }
        });
        /* 端点激活 */
        this.toActive();
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
                Object message = getProtocol().getDecoder().decode(content);
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
            if (in.remaining() < DATA_MIN_LIMIT - index) return -1;
            value = in.get();
            if (value == MAGIC[index++]) {
                if (index == MAGIC.length) break;
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
    protected AuthIdentity getIdentity(final IoSession session) {
        CoreHelper.setConsole(this.console);
        AuthIdentity identity = (AuthIdentity) session.getAttribute(ATTR_IDENTITY); // 获取会话身份
        active: synchronized (session) {
            if (session.isConnected()) {
                /* 激活会话身份 */
                String client = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress(); // 远程主机地址信息
                if (identity == null) {
                    // 生成系统会话身份（令牌为通信链路会话）
                    session.setAttribute(ATTR_IDENTITY, identity = AuthHelper.toLogin(Constants.SYSTEM, new BaseToken(client, session)).getIdentity());
                    /* 设置通信身份信息 */
                    session.getConfig().setBothIdleTime(this.timeout); // 设置会话超时
                    identity.setTimeout(this.timeout); // 设置身份超时
                    this.getHandler().reload(this, identity); // 重载身份
                } else {
                    try {
                        identity.active(this.createable); // 激活身份（将自动更新访问频率）
                        break active;
                    } catch (Exception e) {
                        LOG.warn("(!) Failure to active session identity, Need to regenerate session identity - {}", e.getMessage(), e.getCause());
                    }
                    try {
                        this.getHandler().unload(this, identity); // 卸载身份
                    } catch (Exception e) {
                        LOG.warn("(!) Failure to unload session identity - {}", e.getMessage(), e.getCause());
                    }
                    session.removeAttribute(ATTR_IDENTITY); // 删除身份
                    identity = getIdentity(session); // 重新获取
                }
            }
        }
        return identity;
    }

    /**
     * 关闭服务
     */
    protected void close() {
        synchronized (this) {
            if (this.service != null) {
                this.service.dispose(true);
                this.service = null;
            }
        }
    }

}

package net.isger.brick.bus;

import java.net.InetSocketAddress;
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
import net.isger.brick.core.CoreHelper;
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

    private static final String ATTR_IDENTITY = "brick.bus.identity";

    /** 延迟超时（分钟） */
    private static final int DELAY_TIMEOUT = 5;

    private static final Logger LOG;

    /** 总线 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Bus bus;

    @Ignore(mode = Mode.INCLUDE)
    private boolean createable;

    /** 超时时长（分钟） */
    @Ignore(mode = Mode.INCLUDE)
    private Integer timeout;

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
            if (this.service != null) {
                return;
            }
            super.open();
            this.service = createService(); // 创建服务
        }
        /* 初始数据 */
        if (this.timeout == null || this.timeout < 1) {
            this.timeout = 1; // 默认会话1分钟超时【实际将会延迟5分钟，即6分钟超时】
        }
        /* 添加协议 */
        final ProtocolEncoder encoder = createEncoder();
        final ProtocolDecoder decoder = createDecoder();
        this.service.getFilterChain().addLast(getProtocolName(), new ProtocolCodecFilter(new ProtocolCodecFactory() {
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
                /* 打开会话处理 */
                LOG.info("Session [{}] opened of [{}]", session.getId(), identity.getToken().getPrincipal());
                getHandler().open(MinaEndpoint.this, identity);
            }

            public void messageReceived(IoSession session, Object message) throws Exception {
                AuthIdentity identity = getIdentity(session); // 会话失效时，身份为新建实例（空数据）
                /* 接受消息处理 */
                LOG.debug("Session [{}] received message: \r\n{}", session.getId(), message);
                message = getHandler().handle(MinaEndpoint.this, identity, message);
                if (message != null) {
                    LOG.debug("Session [{}] response message: \r\n{}", session.getId(), message);
                    session.write(message); // 请求响应
                }
            }

            public void sessionClosed(final IoSession session) throws Exception {
                MinaEndpoint.this.executor.execute(new Runnable() {
                    public void run() {
                        try {
                            AuthIdentity identity = getIdentity(session);
                            /* 关闭会话处理 */
                            getHandler().close(MinaEndpoint.this, identity);
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
        CoreHelper.setConsole(this.console);
        AuthIdentity identity = (AuthIdentity) session.getAttribute(ATTR_IDENTITY); // 获取会话身份
        active: synchronized (session) {
            if (session.isConnected()) {
                /* 激活会话身份 */
                String client = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress(); // 远程主机地址信息
                if (identity == null) {
                    // AuthCommand cmd =
                    // AuthHelper.makeCommand(Constants.SYSTEM, new
                    // BaseToken("brick.mina:" + sessionId, session));
                    // 默认系统会话身份（令牌为通信链路会话）
                    // cmd.setOperate(AuthCommand.OPERATE_LOGIN);
                    // this.console.execute(cmd);
                    // 生成系统会话身份（令牌为通信链路会话）
                    session.setAttribute(ATTR_IDENTITY, identity = AuthHelper.toLogin(Constants.SYSTEM, new BaseToken(client, session)).getIdentity());
                    /* 设置通信身份信息 */
                    // identity.setAttribute(ATTR_CLIENT, client);
                    int timeout = (int) TimeUnit.MINUTES.toMillis(this.timeout + DELAY_TIMEOUT); // 延迟5分钟
                    identity.setTimeout(timeout); // 设置身份超时
                    session.getConfig().setBothIdleTime(timeout); // 设置会话超时
                    getHandler().reload(this, identity); // 重载身份
                } else {
                    try {
                        identity.active(this.createable); // 激活身份
                        break active;
                    } catch (Exception e) {
                        LOG.warn("(!) Failure to active session identity - {}", e.getMessage(), e.getCause());
                    }
                    try {
                        getHandler().unload(this, identity); // 卸载身份
                    } catch (Exception e) {
                        LOG.warn("(!) Failure to unload session identity - {}", e.getMessage(), e.getCause());
                    }
                    session.removeAttribute(ATTR_IDENTITY); // 删除身份
                    identity = getIdentity(session); // 重新获取
                }
                /* 恢复通信身份信息（内部会话被切换时，属性可能会被清除） */
                // identity.setAttribute(ATTR_CLIENT, client);
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

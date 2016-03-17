package net.isger.brick.bus;

import net.isger.brick.Constants;
import net.isger.brick.bus.protocol.Protocol;
import net.isger.brick.core.Command;
import net.isger.brick.core.Console;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandler;
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
import org.apache.mina.filter.codec.serialization.ObjectSerializationDecoder;
import org.apache.mina.filter.codec.serialization.ObjectSerializationEncoder;

public abstract class MinaEndpoint extends SocketEndpoint {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Bus bus;

    private IoService service;

    protected Status status;

    protected void open() {
        service = createService();
        /* 添加协议 */
        final Protocol protocol = bus.getProtocol(this.protocol);
        final ProtocolEncoder encoder;
        final ProtocolDecoder decoder;
        if (protocol == null) {
            encoder = new ObjectSerializationEncoder();
            decoder = new ObjectSerializationDecoder();
        } else {
            encoder = new ProtocolEncoderAdapter() {
                public void encode(IoSession session, Object message,
                        ProtocolEncoderOutput out) throws Exception {
                    byte[] value = protocol.getEncoder().encode(message);
                    if (value != null) {
                        IoBuffer buf = IoBuffer.allocate(value.length)
                                .setAutoExpand(true);
                        buf.put(value);
                        buf.flip();
                        out.write(buf);
                    }
                }
            };
            decoder = new CumulativeProtocolDecoder() {
                protected boolean doDecode(IoSession session, IoBuffer in,
                        ProtocolDecoderOutput out) throws Exception {
                    Object message = protocol.getDecoder().decode(
                            in.asInputStream());
                    boolean result = message != null;
                    if (result) {
                        out.write(message);
                    }
                    return result;
                }
            };
        }
        DefaultIoFilterChainBuilder chain = service.getFilterChain();
        chain.addLast(this.protocol, new ProtocolCodecFilter(
                new ProtocolCodecFactory() {
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
        IoHandler ioHandler;
        if (handler == null) {
            ioHandler = new IoHandlerAdapter() {
                public void messageReceived(IoSession session, Object message)
                        throws Exception {
                    console.execute((Command) message);
                    session.write(message);
                }
            };
        } else {
            ioHandler = new IoHandlerAdapter() {
                public void messageReceived(IoSession session, Object message)
                        throws Exception {
                    message = handler.handle(message);
                    if (message != null) {
                        session.write(message);
                    }
                }
            };
        }
        service.setHandler(ioHandler);
    }

    public Status getStatus() {
        return status;
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

    protected void close() {
        service.dispose(true);
        service = null;
    }
}

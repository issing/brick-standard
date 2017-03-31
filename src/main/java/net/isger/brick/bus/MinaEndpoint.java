package net.isger.brick.bus;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

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

@Ignore
public abstract class MinaEndpoint extends SocketEndpoint {

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Bus bus;

    private IoService service;

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
                Object message = getProtocol().getDecoder().decode(in.array());
                boolean result = message != null;
                if (result) {
                    out.write(message);
                }
                return result;
            }
        };
        DefaultIoFilterChainBuilder chain = service.getFilterChain();
        chain.addLast(getProtocolName(), new ProtocolCodecFilter(
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
        service.setHandler(new IoHandlerAdapter() {
            public void messageReceived(IoSession session, Object message)
                    throws Exception {
                message = getHandler().handle(message);
                if (message != null) {
                    session.write(message);
                }
            }
        });
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

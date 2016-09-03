package org.onosproject.oxp.impl.oxpsuper;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.onosproject.net.DeviceId;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.impl.OxpDomain10;
import org.onosproject.oxp.impl.OxpSuper10;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.protocol.*;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by cr on 16-9-1.
 */
public class OxpSuperChannelHandler extends IdleStateAwareChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(OxpSuperChannelHandler.class);

    private OxpSuperController superController;
    private OXPDomain oxpDomain;
    private Channel channel;
    private volatile ChannelState state;

    private OXPVersion oxpVersion;
    private OXPFactory oxpFactory;

    private int handshakeTransactionIds = -1;

    public OxpSuperChannelHandler(OxpSuperController superController) {
        this.superController = superController;
        this.oxpVersion = superController.getOxpVersion();
        this.oxpFactory = OXPFactories.getFactory(oxpVersion);
    }

    /**
     * *******************************
     *   Channel State Machine
     * *******************************
     */
    enum ChannelState {

        INIT(false),
        WAIT_HELLO(false){
            @Override
            void processOxpHello(OxpSuperChannelHandler h, OXPHello m) throws IOException {
                if (m.getVersion().getWireVersion() == h.getOxpVersion().getWireVersion()) {
                    h.oxpVersion = h.getOxpVersion();
                    h.oxpFactory = OXPFactories.getFactory(h.oxpVersion);
                    h.oxpDomain = new OxpDomain10(h.superController);
                    h.oxpDomain.setOxpVersion(h.oxpVersion);
                    h.sendHandshakeHelloMsg();
                } else {
                    log.error("Received Hello of version {} from OxpDomainController at {}, but this DomainController " +
                            "work with OXP1.0. OxpDomainController disconnected ...", m.getVersion(), h.channel.getRemoteAddress());
                    h.channel.disconnect();
                    return;
                }
                h.sendHandshakeFeaturesRequestMsg();
                h.setState(WAIT_FEATURES_REPLY);
            }

            @Override
            void processOxpError(OxpSuperChannelHandler h, OXPErrorMsg m) throws IOException {
                logError(h, m);
            }
        },
        WAIT_FEATURES_REPLY(false) {
            @Override
            void processOxpFeatureReply(OxpSuperChannelHandler h, OXPFeaturesReply m) throws IOException {
                h.oxpDomain.setDomainId(m.getDomainId());
                h.oxpDomain.setDeviceId(DeviceId.deviceId("oxp:" + h.oxpDomain.getDomainId().toString()));
                h.oxpDomain.setCapabilities(m.getCapabilities());
                h.oxpDomain.setOxpSbpType(m.getSbpType());
                h.oxpDomain.setOxpSbpVersion(m.getSbpVsesion());
                h.sendHandshakeGetConfigRequestMsg();
                h.setState(WAIT_CONFIG_REPLY);
            }
        },
        WAIT_CONFIG_REPLY(false) {
            @Override
            void processOxpGetConfigReply(OxpSuperChannelHandler h, OXPGetConfigReply m) throws IOException {
                h.oxpDomain.setFlags(m.getFlags());
                h.oxpDomain.setPeriod(m.getPeriod());
                h.oxpDomain.setMissSendLen(m.getMissSendLength());
                h.oxpDomain.setChannel(h.channel);
                h.oxpDomain.setConnected(true);
                h.superController.addDomain(h.oxpDomain.getDeviceId(), h.oxpDomain);
                h.setState(ACTIVE);
            }
        },
        ACTIVE(true) {
            @Override
            void processOxpError(OxpSuperChannelHandler h, OXPErrorMsg m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOxpTopologyReply(OxpSuperChannelHandler h, OXPTopologyReply m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOXPHostRely(OxpSuperChannelHandler h, OXPHostReply m) throws IOException {
                h.dispatchMessage(m);
            }

            void processOXPHostUpdate(OxpSuperChannelHandler h, OXPHostUpdate m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOxpGetConfigReply(OxpSuperChannelHandler h, OXPGetConfigReply m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOXPVportStatus(OxpSuperChannelHandler h, OXPVportStatus m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOxpSbp(OxpSuperChannelHandler h, OXPSbp m) throws IOException {
                h.dispatchMessage(m);
            }
        };

        private final boolean handshakeComplete;
        ChannelState(boolean handshakeComplete) {
            this.handshakeComplete = handshakeComplete;
        }

        public boolean isHandshakeComplete() {
            return handshakeComplete;
        }

        protected String getSuperStateMessage(OxpSuperChannelHandler h,
                                              OXPMessage m,
                                              String details) {
            return String.format("Super State:[%s],received:[%s], details:[%s]",
                    this.toString(),
                    m.getType().toString(),
                    details);
        }

        /**
         * We have an OFMessage we didn't expect given the current state and
         * we want to ignore the message.
         * @param h
         * @param m
         */
        protected void unhandledMessageReceived(OxpSuperChannelHandler h,
                                                OXPMessage m) {
            if (log.isDebugEnabled()) {
                String msg = getSuperStateMessage(h, m, "Ignoring unexpected message");
                log.debug(msg);
            }
        }

        protected void logError(OxpSuperChannelHandler h, OXPErrorMsg error) {
            log.error("Oxp msg error:{} from super in state {}",
                    error,
                    this.toString());
        }

        void processOxpMessage(OxpSuperChannelHandler h, OXPMessage m) throws IOException{
            switch (m.getType()) {
                case OXPT_HELLO:
                    processOxpHello(h, (OXPHello) m);
                    break;
                case OXPT_ECHO_REQUEST:
                    processOxpEchoRequest(h, (OXPEchoRequest) m);
                    break;
                case OXPT_ECHO_REPLY:
                    processOxpEchoReply(h, (OXPEchoReply) m);
                    break;
                case OXPT_ERROR:
                    processOxpError(h, (OXPErrorMsg) m);
                    break;
                case OXPT_REATURES_REPLY:
                    processOxpFeatureReply(h, (OXPFeaturesReply) m);
                    break;
                case OXPT_GET_CONFIG_REPLY:
                    processOxpGetConfigReply(h, (OXPGetConfigReply) m);
                    break;
                case OXPT_SBP:
                    processOxpSbp(h, (OXPSbp) m);
                    break;
                case OXPT_TOPO_REPLY:
                    processOxpTopologyReply(h, (OXPTopologyReply) m);
                    break;
                case OXPT_HOST_UPDATE:
                    processOXPHostUpdate(h, (OXPHostUpdate) m);
                    break;
                case OXPT_HOST_REPLY:
                    processOXPHostRely(h, (OXPHostReply) m);
                    break;
                case OXPT_VPORT_STATUS:
                    processOXPVportStatus(h, (OXPVportStatus) m);
                default:
                    unhandledMessageReceived(h, m);
            }
        }

        void processOxpHello(OxpSuperChannelHandler h, OXPHello m) throws IOException{

        }

        void processOxpEchoRequest(OxpSuperChannelHandler h, OXPEchoRequest m) throws IOException {
            OXPEchoReply reply = h.oxpFactory.buildEchoReply()
                    .setData(m.getData())
                    .setXid(m.getXid())
                    .build();
            h.channel.write(Collections.singletonList(reply));
        }

        void processOxpEchoReply(OxpSuperChannelHandler h, OXPEchoReply m) throws IOException {

        }

        void processOxpError(OxpSuperChannelHandler h, OXPErrorMsg m) throws IOException {

        }

        void processOxpFeatureReply(OxpSuperChannelHandler h, OXPFeaturesReply m) throws IOException {

        }

        void processOxpGetConfigReply(OxpSuperChannelHandler h, OXPGetConfigReply m) throws IOException {

        }


        void processOxpTopologyReply(OxpSuperChannelHandler h, OXPTopologyReply m) throws IOException {

        }

        void processOXPHostUpdate(OxpSuperChannelHandler h, OXPHostUpdate m) throws IOException {

        }
        void processOXPHostRely(OxpSuperChannelHandler h, OXPHostReply m) throws IOException {

        }

        void processOXPVportStatus(OxpSuperChannelHandler h, OXPVportStatus m) throws IOException {

        }

        void processOxpSbp(OxpSuperChannelHandler h, OXPSbp m) throws IOException {

        }


    }

    private void sendHandshakeHelloMsg() throws IOException {
        OXPMessage.Builder mb = oxpFactory.buildHello()
                .setXid(this.handshakeTransactionIds--);
        log.info("Sending OXP_10 Hello to Domain: {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(mb.build()));
    }

    private void sendHandshakeFeaturesRequestMsg() throws IOException {
        OXPMessage.Builder mb = oxpFactory.buildFeaturesRequst()
                .setXid(this.handshakeTransactionIds--);
        channel.write(Collections.singletonList(mb.build()));
    }

    private void sendHandshakeGetConfigRequestMsg() throws IOException {
        OXPMessage.Builder mb = oxpFactory.buildGetConfigRequest()
                .setXid(this.handshakeTransactionIds--);
        channel.write(Collections.singletonList(mb.build()));
    }

    private void setState(ChannelState state) {
        this.state = state;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof List) {
            List<OXPMessage> msgList = (List<OXPMessage>) e.getMessage();

            for (OXPMessage oxpm : msgList) {
                state.processOxpMessage(this, oxpm);
            }
        } else {
            state.processOxpMessage(this, (OXPMessage) e.getMessage());
        }
    }

    //*************************
    //  Channel handler methods
    //*************************

    @Override
    public void channelConnected(ChannelHandlerContext ctx,
                                 ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        log.info("New switch connection from {}",
                channel.getRemoteAddress());
        /*
            hack to wait for the switch to tell us what it's
            max version is. This is not spec compliant and should
            be removed as soon as switches behave better.
         */
        //sendHandshakeHelloMessage();
        setState(ChannelState.WAIT_HELLO);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Oxp domain controller disconnected from here.");
        superController.removeDomain(oxpDomain.getDeviceId());
    }

    /**
     * TODO: Need change method to send echo periodically
     * @param ctx
     * @param e
     * @throws Exception
     */
    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        super.channelIdle(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.info(e.toString());
    }

    private void dispatchMessage(OXPMessage m) {
        oxpDomain.handleMessage(m);
    }

    public OXPVersion getOxpVersion() {
        return this.oxpVersion;
    }

}

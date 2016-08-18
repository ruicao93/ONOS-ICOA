package org.onosproject.oxp.impl;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.onosproject.oxp.OxpDomainController;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by cr on 16-8-15.
 */
public class OxpDomainChannelHandler extends IdleStateAwareChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(OxpDomainChannelHandler.class);

    private DomainConnector domainConnector;
    private OxpDomainController domainController;
    private OxpSuper oxpSuper;
    private Channel channel;
    private volatile ChannelState state;

    private OXPVersion oxpVersion;
    private OXPFactory oxpFactory;

    private int handshakeTransactionIds = -1;

    OxpDomainChannelHandler(OxpDomainController domainController) {
        this.domainController = domainController;
        this.state = ChannelState.INIT;
        this.oxpVersion = domainController.getOxpVersion();
        this.oxpFactory = OXPFactories.getFactory(oxpVersion);
    }


    public void disconnectSuper() {
        oxpSuper.disconnectSuper();
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
            void processOxpHello(OxpDomainChannelHandler h, OXPHello m) throws IOException {
                if (m.getVersion().getWireVersion() == h.getOxpVersion().getWireVersion()) {
                    h.oxpVersion = OXPVersion.OXP_10;
                    h.oxpFactory = OXPFactories.getFactory(h.oxpVersion);
                } else {
                    log.error("Received Hello of version {} from OxpSuperController at {}, but this DomainController " +
                            "work with OXP1.0. OxpSuperController disconnected ...", m.getVersion(), h.channel.getRemoteAddress());
                    h.channel.disconnect();
                    return;
                }
                h.setState(WAIT_FEATURES_REQUEST);
            }

            @Override
            void processOxpError(OxpDomainChannelHandler h, OXPErrorMsg m) throws IOException {
                logError(h, m);
            }
        },
        WAIT_FEATURES_REQUEST(false) {
            @Override
            void processOxpFeatureRequest(OxpDomainChannelHandler h, OXPFeaturesRequest m) throws IOException {
                h.sendFeaturesReply();
                h.setState(WAIT_CONFIG_REQUEST);
            }
        },
        WAIT_CONFIG_REQUEST(false) {
            @Override
            void processOxpGetConfigRequest(OxpDomainChannelHandler h, OXPGetConfigRequest m) throws IOException {
                h.sendGetConfigReply();
                h.oxpSuper = new OxpSuper10(h.domainController);
                h.oxpSuper.setChannel(h.channel);
                h.oxpSuper.setConnected(true);
                h.domainController.connectToSuper(h.oxpSuper);
                h.setState(ACTIVE);
            }
        },
        ACTIVE(true) {
            @Override
            void processOxpError(OxpDomainChannelHandler h, OXPErrorMsg m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOxpTopologyRequest(OxpDomainChannelHandler h, OXPTopologyRequest m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOXPHostRequest(OxpDomainChannelHandler h, OXPHostRequest m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOxpSetConfig(OxpDomainChannelHandler h, OXPSetConfig m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processOxpSbp(OxpDomainChannelHandler h, OXPSbp m) throws IOException {
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

        protected String getSuperStateMessage(OxpDomainChannelHandler h,
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
        protected void unhandledMessageReceived(OxpDomainChannelHandler h,
                                                OXPMessage m) {
            if (log.isDebugEnabled()) {
                String msg = getSuperStateMessage(h, m, "Ignoring unexpected message");
                log.debug(msg);
            }
        }

        protected void logError(OxpDomainChannelHandler h, OXPErrorMsg error) {
            log.error("Oxp msg error:{} from super in state {}",
                    error,
                    this.toString());
        }

        void processOxpMessage(OxpDomainChannelHandler h, OXPMessage m) throws IOException{
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
                case OXPT_FEATURES_REQUEST:
                    processOxpFeatureRequest(h, (OXPFeaturesRequest) m);
                    break;
                case OXPT_GET_CONFIG_REQUEST:
                    processOxpGetConfigRequest(h, (OXPGetConfigRequest) m);
                    break;
                case OXPT_SET_CONFIG:
                    processOxpSetConfig(h, (OXPSetConfig) m);
                    break;
                case OXPT_SBP:
                    processOxpSbp(h, (OXPSbp) m);
                    break;
                case OXPT_TOPO_REQUEST:
                    processOxpTopologyRequest(h, (OXPTopologyRequest) m);
                    break;
                case OXPT_HOST_REQUEST:
                    processOXPHostRequest(h, (OXPHostRequest) m);
                    break;
                default:
                    unhandledMessageReceived(h, m);
            }
        }

        void processOxpHello(OxpDomainChannelHandler h, OXPHello m) throws IOException{

        }

        void processOxpEchoRequest(OxpDomainChannelHandler h, OXPEchoRequest m) throws IOException {
            OXPEchoReply reply = h.oxpFactory.buildEchoReply()
                    .setData(m.getData())
                    .setXid(m.getXid())
                    .build();
            h.channel.write(Collections.singletonList(reply));
        }

        void processOxpEchoReply(OxpDomainChannelHandler h, OXPEchoReply m) throws IOException {

        }

        void processOxpError(OxpDomainChannelHandler h, OXPErrorMsg m) throws IOException {

        }

        void processOxpFeatureRequest(OxpDomainChannelHandler h, OXPFeaturesRequest m) throws IOException {

        }

        void processOxpGetConfigRequest(OxpDomainChannelHandler h, OXPGetConfigRequest m) throws IOException {

        }

        void processOxpSetConfig(OxpDomainChannelHandler h, OXPSetConfig m) throws IOException {

        }

        void processOxpTopologyRequest(OxpDomainChannelHandler h, OXPTopologyRequest m) throws IOException {

        }

        void processOXPHostRequest(OxpDomainChannelHandler h, OXPHostRequest m) throws IOException {

        }

        void processOxpSbp(OxpDomainChannelHandler h, OXPSbp m) throws IOException {

        }


    }


    private void sendHandshakeHelloMsg() throws IOException {
        OXPMessage.Builder mb = oxpFactory.buildHello()
                .setXid(this.handshakeTransactionIds--);
        log.info("Sending OXP_10 Hello to Super: {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(mb.build()));
    }

    private void sendFeaturesReply() throws IOException {
        OXPMessage m = oxpFactory.buildFeaturesReply()
                .setCapabilities(domainController.getCapabilities())
                .setDomainId(domainController.getDomainId())
                .setSbpType(domainController.getOxpSbpTpe())
                .setSbpVersion(domainController.getOxpSbpVersion())
                .setXid(this.handshakeTransactionIds--)
                .build();
        channel.write(Collections.singletonList(m));
    }

    private void sendGetConfigReply() throws IOException {
        OXPMessage m = oxpFactory.buildGetConfigReply()
                .setFlags(domainController.getFlags())
                .setPeriod((byte) domainController.getPeriod())
                .setMissSendLength((short) domainController.getMissSendLen())
                .build();
        channel.write(Collections.singletonList(m));
    }



    //**************************
    //   Channel handler methods
    //**************************
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        log.info("Connect to oxp super controller");
        sendHandshakeHelloMsg();
        setState(ChannelState.WAIT_HELLO);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Oxp super controller disconnected from here.");
        oxpSuper.disconnectSuper();
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

    //**************************
    //  Channel utility methods
    //**************************

    public boolean isHandshakeComplete() {
        return this.state.isHandshakeComplete();
    }

    private void dispatchMessage(OXPMessage m) {
        oxpSuper.handleMessage(m);
    }

    private void setState(ChannelState state) {
        this.state = state;
    }

    public OXPVersion getOxpVersion() {
        return this.oxpVersion;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.info(e.toString());
    }
}

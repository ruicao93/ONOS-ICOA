package org.onosproject.oxp.impl;

import org.jboss.netty.channel.Channel;
import org.onlab.packet.IpAddress;
import org.onosproject.oxp.OxpDomainController;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.protocol.OXPFactories;
import org.onosproject.oxp.protocol.OXPFactory;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by cr on 16-8-14.
 */
public class OxpSuper10 implements OxpSuper {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Channel channel;
    protected String channelId;

    private boolean connected;

    private OxpDomainController domainController;
    private OXPVersion oxpVersion;

    private final AtomicInteger xidCounter = new AtomicInteger(0);




    public OxpSuper10(OxpDomainController domainController) {
        this.domainController = domainController;
    }

    /**
     * Channel related
     */
    @Override
    public void disconnectSuper() {
        setConnected(false);
        this.channel.close();
    }

    @Override
    public void sendMsg(OXPMessage msg) {
        this.sendMsgsOnChannel(Collections.singletonList(msg));
    }

    @Override
    public void sendMsg(List<OXPMessage> msgs) {
        sendMsgsOnChannel(msgs);
    }


    /**
     * ***********************
     *   Message handling
     * ***********************
     * @param msg
     */
    @Override
    public void handleMessage(OXPMessage msg) {
        this.domainController.processMessage(msg);
    }


    @Override
    public OXPFactory factory() {
        return OXPFactories.getFactory(oxpVersion);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String channleId() {
        return channelId;
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public int getNextTransactionId() {
        return this.xidCounter.getAndIncrement();
    }

    private void sendMsgsOnChannel(List<OXPMessage> msgs) {
        if (channel.isConnected()) {
            channel.write(msgs);
            domainController.processDownstreamMessage(msgs);
        } else {
            log.warn("Drop msg because oxpsuper channel is disconnected,msgs:{}", msgs);
        }
    }

    public void sendHandshakeMessage(OXPMessage msg) {
        sendMsgsOnChannel(Collections.singletonList(msg));
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
        final SocketAddress address = channel.getRemoteAddress();
        if (address instanceof InetSocketAddress) {
            final InetSocketAddress inetAddress = (InetSocketAddress) address;
            final IpAddress ipAddress = IpAddress.valueOf(inetAddress.getAddress());
            if (ipAddress.isIp4()) {
                channelId = ipAddress.toString() + ':' + inetAddress.getPort();
            } else {
                channelId = '[' + ipAddress.toString() + "]:" + inetAddress.getPort();
            }
        }

    }

    public void setOxpVersion(OXPVersion oxpVersion) {
        this.oxpVersion = oxpVersion;
    }

}

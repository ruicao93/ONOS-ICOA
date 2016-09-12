package org.onosproject.oxp.impl;

import org.jboss.netty.channel.Channel;
import org.onosproject.net.DeviceId;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpSuper;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.DomainId;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by cr on 16-9-1.
 */
public class OxpDomain10 implements OXPDomain {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Set<OXPConfigFlags> flags;
    private int period;
    private long missSendLen;
    private Set<OXPCapabilities> capabilities;
    private OXPSbpType oxpSbpType;
    private OXPSbpVersion oxpSbpVersion;
    private DomainId domainId;
    private DeviceId deviceId;
    private OxpSuper oxpSuper;
    private OXPVersion oxpVersion;
    private OxpSuperController superController;
    private OFVersion ofVersion;

    private String channelId;
    private Channel channel;

    private boolean connected;

    boolean advancedModeFlag = false;
    boolean bwFlag = false;
    boolean delayFlag = false;  // TODO: support latency in the future
    boolean hopFlag = false;
    boolean compressModeFlag = false;


    public OxpDomain10(OxpSuperController superController) {
        this.superController = superController;
    }

    private void sendMsgsOnChannel(List<OXPMessage> msgs) {
        if (channel.isConnected()) {
            channel.write(msgs);
            superController.processDownstreamMessage(deviceId, msgs);
        } else {
            log.warn("Drop msg because oxpdomain channel is disconnected,msgs:{}", msgs);
        }
    }

    @Override
    synchronized public void sendMsg(OXPMessage msg) {
        this.sendMsgsOnChannel(Collections.singletonList(msg));
    }

    @Override
    public void sendMsg(List<OXPMessage> msgs) {
        sendMsgsOnChannel(msgs);
    }


    @Override
    public void handleMessage(OXPMessage msg) {
        this.superController.processMessage(deviceId, msg);
    }

    @Override
    public OXPFactory factory() {
        return OXPFactories.getFactory(oxpVersion);
    }

    @Override
    public OFFactory ofFactory() {
        return OFFactories.getFactory(ofVersion);
    }

    @Override
    public void setConnected(boolean isConnected) {
        this.connected = isConnected;
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
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public DeviceId getDeviceId() {
        return this.deviceId;
    }

    @Override
    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Set<OXPConfigFlags> getFlags() {
        return this.flags;
    }

    @Override
    public void setFlags(Set<OXPConfigFlags> flags) {
        this.flags = flags;
        if (getFlags().contains(OXPConfigFlags.MODE_ADVANCED)) {
            advancedModeFlag = true;
            if (getFlags().contains(OXPConfigFlags.CAP_BW)) {
                bwFlag = true;
            } else if (getFlags().contains(OXPConfigFlags.CAP_DELAY)) {
                delayFlag = true;
            } else if (getFlags().contains(OXPConfigFlags.CAP_HOP)) {
                hopFlag = true;
            } else {
                bwFlag = true; // default
            }
        }
        if (getFlags().contains(OXPConfigFlags.MODE_COMPRESSED)) {
            compressModeFlag = true;
        }
    }

    @Override
    public int getPeriod() {
        return this.period;
    }

    @Override
    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public long getMissSendLen() {
        return this.missSendLen;
    }

    @Override
    public void setMissSendLen(long missSendLen) {
        this.missSendLen = missSendLen;
    }

    @Override
    public Set<OXPCapabilities> getCapabilities() {
        return this.capabilities;
    }

    @Override
    public void setCapabilities(Set<OXPCapabilities> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public DomainId getDomainId() {
        return this.domainId;
    }

    @Override
    public void setDomainId(DomainId domainId) {
        this.domainId = domainId;
    }

    @Override
    public OXPSbpType getOxpSbpTpe() {
        return oxpSbpType;
    }

    @Override
    public void setOxpSbpType(OXPSbpType oxpSbpType) {
        this.oxpSbpType = oxpSbpType;
    }

    @Override
    public OXPSbpVersion getOxpSbpVersion() {
        return oxpSbpVersion;
    }

    @Override
    public void setOxpSbpVersion(OXPSbpVersion oxpSbpVersion) {
        this.oxpSbpVersion = oxpSbpVersion;
        if (oxpSbpType == OXPSbpType.OPENFLOW && oxpSbpVersion.getSbpVersion() == (0x4)) {
            ofVersion = OFVersion.OF_13;
        }
    }

    @Override
    public int getOxpSuperPort() {
        return 0;
    }

    @Override
    public void setOxpSuperPort(int oxpSuperPort) {

    }

    @Override
    public String getOxpSuperIp() {
        return null;
    }

    @Override
    public void setOxpSuperIp(String oxpSuperIp) {

    }

    @Override
    public OXPVersion getOxpVersion() {
        return oxpVersion;
    }

    @Override
    public void setOxpVersion(OXPVersion oxpVersion) {
        this.oxpVersion = oxpVersion;
    }

    @Override
    public boolean isAdvancedMode() {
        return advancedModeFlag;
    }

    @Override
    public boolean isCapBwSet() {
        return bwFlag;
    }

    @Override
    public boolean isCapDelaySet() {
        return delayFlag;
    }

    @Override
    public boolean isCapHopSet() {
        return hopFlag;
    }

    @Override
    public boolean isCompressedMode() {
        return compressModeFlag;
    }
}

package org.onosproject.oxp.oxpsuper;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.oxp.OXPDomain;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.protocol.*;
import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cr on 16-9-1.
 */
public interface OxpSuperController {
    OXPVersion getOxpVersion();
    void setOxpVersion(OXPVersion oxpVersion);

    int getOxpSuperPort();
    void setOxpSuperPort(int oxpSuperPort);

    String getOxpSuperIp();
    void setOxpSuperIp(String oxpSuperIp);

    void addMessageListener(OxpDomainMessageListener listener);

    void removeMessageListener(OxpDomainMessageListener listener);

    void addOxpDomainListener(OxpDomainListener listener);
    void removeOxpDomainListener(OxpDomainListener listener);

    void sendMsg(DeviceId deviceId, OXPMessage msg);

    void sendSbpPacketOut(DeviceId deviceId, PortNumber outPort, byte[] data);
    void sendSbpFwdReply(DeviceId deviceId, IpAddress srcIp, IpAddress dstIp,
                         PortNumber srcPort, PortNumber dstPort,
                         IpAddress mask, short ethType, byte qos);

    void addDomain(DeviceId deviceId, OXPDomain domain);
    void removeDomain(DeviceId deviceId);

    void processDownstreamMessage(DeviceId deviceId,List<OXPMessage> msgs);
    void processMessage(DeviceId deviceId,OXPMessage msg);

    OXPDomain getOxpDomain(DeviceId deviceId);
    Set<OXPDomain> getOxpDomains();
    long getDomainCount();

    Device getDevice(DeviceId deviceId);
    Set<Device> getDevices();

    OFMessage parseOfMessage(OXPSbp sbp);
    Ethernet parseEthernet(byte data[]);

    Map<OXPType, Long> getMsgCountStatis();
    Map<OXPType, Long> getMsgLengthStatis();
}

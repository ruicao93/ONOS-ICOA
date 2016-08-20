package org.onlab.packet;

import org.apache.commons.lang.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import static org.onlab.packet.LLDPOrganizationalTLV.OUI_LENGTH;
import static org.onlab.packet.LLDPOrganizationalTLV.SUBTYPE_LENGTH;

/**
 * Created by cr on 16-8-19.
 */
public class OXPLLDP extends LLDP {

    public static final byte[] ONLAB_OUI = {(byte) 0xa4, 0x23, 0x05};
    public static final String OXP_CHASSIS_VALUE_PREFIX = "dpid:";
    public static final String OXP_DOMAINID_VALUE_PREFIX = "domain_id:";

    public static final String OXP_ORGANIZATION_NAME = "OXP Discovery";

    protected static final byte NAME_SUBTYPE = 1;
    protected static final byte DEVICE_SUBTYPE = 2;
    protected static final byte DOMAIN_SUBTYPE = 3;

    private static final short NAME_LENGTH = OUI_LENGTH + SUBTYPE_LENGTH;
    private static final short DEVICE_LENGTH = OUI_LENGTH + SUBTYPE_LENGTH;
    private static final short DOMAIN_LENGTH = OUI_LENGTH + SUBTYPE_LENGTH;

    public static final byte OXP_CHASSIS_SUBTYPE = 7;
    public static final byte OXP_PORTID_SUBTYPE = 2;
    public static final byte OXP_DOMAINID_TYPE = 9;
    public static final byte OXP_DOMAINID_SUBTYPE = 7;
    public static final byte OXP_VPORTID_TYPE = 10;
    public static final byte OXP_VPORTID_SUBTYPE = 2;

    public static final short OXP_TL_LENGTH = 2;

    private  final byte[] ttlValue = new byte[] {0, 0x78};
    DecimalFormat df = new DecimalFormat("0000000000000000");


    public OXPLLDP() {
        setTtl(new LLDPTLV().setType(TTL_TLV_TYPE)
        .setLength((short) ttlValue.length)
        .setValue(ttlValue));
    }

    public OXPLLDP(LLDP lldp) {
        this.portId = lldp.getPortId();
        this.chassisId = lldp.getChassisId();
        this.ttl = lldp.getChassisId();
        this.optionalTLVList = lldp.getOptionalTLVList();
    }

    public void setChassisId(long dpid) {
        byte[] chassis = ArrayUtils.addAll(new byte[] {OXP_CHASSIS_SUBTYPE},
                (OXP_CHASSIS_VALUE_PREFIX + String.format("%016x", dpid)).getBytes());
        LLDPTLV chassisTLV = new LLDPTLV();
        chassisTLV.setLength((short) (chassis.length));
        chassisTLV.setType(CHASSIS_TLV_TYPE);
        chassisTLV.setValue(chassis);
        this.setChassisId(chassisTLV);
    }

    public void setPortId(int portNum) {
        byte[] port = ArrayUtils.addAll(new byte[] {OXP_PORTID_SUBTYPE},
                ByteBuffer.allocate(4).putInt(portNum).array());
        LLDPTLV portTlv = new LLDPTLV();
        portTlv.setLength((short) (port.length));
        portTlv.setType(PORT_TLV_TYPE);
        portTlv.setValue(port);
        this.setPortId(portTlv);
    }


    public void setDomainId(long domainId) {
        byte[] domain = ArrayUtils.addAll(new byte[] {OXP_DOMAINID_SUBTYPE},
                (OXP_DOMAINID_VALUE_PREFIX + String.format("%016x", domainId)).getBytes());
        LLDPTLV domainTlv = new LLDPTLV();
        domainTlv.setLength((short) domain.length);
        domainTlv.setType(OXP_DOMAINID_TYPE);
        domainTlv.setValue(domain);
        optionalTLVList.add(domainTlv);
    }

    public void setVportId(int vportNo) {
        byte[] vport = ArrayUtils.addAll(new byte[] {OXP_VPORTID_SUBTYPE},
                ByteBuffer.allocate(4).putInt(vportNo).array());
        LLDPTLV vportTlv = new LLDPTLV();
        vportTlv.setLength((short) vport.length);
        vportTlv.setType(OXP_VPORTID_TYPE);
        vportTlv.setValue(vport);
        optionalTLVList.add(vportTlv);
    }

    public void setOXPLLDPName(String name) {
        LLDPOrganizationalTLV nametlv = new LLDPOrganizationalTLV();
        nametlv.setLength((short) (name.length() + NAME_LENGTH));
        nametlv.setInfoString(name);
        nametlv.setSubType(NAME_SUBTYPE);
        nametlv.setOUI(ONLAB_OUI);
        optionalTLVList.add(nametlv);
    }

    public LLDPOrganizationalTLV getNameTlv() {
        for (LLDPTLV tlv : this.getOptionalTLVList()) {
            if (tlv.getType() == LLDPOrganizationalTLV.ORGANIZATIONAL_TLV_TYPE) {
                LLDPOrganizationalTLV orgTlv = (LLDPOrganizationalTLV) tlv;
                if (orgTlv.getSubType() == NAME_SUBTYPE) {
                    return orgTlv;
                }
            }
        }
        return null;
    }

    public String getNameString() {
        LLDPOrganizationalTLV tlv = getNameTlv();
        if (tlv != null) {
            return new String(tlv.getInfoString(), StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getDpid() {
        LLDPTLV chassisTlv = getChassisId();
        byte[] chassis = chassisTlv.getValue();
        ByteBuffer chassisBB = ByteBuffer.wrap(chassis);
        chassisBB.position(1);
        byte[] dpidBytes = new byte[chassis.length - 1];
        chassisBB.get(dpidBytes);
        String dpidStr = new String(dpidBytes, StandardCharsets.UTF_8);
        if (dpidStr.startsWith(OXP_CHASSIS_VALUE_PREFIX)) {
            return dpidStr.substring(OXP_CHASSIS_VALUE_PREFIX.length());
        }
        return null;
    }

    public int getPortNum() {
        LLDPTLV portTlv = getPortId();
        byte[] port = portTlv.getValue();
        ByteBuffer bb = ByteBuffer.wrap(port);
        bb.position(1);
        return bb.getInt();
    }

    public Long getDomainId() {
        for (LLDPTLV tlv : this.getOptionalTLVList()) {
            if (tlv.getType() == OXP_DOMAINID_TYPE) {
                byte[] domain = tlv.getValue();
                ByteBuffer bb = ByteBuffer.wrap(domain);
                bb.position(1);
                byte[] domainBytes = new byte[domain.length - 1];
                bb.get(domainBytes);
                String domainIdStr = new String(domainBytes, StandardCharsets.UTF_8);
                if (domainIdStr.startsWith(OXP_DOMAINID_VALUE_PREFIX)) {
                    return Long.valueOf(domainIdStr.substring(OXP_DOMAINID_VALUE_PREFIX.length()), 16);
                }
            }
        }
        return null;
    }

    public Integer getVportNum() {
        for (LLDPTLV tlv : this.getOptionalTLVList()) {
            if (tlv.getType() == OXP_VPORTID_TYPE) {
                ByteBuffer bb = ByteBuffer.wrap(tlv.getValue());
                bb.position(1);
                return bb.getInt();
            }
        }
        return null;
    }

    public static OXPLLDP parseOXPLLDP(Ethernet eth) {
        if (eth.getEtherType() == Ethernet.TYPE_LLDP) {
            OXPLLDP oxpLldp = new OXPLLDP((LLDP) eth.getPayload());
            if (null != oxpLldp.getDpid()) {
                return oxpLldp;
            }
        }
        return null;
    }

    public static OXPLLDP oxpLLDP(long dpid, int portNum, long domainId, int vportNum) {
        OXPLLDP probe = new OXPLLDP();
        probe.setChassisId(dpid);
        probe.setPortId(portNum);
        probe.setDomainId(domainId);
        probe.setVportId(vportNum);
        //probe.setOXPLLDPName(OXP_ORGANIZATION_NAME);
        return probe;
    }

}

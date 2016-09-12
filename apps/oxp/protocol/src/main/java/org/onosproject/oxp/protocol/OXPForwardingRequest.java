package org.onosproject.oxp.protocol;

import org.onosproject.oxp.types.IPv4Address;
import org.onosproject.oxp.types.MacAddress;

/**
 * Created by cr on 16-9-11.
 */
public interface OXPForwardingRequest extends OXPSbpCmpData {
    IPv4Address getSrcIpAddress();
    IPv4Address getDstIpAddress();
    int getInport();
    IPv4Address getMask();
    short getEthType();
    byte getQos();
    byte[] getData();
}

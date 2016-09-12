package org.onosproject.oxp.protocol;

/**
 * Created by cr on 16-9-11.
 */
public interface OXPPacketOut extends OXPSbpCmpData {
    long getOutPort();
    byte[] getData();
}

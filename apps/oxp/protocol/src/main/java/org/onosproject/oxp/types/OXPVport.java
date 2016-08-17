package org.onosproject.oxp.types;

import com.google.common.hash.PrimitiveSink;
import com.google.common.primitives.UnsignedInts;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-7-21.
 */
public class OXPVport implements OXPValueType<OXPVport> {

    static final short LENGTH = 2;

    private static final short OXPP_MAX_SHORT = (short) 0xf00;
    private static final short OXPP_IN_PORT_SHORT = (short) 0xff8;
    private static final short OXPP_FLOOD_SHORT = (short) 0xffb;
    private static final short OXPP_ALL_SHORT = (short) 0xffc;
    private static final short OXPP_CONTROLLER_SHORT = (short) 0xffd;
    private static final short OXPP_LOCAL_SHORT = (short) 0xffe;
    private static final short OXPP_NONE_SHORT = (short) 0xfff;

    private final short portNumber;

    public static final OXPVport MAX = new NamedVport(OXPP_MAX_SHORT, "max");
    public static final OXPVport IN_PORT = new NamedVport(OXPP_IN_PORT_SHORT, "in_port");
    public static final OXPVport FLOOD = new NamedVport(OXPP_FLOOD_SHORT, "flood");
    public static final OXPVport ALL = new NamedVport(OXPP_ALL_SHORT, "all");
    public static final OXPVport CONTROLLER = new NamedVport(OXPP_CONTROLLER_SHORT, "controller");
    public static final OXPVport LOCAL = new NamedVport(OXPP_LOCAL_SHORT, "local");
    public static final OXPVport NONE = new NamedVport(OXPP_NONE_SHORT, "noone");


    private OXPVport(short portNumber) {
        this.portNumber = portNumber;
    }

    public static OXPVport ofShort(final short portNumber) {
        switch (portNumber) {
            case OXPP_MAX_SHORT:
                return MAX;
            case OXPP_IN_PORT_SHORT:
                return IN_PORT;
            case OXPP_FLOOD_SHORT:
                return FLOOD;
            case OXPP_ALL_SHORT:
                return ALL;
            case OXPP_CONTROLLER_SHORT:
                return CONTROLLER;
            case OXPP_LOCAL_SHORT:
                return LOCAL;
            case OXPP_NONE_SHORT:
                return NONE;
            default:
                if (portNumber < 0 || portNumber > OXPP_MAX_SHORT)
                    throw new  IllegalArgumentException("Unknown special port number: "
                            + portNumber);
                return new OXPVport(portNumber);
        }
    }

    public short getPortNumber() {
        return portNumber;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public OXPVport applyMask(OXPVport mask) {
        return null;
    }

    @Override
    public int compareTo(OXPVport o) {
        return UnsignedInts.compare(this.portNumber, o.portNumber);
    }

    @Override
    public void putTo(PrimitiveSink sink) {
        sink.putShort(portNumber);
    }

    public void writeTo(ChannelBuffer bb) {
        bb.writeShort(portNumber);
    }

    public static OXPVport readFrom(ChannelBuffer bb) {
        return OXPVport.ofShort(bb.readShort());
    }

    static class NamedVport extends OXPVport {
        private final String name;
        NamedVport(final short portNo, final String name) {
            super(portNo);
            this.name = name;
        }
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OXPVport))
            return false;
        OXPVport other = (OXPVport) obj;
        if (this.portNumber != other.portNumber)
            return false;
        return true;
    }
}

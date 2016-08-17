
package org.onosproject.oxp.protocol;

/**
 * Created by cr on 16-4-6.
 * OXP type enum
 */
public enum OXPType {
    OXPT_HELLO(0),
    OXPT_ERROR(1),
    OXPT_ECHO_REQUEST(2),
    OXPT_ECHO_REPLY(3),
    OXPT_EXPERIMENTER(4),
    OXPT_FEATURES_REQUEST(5),
    OXPT_REATURES_REPLY(6),
    OXPT_GET_CONFIG_REQUEST(7),
    OXPT_GET_CONFIG_REPLY(8),
    OXPT_SET_CONFIG(9),
    OXPT_TOPO_REQUEST(10),
    OXPT_TOPO_REPLY(11),
    OXPT_HOST_REQUEST(12),
    OXPT_HOST_REPLY(13),
    OXPT_HOST_UPDATE(14),
    OXPT_VPORT_STATUS(15),
    OXPT_SBP(16),
    OXPT_VENDER(17);

    private int value;

    OXPType(int value) {
        this.value = value;
    }

    public static OXPType valaueOf(int value) {
        switch (value) {
            case 0:
                return OXPT_HELLO;
            case 1:
                return OXPT_ERROR;
            case 2:
                return OXPT_ECHO_REQUEST;
            case 3:
                return OXPT_ECHO_REPLY;
            case 4:
                return OXPT_EXPERIMENTER;
            case 5:
                return OXPT_FEATURES_REQUEST;
            case 6:
                return OXPT_REATURES_REPLY;
            case 7:
                return OXPT_GET_CONFIG_REQUEST;
            case 8:
                return OXPT_GET_CONFIG_REPLY;
            case 9:
                return OXPT_SET_CONFIG;
            case 10:
                return OXPT_TOPO_REQUEST;
            case 11:
                return OXPT_TOPO_REPLY;
            case 12:
                return OXPT_HOST_REQUEST;
            case 13:
                return OXPT_HOST_REPLY;
            case 14:
                return OXPT_HOST_UPDATE;
            case 15:
                return OXPT_VPORT_STATUS;
            case 16:
                return OXPT_SBP;
            case 17:
                return OXPT_VENDER;
            default:
                throw new IllegalArgumentException("Illegal wire value for type OXPSbpType in version 1.0: " + value);
        }
    }

    public int value() {
        return value;
    }
}

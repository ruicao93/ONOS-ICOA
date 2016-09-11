
package org.onosproject.oxp.protocol;

/**
 * Created by cr on 16-4-6.
 * OXP type enum
 */
public enum OXPType {
    OXPT_HELLO(0, "Hello"),
    OXPT_ERROR(1, "Error"),
    OXPT_ECHO_REQUEST(2,"EchoRequest"),
    OXPT_ECHO_REPLY(3, "EchoReply"),
    OXPT_EXPERIMENTER(4, "Experiment"),
    OXPT_FEATURES_REQUEST(5, "FeaturesRequest"),
    OXPT_REATURES_REPLY(6, "ReqturesReply"),
    OXPT_GET_CONFIG_REQUEST(7, "GetConfigRequest"),
    OXPT_GET_CONFIG_REPLY(8, "GetConfigReply"),
    OXPT_SET_CONFIG(9, "SetConfig"),
    OXPT_TOPO_REQUEST(10, "TopoRequest"),
    OXPT_TOPO_REPLY(11, "TopoReply"),
    OXPT_HOST_REQUEST(12, "HostRequest"),
    OXPT_HOST_REPLY(13, "HostReply"),
    OXPT_HOST_UPDATE(14, "HostUpdate"),
    OXPT_VPORT_STATUS(15, "VportStatus"),
    OXPT_SBP(16, "SBP"),
    OXPT_VENDER(17, "VENDOR");

    private int value;
    private String name;

    OXPType(int value, String name) {
        this.value = value;
        this.name = name;
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

    public String getName() {
        return name;
    }
}

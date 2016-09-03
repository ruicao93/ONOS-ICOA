package org.onosproject.net.topology;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.config.Config;

/**
 * Created by cr on 16-9-3.
 */
public class OxpDomainConfig extends Config<ApplicationId> {

    public static final String ID = "id";
    public static final String FLAGS = "flags";
    public static final String PERIOD = "period";
    public static final String MISS_SEND_LENGTH = "missSendLength";
    public static final String CAPABILITIES = "capabilities";
    public static final String SBPTYPE = "sbpType";
    public static final String SBPVERSION = "sbpVersion";
    public static final String OXPVERSION = "oxpVersion";
    public static final String SUPER_IP = "superIp";
    public static final String SUPER_PORT = "superPort";

    public String getDomainId() {
        return object.get(ID).asText();
    }

    public int getFlags() {
        return object.get(FLAGS).asInt();
    }

    public int getPeriod() {
        return object.get(PERIOD).asInt();
    }

    public int getMissSendLength() {
        return object.get(MISS_SEND_LENGTH).asInt();
    }

    public int getCapabilities() {
        return object.get(CAPABILITIES).asInt();
    }

    public int getSbpType() {
        return object.get(SBPTYPE).asInt();
    }

    public int getSbpVersion() {
        return object.get(SBPVERSION).asInt();
    }

    public int getOxpVersion() {
        return object.get(OXPVERSION).asInt();
    }
    public String getSuperIp() {
        return object.get(SUPER_IP).asText();
    }

    public int getSuperPort() {
        return object.get(SUPER_PORT).asInt();
    }
}

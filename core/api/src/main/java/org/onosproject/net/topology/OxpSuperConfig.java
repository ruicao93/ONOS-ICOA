package org.onosproject.net.topology;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.config.Config;

/**
 * Created by cr on 16-9-3.
 */
public class OxpSuperConfig extends Config<ApplicationId> {
    public static final String OXP_VERSION = "oxpVersion";
    public static final String SUPER_PORT = "superPort";

    public int getOxpVersin() {
        return object.get(OXP_VERSION).asInt();
    }

    public int getSuperPort() {
        return object.get(SUPER_PORT).asInt();
    }


}

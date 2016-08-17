package org.onosproject.oxp.protocol;

/**
 * Created by cr on 16-7-22.
 */
public enum  OXPSbpModeFlag {
    NORMAL(false),
    COMPRESSED(true);

    private boolean isCompressed;

    OXPSbpModeFlag(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public boolean isCompressed() {
        return isCompressed;
    }
}

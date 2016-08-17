package org.onosproject.oxp.protocol;

/**
 * Created by cr on 16-7-21.
 */
public enum OXPHostState {
    ACTIVE(true),
    INACTIVE(false);

    private final boolean activeState;

    private OXPHostState(boolean activeState) {
        this.activeState = activeState;
    }

    public boolean isActive() {
        return activeState;
    }
}

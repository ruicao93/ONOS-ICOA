
package org.onosproject.oxp.protocol;

/**
 * Created by cr on 16-4-6.
 */
public enum OXPVersion {
    OXP_10(1);

    public final int wireVersion;

    OXPVersion(int wireVersion) { this.wireVersion = wireVersion; }

    public int getWireVersion() {
        return wireVersion;
    }

    public int toWireValue() {
        return wireVersion;
    }

    public static OXPVersion ofWireValue(int wireValue) {
        switch (wireValue){
            case 1:
                return OXP_10;
            default:
                return null;
        }
    }
}

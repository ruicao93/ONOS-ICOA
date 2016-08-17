
package org.onosproject.oxp.protocol;

import org.onosproject.oxp.types.PrimitiveSinkable;

/**
 * Created by cr on 16-4-6.
 */
public interface OXPObject extends Writeable, PrimitiveSinkable {
    OXPVersion getVersion();
}

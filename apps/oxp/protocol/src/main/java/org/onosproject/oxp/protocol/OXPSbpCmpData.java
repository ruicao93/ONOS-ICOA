package org.onosproject.oxp.protocol;

import org.onosproject.oxp.types.PrimitiveSinkable;

/**
 * Created by cr on 16-9-11.
 */
public interface OXPSbpCmpData extends Writeable, PrimitiveSinkable {
    byte[] getData();
}

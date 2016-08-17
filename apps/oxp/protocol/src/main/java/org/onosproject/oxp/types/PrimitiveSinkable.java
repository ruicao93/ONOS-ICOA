package org.onosproject.oxp.types;

import com.google.common.hash.PrimitiveSink;

/**
 * Created by cr on 16-4-6.
 */
public interface PrimitiveSinkable {
    public void putTo(PrimitiveSink sink);
}

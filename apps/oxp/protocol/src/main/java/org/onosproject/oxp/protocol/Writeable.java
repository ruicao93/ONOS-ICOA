
package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by cr on 16-4-6.
 */
public interface Writeable {
    void writeTo(ChannelBuffer bb);
}

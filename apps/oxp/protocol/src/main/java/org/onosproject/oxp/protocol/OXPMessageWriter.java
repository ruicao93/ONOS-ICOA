package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;

/**
 * Created by cr on 16-4-7.
 */
public interface OXPMessageWriter<T> {
    public void write(ChannelBuffer bb, T message) throws OXPParseError;
}

package org.onosproject.oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.exceptions.OXPParseError;

/**
 * Created by cr on 16-4-7.
 */
public interface OXPMessageReader<T> {
    T readFrom(ChannelBuffer bb) throws OXPParseError;
}

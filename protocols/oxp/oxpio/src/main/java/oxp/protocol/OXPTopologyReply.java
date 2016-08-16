package oxp.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.oxp.types.OXPInternalLink;

import java.util.List;

/**
 * Created by cr on 16-7-21.
 */
public interface OXPTopologyReply extends OXPObject, OXPMessage {
    OXPVersion getVersion();
    OXPType getType();
    long getXid();
    List<OXPInternalLink> getInternalLinks();

    void writeTo(ChannelBuffer bb);

    Builder createBuilder();
    public interface Builder extends OXPMessage.Builder {
        OXPTopologyReply build();
        OXPVersion getVersion();
        OXPType getType();
        long getXid();
        Builder setXid(long xid);
        List<OXPInternalLink> getInternalLink();
        Builder setInternalLink(List<OXPInternalLink> list);
    }
}

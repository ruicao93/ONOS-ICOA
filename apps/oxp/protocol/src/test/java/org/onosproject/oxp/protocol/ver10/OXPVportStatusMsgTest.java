package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.OXPVport;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-22.
 */
public class OXPVportStatusMsgTest extends TestBaseVer10 {
    @Test
    public void OXPTopologyReplyMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPVport vport = OXPVport.ofShort((short) 1);
        Set<OXPVportState> state = new HashSet<>();
        state.add(OXPVportState.LIVE);
        OXPVportDesc vportDesc = new OXPVportDescVer10.Builder().setPortNo(vport)
                .setState(state)
                .build();
        OXPVportStatus vportStatus = getMsgFactory()
                .buildVportStatus()
                .setReason(OXPVportReason.MODIFY)
                .setVportDesc(vportDesc)
                .build();
        vportStatus.writeTo(buffer);
        assertThat(vportStatus, instanceOf(OXPVportStatusVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(vportStatus.getClass()));

        OXPVportStatus messageRev = (OXPVportStatus) vportStatus;
        assertThat(vportStatus, is(messageRev));
    }
}

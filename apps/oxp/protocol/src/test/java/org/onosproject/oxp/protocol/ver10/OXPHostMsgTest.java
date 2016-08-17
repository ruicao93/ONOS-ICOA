package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.IPv4Address;
import org.onosproject.oxp.types.MacAddress;
import org.onosproject.oxp.types.OXPHost;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-22.
 */
public class OXPHostMsgTest extends TestBaseVer10 {
    @Test
    public void OXPHostRequestMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPHostRequest hostRequest = getMsgFactory()
                .buildHostRequest()
                .build();
        hostRequest.writeTo(buffer);
        assertThat(hostRequest, instanceOf(OXPHostRequestVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(hostRequest.getClass()));

        OXPHostRequest messageRev = (OXPHostRequest) message;
        assertThat(hostRequest, is(messageRev));
    }

    @Test
    public void OXPHostReplyMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        IPv4Address ipAddress = IPv4Address.of("192.168.0.1");
        MacAddress macAddress = MacAddress.of("90:b1:1c:5b:df:4e");
        IPv4Address mask = IPv4Address.of("255.255.255.0");
        OXPHostState hostState = OXPHostState.ACTIVE;
        OXPHost host = OXPHost.of(ipAddress, macAddress, mask, hostState);
        List<OXPHost> hosts = new ArrayList<>();
        hosts.add(host);
        OXPHostReply hostReply = getMsgFactory()
                .buildHostReply()
                .setHosts(hosts)
                .build();
        hostReply.writeTo(buffer);
        assertThat(hostReply, instanceOf(OXPHostReplyVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(hostReply.getClass()));

        OXPHostReply messageRev = (OXPHostReply) message;
        assertThat(hostReply, is(messageRev));
    }

    @Test
    public void OXPHostUpdateMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        IPv4Address ipAddress = IPv4Address.of("192.168.0.1");
        MacAddress macAddress = MacAddress.of("90:b1:1c:5b:df:4e");
        IPv4Address mask = IPv4Address.of("255.255.255.0");
        OXPHostState hostState = OXPHostState.ACTIVE;
        OXPHost host = OXPHost.of(ipAddress, macAddress, mask, hostState);
        List<OXPHost> hosts = new ArrayList<>();
        hosts.add(host);
        OXPHostUpdate hostUpdate = getMsgFactory()
                .buildHostUpdate()
                .setHosts(hosts)
                .build();
        hostUpdate.writeTo(buffer);
        assertThat(hostUpdate, instanceOf(OXPHostUpdateVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(hostUpdate.getClass()));

        OXPHostUpdate messageRev = (OXPHostUpdate) message;
        assertThat(hostUpdate, is(messageRev));
    }
}

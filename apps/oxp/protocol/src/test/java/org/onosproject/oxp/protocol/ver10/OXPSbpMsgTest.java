package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.types.IPv4Address;
import org.onosproject.oxp.types.MacAddress;
import org.onosproject.oxp.types.OXPSbpData;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-23.
 */
public class OXPSbpMsgTest extends TestBaseVer10 {
    @Test
    public void OXPSbpNormalMsgTest() throws Exception{


        ChannelBuffer dataBuffer = ChannelBuffers.dynamicBuffer();
        OXPEchoRequest echoRequest = getMsgFactory()
                .buildEchoRequest()
                .setData("OXPEchoRequestMsg test.".getBytes())
                .build();
        echoRequest.writeTo(dataBuffer);

        byte[] data = new byte[dataBuffer.readableBytes()];
        dataBuffer.getBytes(0, data);


        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPSbpCmpType sbpCmpType = OXPSbpCmpType.NORMAL;
        Set<OXPSbpFlags> flags = new HashSet<>();
        flags.add(OXPSbpFlags.DATA_EXIST);
        OXPSbpData sbpData = OXPSbpData.of(data, OXPVersion.OXP_10);

        OXPSbp sbpMsg = getMsgFactory()
                .buildSbp()
                .setSbpCmpType(sbpCmpType)
                .setFlags(flags)
                .setDataLength((short) sbpData.getLength())
                .setSbpXid(echoRequest.getXid())
                .setSbpData(sbpData)
                .build();
        sbpMsg.writeTo(buffer);
        assertThat(sbpMsg, instanceOf(OXPSbpVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(sbpMsg.getClass()));

        OXPSbp messageRev = (OXPSbp) message;
        assertThat(sbpMsg, is(messageRev));

        ChannelBuffer sbpDataBuffer = ChannelBuffers.dynamicBuffer();
        sbpDataBuffer.writeBytes(sbpMsg.getSbpData().getData());
        OXPMessage sbpDataCopy = getMsgReader().readFrom(sbpDataBuffer);
        assertThat(sbpDataCopy, instanceOf(echoRequest.getClass()));

        OXPEchoRequest sbpDataParsed = (OXPEchoRequest) sbpDataCopy;
        assertThat(echoRequest, is(sbpDataParsed));
    }

    @Test
    public void OXPSbpFwdReqMsgTest() throws Exception{


        ChannelBuffer dataBuffer = ChannelBuffers.dynamicBuffer();
        OXPEchoRequest echoRequest = getMsgFactory()
                .buildEchoRequest()
                .setData("OXPEchoRequestMsg test.".getBytes())
                .build();
        echoRequest.writeTo(dataBuffer);

        byte[] data = new byte[dataBuffer.readableBytes()];
        dataBuffer.getBytes(0, data);


        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPSbpCmpType sbpCmpType = OXPSbpCmpType.FORWARDING_REQUEST;
        Set<OXPSbpFlags> flags = new HashSet<>();
        flags.add(OXPSbpFlags.DATA_EXIST);
        IPv4Address srcIpAddress = IPv4Address.of("192.168.0.1");
        IPv4Address dstIpAddress = IPv4Address.of("192.168.0.2");
        int inPort = 1;
        short ethType = 2048;
        byte qos = 1;
        IPv4Address mask = IPv4Address.of("255.255.255.0");
        OXPSbpCmpData sbpCmpData = OXPForwardingRequestVer10.of(srcIpAddress, dstIpAddress,
                inPort, mask,
                ethType, qos, data);

        OXPSbp sbpMsg = getMsgFactory()
                .buildSbp()
                .setSbpCmpType(sbpCmpType)
                .setFlags(flags)
                .setDataLength((short) sbpCmpData.getData().length)
                .setSbpXid(echoRequest.getXid())
                .setSbpCmpData(sbpCmpData)
                .build();
        sbpMsg.writeTo(buffer);
        assertThat(sbpMsg, instanceOf(OXPSbpVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(sbpMsg.getClass()));

        OXPSbp messageRev = (OXPSbp) message;
        assertThat(sbpMsg, is(messageRev));
        assertThat(messageRev.getSbpCmpData(), instanceOf(OXPForwardingRequestVer10.class));
        ChannelBuffer sbpDataBuffer = ChannelBuffers.dynamicBuffer();
        sbpDataBuffer.writeBytes(messageRev.getSbpCmpData().getData());
        OXPMessage sbpDataCopy = getMsgReader().readFrom(sbpDataBuffer);
        assertThat(sbpDataCopy, instanceOf(echoRequest.getClass()));

        OXPEchoRequest sbpDataParsed = (OXPEchoRequest) sbpDataCopy;
        assertThat(echoRequest, is(sbpDataParsed));
        assertThat(messageRev.getSbpCmpData(), is(sbpCmpData));
    }

    @Test
    public void OXPSbpFwdReplyMsgTest() throws Exception {
        IPv4Address srcIpAddress = IPv4Address.of("192.168.0.1");
        IPv4Address dstIpAddress = IPv4Address.of("192.168.0.2");
        int srcVport = 1;
        int dstVport = 2;
        IPv4Address mask = IPv4Address.of("255.255.255.0");
        short ethType = 2048;
        byte qos = 1;
        OXPForwardingReply sbpCmpFwdReply = OXPForwardingReplyVer10.of(srcIpAddress, dstIpAddress, srcVport, dstVport,
                mask, ethType, qos);
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPSbpCmpType sbpCmpType = OXPSbpCmpType.FORWARDING_REPLY;
        Set<OXPSbpFlags> flags = new HashSet<>();
        flags.add(OXPSbpFlags.DATA_EXIST);
        OXPSbp sbpMsg = getMsgFactory()
                .buildSbp()
                .setSbpCmpType(sbpCmpType)
                .setFlags(flags)
                .setDataLength((short) sbpCmpFwdReply.getData().length)
                .setSbpXid(1)
                .setSbpCmpData(sbpCmpFwdReply)
                .build();
        sbpMsg.writeTo(buffer);

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(sbpMsg.getClass()));
        OXPSbp messageRev = (OXPSbp) message;
        assertThat(sbpMsg, is(messageRev));
    }

    @Test
    public void OXPSbpPacketOutMsgTest() throws Exception {
        int outPort = 1;
        byte[] data = new byte[] {1,2,3,4,5};
        OXPPacketOut oxpPacketOut = OXPPacketOutVer10.of(outPort, data);
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPSbpCmpType sbpCmpType = OXPSbpCmpType.PACKET_OUT;
        Set<OXPSbpFlags> flags = new HashSet<>();
        flags.add(OXPSbpFlags.DATA_EXIST);
        OXPSbp sbpMsg = getMsgFactory()
                .buildSbp()
                .setSbpCmpType(sbpCmpType)
                .setFlags(flags)
                .setDataLength((short) oxpPacketOut.getData().length)
                .setSbpXid(1)
                .setSbpCmpData(oxpPacketOut)
                .build();
        sbpMsg.writeTo(buffer);

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(sbpMsg.getClass()));
        OXPSbp messageRev = (OXPSbp) message;
        assertThat(sbpMsg, is(messageRev));
    }
}

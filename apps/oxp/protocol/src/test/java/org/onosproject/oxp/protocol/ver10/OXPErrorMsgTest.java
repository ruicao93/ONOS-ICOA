package org.onosproject.oxp.protocol.ver10;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.oxp.protocol.OXPBadRequestCode;
import org.onosproject.oxp.protocol.OXPDomainConfigFaliedCode;
import org.onosproject.oxp.protocol.OXPHelloFailedCode;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.errormsg.OXPBadRequestErrorMsg;
import org.onosproject.oxp.protocol.errormsg.OXPDomainConfigFailedErrorMsg;
import org.onosproject.oxp.protocol.errormsg.OXPHelloFailedErrorMsg;
import org.onosproject.oxp.types.OXPErrorCauseData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by cr on 16-7-17.
 */
public class OXPErrorMsgTest extends TestBaseVer10{
    @Test
    public void OXPHelloFailedErrorMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPHelloFailedErrorMsg helloFailedErrorMsg = getMsgFactory().errorMsgs()
                .buildHelloFailedErrorMsg()
                .setCode(OXPHelloFailedCode.EPERM)
                .setData(OXPErrorCauseData.NONE)
                .build();
        helloFailedErrorMsg.writeTo(buffer);
        assertThat(helloFailedErrorMsg, instanceOf(OXPHelloFailedErrorMsgVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(helloFailedErrorMsg.getClass()));

        OXPHelloFailedErrorMsg messageRev = (OXPHelloFailedErrorMsg) message;
        assertThat(helloFailedErrorMsg, is(messageRev));
    }

    @Test
    public void OXPBadRequestErrorMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPBadRequestErrorMsg badRequestErrorMsg = getMsgFactory().errorMsgs()
                .buildBadRequestErrorMsg()
                .setCode(OXPBadRequestCode.BAD_EXP_TYPE)
                .setData(OXPErrorCauseData.NONE)
                .build();
        badRequestErrorMsg.writeTo(buffer);
        assertThat(badRequestErrorMsg, instanceOf(OXPBadRequestErrorMsgVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(badRequestErrorMsg.getClass()));

        OXPBadRequestErrorMsg messageRev = (OXPBadRequestErrorMsg) message;
        assertThat(badRequestErrorMsg, is(messageRev));
    }

    @Test
    public void OXPDomainConfigFailedErrorMsgTest() throws Exception{
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        OXPDomainConfigFailedErrorMsg domainConfigFailedErrorMsg = getMsgFactory().errorMsgs()
                .buildDomainConfigFailedErrorMsg()
                .setCode(OXPDomainConfigFaliedCode.BAD_FALGS)
                .setData(OXPErrorCauseData.NONE)
                .build();
        domainConfigFailedErrorMsg.writeTo(buffer);
        assertThat(domainConfigFailedErrorMsg, instanceOf(OXPDomainConfigFailedErrorMsgVer10.class));

        OXPMessage message = getMsgReader().readFrom(buffer);
        assertThat(message, instanceOf(domainConfigFailedErrorMsg.getClass()));

        OXPDomainConfigFailedErrorMsg messageRev = (OXPDomainConfigFailedErrorMsg) message;
        assertThat(domainConfigFailedErrorMsg, is(messageRev));
    }
}

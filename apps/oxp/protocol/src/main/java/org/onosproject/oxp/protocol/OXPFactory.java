package org.onosproject.oxp.protocol;


import org.onosproject.oxp.protocol.errormsg.OXPErrorMsgs;

import java.util.List;

/**
 * Created by cr on 16-4-9.
 */
public interface OXPFactory extends XidGenerator {

    //Subfactories
    OXPErrorMsgs errorMsgs();

    OXPHello.Builder buildHello();
    OXPHello hello(List<OXPHelloElem> elements);
    OXPEchoRequest.Builder buildEchoRequest();
    OXPEchoReply.Builder buildEchoReply();
    OXPFeaturesRequest.Builder buildFeaturesRequst();
    OXPFeaturesReply.Builder buildFeaturesReply();
    OXPGetConfigRequest.Builder buildGetConfigRequest();
    OXPGetConfigReply.Builder buildGetConfigReply();
    OXPSetConfig.Builder buildSetConfig();
    OXPTopologyRequest.Builder buildTopologyRequest();
    OXPTopologyReply.Builder buildTopologyReply();
    OXPHostRequest.Builder buildHostRequest();
    OXPHostReply.Builder buildHostReply();
    OXPHostUpdate.Builder buildHostUpdate();
    OXPVportStatus.Builder buildVportStatus();
    OXPSbp.Builder buildSbp();



    OXPMessageReader<OXPMessage> getReader();
    OXPVersion getVersion();
}

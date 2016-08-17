package org.onosproject.oxp.protocol.errormsg;

import org.onosproject.oxp.protocol.OXPErrorMsg;
import org.onosproject.oxp.protocol.OXPMessageReader;
import org.onosproject.oxp.protocol.OXPVersion;
import org.onosproject.oxp.protocol.XidGenerator;

/**
 * Created by cr on 16-7-17.
 */
public interface OXPErrorMsgs extends XidGenerator {
    //Subfactories

    OXPHelloFailedErrorMsg.Builder buildHelloFailedErrorMsg();
    OXPBadRequestErrorMsg.Builder buildBadRequestErrorMsg();
    OXPDomainConfigFailedErrorMsg.Builder buildDomainConfigFailedErrorMsg();
    OXPExperimenterErrorMsg.Builder buildExperimenterErrorMsg();

    OXPMessageReader<OXPErrorMsg> getReader();
    OXPVersion getVersion();
}

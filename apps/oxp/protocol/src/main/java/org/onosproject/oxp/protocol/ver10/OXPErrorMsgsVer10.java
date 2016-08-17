package org.onosproject.oxp.protocol.ver10;

import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.errormsg.*;

/**
 * Created by cr on 16-7-17.
 */
public class OXPErrorMsgsVer10 implements OXPErrorMsgs {
    public final static OXPErrorMsgsVer10 INSTANCE = new OXPErrorMsgsVer10();

    private final XidGenerator xidGenerator = XidGenerators.global();

    @Override
    public OXPHelloFailedErrorMsg.Builder buildHelloFailedErrorMsg() {
        return new OXPHelloFailedErrorMsgVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPBadRequestErrorMsg.Builder buildBadRequestErrorMsg() {
        return new OXPBadRequestErrorMsgVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPDomainConfigFailedErrorMsg.Builder buildDomainConfigFailedErrorMsg() {
        return new OXPDomainConfigFailedErrorMsgVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPExperimenterErrorMsg.Builder buildExperimenterErrorMsg() {
        return null;
    }

    @Override
    public OXPMessageReader<OXPErrorMsg> getReader() {
        return OXPErrorMsgVer10.READER;
    }

    @Override
    public OXPVersion getVersion() {
        return null;
    }

    @Override
    public long nextXid() {
        return xidGenerator.nextXid();
    }
}

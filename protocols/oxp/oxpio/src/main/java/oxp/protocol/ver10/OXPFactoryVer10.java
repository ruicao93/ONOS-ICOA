package oxp.protocol.ver10;

import org.onosproject.oxp.protocol.*;
import org.onosproject.oxp.protocol.errormsg.OXPErrorMsgs;

import java.util.List;

/**
 * Created by cr on 16-4-9.
 */
public class OXPFactoryVer10 implements OXPFactory {

    public static final OXPFactoryVer10 INSTANCE = new OXPFactoryVer10();

    private final XidGenerator xidGenerator = XidGenerators.global();

    @Override
    public OXPErrorMsgs errorMsgs() {
        return OXPErrorMsgsVer10.INSTANCE;
    }

    @Override
    public OXPHello.Builder buildHello() {
        return new OXPHelloVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPHello hello(List<OXPHelloElem> elements) {
        return new OXPHelloVer10(
                nextXid()
        );
    }

    @Override
    public OXPEchoRequest.Builder buildEchoRequest() {
        return new OXPEchoRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPEchoReply.Builder buildEchoReply() {
        return new OXPEchoReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPFeaturesRequest.Builder buildFeaturesRequst() {
        return new OXPFeaturesRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPFeaturesReply.Builder buildFeaturesReply() {
        return new OXPFeaturesReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPGetConfigRequest.Builder buildGetConfigRequest() {
        return new OXPGetConfigRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPGetConfigReply.Builder buildGetConfigReply() {
        return new OXPGetConfigReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPSetConfig.Builder buildSetConfig() {
        return new OXPSetConfigVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPTopologyRequest.Builder buildTopologyRequest() {
        return new OXPTopologyRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPTopologyReply.Builder buildTopologyReply() {
        return new OXPTopologyReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPSbp.Builder buildSbp() {
        return new OXPSbpVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPHostRequest.Builder buildHostRequest() {
        return new OXPHostRequestVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPHostReply.Builder buildHostReply() {
        return new OXPHostReplyVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPHostUpdate.Builder buildHostUpdate() {
        return new OXPHostUpdateVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPVportStatus.Builder buildVportStatus() {
        return new OXPVportStatusVer10.Builder().setXid(nextXid());
    }

    @Override
    public OXPMessageReader<OXPMessage> getReader() {
        return OXPMessageVer10.READER;
    }

    @Override
    public OXPVersion getVersion() {
        return OXPVersion.OXP_10;
    }

    @Override
    public long nextXid() {
        return xidGenerator.nextXid();
    }


}

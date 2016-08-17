package org.onosproject.oxp.protocol.ver10;

import org.onosproject.oxp.protocol.*;

/**
 * Created by cr on 16-7-18.
 */
abstract class TestBaseVer10 {
    final static OXPFactory oxpMsgFactory = OXPFactories.getFactory(OXPVersion.OXP_10);
    final static OXPMessageReader<OXPMessage> oxpMsgReader = OXPFactories.getGenericReader();

    public static OXPFactory getMsgFactory() {
        return oxpMsgFactory;
    }

    public static OXPMessageReader<OXPMessage> getMsgReader() {
        return oxpMsgReader;
    }
}

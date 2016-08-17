package org.onosproject.oxp;

import org.jboss.netty.channel.Channel;
import org.onosproject.oxp.protocol.OXPFactory;
import org.onosproject.oxp.protocol.OXPMessage;

import java.util.List;

/**
 * Created by cr on 16-8-14.
 */
public interface OxpSuper {

    void sendMsg(OXPMessage msg);

    void sendMsg(List<OXPMessage> msgs);

    void handleMessage(OXPMessage msg);

    OXPFactory factory();

    void setConnected(boolean isConnected);

    boolean isConnected();

    String channleId();

    void disconnectSuper();
    boolean connectSuper();

    void setChannel(Channel channel);

    int getNextTransactionId();

}

package org.onosproject.oxp;

/**
 * Created by cr on 16-8-17.
 */
public interface OxpSuperListener {

    void connectToSuper(OxpSuper oxpSuper);

    void disconnectFromSuper(OxpSuper oxpSuper);
}

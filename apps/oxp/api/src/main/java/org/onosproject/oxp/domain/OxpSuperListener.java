package org.onosproject.oxp.domain;

import org.onosproject.oxp.OxpSuper;

/**
 * Created by cr on 16-8-17.
 */
public interface OxpSuperListener {

    void connectToSuper(OxpSuper oxpSuper);

    void disconnectFromSuper(OxpSuper oxpSuper);
}

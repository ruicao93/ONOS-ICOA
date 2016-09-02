package org.onosproject.oxp.oxpsuper;

import org.onosproject.oxp.OXPDomain;

/**
 * Created by cr on 16-9-1.
 */
public interface OxpDomainListener {
    void domainConnected(OXPDomain domain);
    void domainDisconnected(OXPDomain domain);
}

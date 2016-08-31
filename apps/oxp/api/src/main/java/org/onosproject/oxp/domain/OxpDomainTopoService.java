package org.onosproject.oxp.domain;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.PortNumber;

/**
 * Created by cr on 16-8-20.
 */
public interface OxpDomainTopoService {

    PortNumber getLogicalVportNum(ConnectPoint connectPoint);

    boolean isOuterPort(ConnectPoint connectPoint);

    ConnectPoint getLocationByVport(PortNumber portNum);
}

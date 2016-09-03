package org.onosproject.oxp.impl.oxpsuper;

import com.google.common.collect.ImmutableList;
import org.apache.felix.scr.annotations.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.oxp.OxpDomainMessageListener;
import org.onosproject.oxp.OxpSuperMessageListener;
import org.onosproject.oxp.oxpsuper.OxpSuperController;
import org.onosproject.oxp.oxpsuper.OxpSuperTopoService;
import org.onosproject.oxp.protocol.OXPMessage;
import org.onosproject.oxp.protocol.OXPType;
import org.onosproject.oxp.protocol.OXPVportDesc;
import org.onosproject.oxp.protocol.OXPVportStatus;
import org.slf4j.Logger;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by cr on 16-9-3.
 */
@Component(immediate = true)
@Service
public class OxpSuperTopoManager implements OxpSuperTopoService {
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private OxpSuperController superController;

    private OxpDomainMessageListener domainMessageListener = new InternalDomainMessageListener();

    private Map<DeviceId, Set<PortNumber>> vportMap;
    private Map<DeviceId, Map<PortNumber, OXPVportDesc>> vportDescMaps;


    @Activate
    private void activate() {
        vportMap = new HashMap<>();
        vportDescMaps = new HashMap<>();
        superController.addMessageListener(domainMessageListener);
    }

    @Deactivate
    private void deactivate() {
        superController.removeMessageListener(domainMessageListener);
        vportMap.clear();
        vportDescMaps.clear();
    }

    @Override
    public List<PortNumber> getVports(DeviceId deviceId) {
        return ImmutableList.copyOf(vportMap.get(deviceId));
    }

    @Override
    public OXPVportDesc getVportDesc(DeviceId deviceId, PortNumber portNumber) {
        return null;
    }

    @Override
    public List<Link> getInterlinks() {
        return null;
    }

    @Override
    public List<Link> getIntraLinks(DeviceId deviceId) {
        return null;
    }

    private void addOrUpdateVport(DeviceId deviceId, OXPVportDesc vportDesc) {
        Set<PortNumber> vportSet = vportMap.get(deviceId);
        if (null == vportSet) {
            vportSet = new HashSet<>();
            vportMap.put(deviceId, vportSet);
        }
        PortNumber vportNum = PortNumber.portNumber(vportDesc.getPortNo().getPortNumber());
        vportSet.add(vportNum);
        Map<PortNumber, OXPVportDesc> vportDescMap = vportDescMaps.get(deviceId);
        if (null == vportDescMap) {
            vportDescMap = new HashMap<>();
            vportDescMaps.put(deviceId, vportDescMap);
        }
        vportDescMap.put(vportNum, vportDesc);
    }

    private void removeVport(DeviceId deviceId, PortNumber vportNum) {
        Set<PortNumber> vportSet = vportMap.get(deviceId);
        if (null == vportSet) {
            vportSet.remove(vportNum);
        }
        Map<PortNumber, OXPVportDesc> vportDescMap = vportDescMaps.get(deviceId);
        if (null == vportDescMap) {
            vportDescMaps.remove(vportNum);
        }
    }
    class InternalDomainMessageListener implements OxpDomainMessageListener {
        @Override
        public void handleIncomingMessage(DeviceId deviceId, OXPMessage msg) {
            if (msg.getType() != OXPType.OXPT_VPORT_STATUS) {
                return;
            }
            OXPVportStatus vportStatus = (OXPVportStatus) msg;
            switch (vportStatus.getReason()) {
                case ADD:
                case MODIFY:
                    addOrUpdateVport(deviceId, vportStatus.getVportDesc());
                    break;
                case DELETE:
                    removeVport(deviceId, PortNumber.portNumber(vportStatus.getVportDesc().getPortNo().getPortNumber()));

            }
        }

        @Override
        public void handleOutGoingMessage(DeviceId deviceId, List<OXPMessage> msgs) {

        }
    }
}

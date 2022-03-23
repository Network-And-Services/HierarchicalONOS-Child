/*
 * Copyright 2022-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sssup.multiinstance;

import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.event.Event;
import org.onosproject.mastership.MastershipInfo;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.topology.TopologyEvent;
import org.onosproject.net.topology.TopologyListener;
import org.onosproject.net.topology.TopologyService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Properties;

import static org.onlab.util.Tools.get;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true,
           service = {AppComponent.class},
           property = {
               "someProperty=Some Default String Value",
           })
public class AppComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());



    /** Some configurable property. */
    private String someProperty;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    private final TopologyListener myTopologyEventListener= new InternalTopologyListener();

    @Activate
    protected void activate() {
        //cfgService.registerProperties(getClass());
        topologyService.addListener(myTopologyEventListener);
        //deviceService.addListener(myEventListener);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        //cfgService.unregisterProperties(getClass(), false);
        topologyService.removeListener(myTopologyEventListener);
        //deviceService.removeListener(myEventListener);
        log.info("Stopped");
    }

    //@Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
        if (context != null) {
            someProperty = get(properties, "someProperty");
        }
        log.info("Reconfigured");
    }


    private boolean checkMembership(DeviceId device){
        MastershipInfo info = mastershipService.getMastershipFor(device);
        if(info.master().isEmpty()){
            log.info("Mastership not yet synced for "+ device);
            //TODO: Add the event to a pending queue, for future reprocessing.
        } else {
            return deviceService.getRole(device) == MastershipRole.MASTER;
        }
        return false;
    }

    private class InternalTopologyListener implements TopologyListener {

        @Override
        public void event(TopologyEvent event) {
            for (Event singleevent : event.reasons()){
                if (singleevent instanceof DeviceEvent){
                    DeviceId deviceid = ((Device) singleevent.subject()).id();
                    if (checkMembership(deviceid)){
                        if (DeviceEvent.Type.DEVICE_ADDED.equals(singleevent.type())) {
                            log.info("DEVICE: Added "+ singleevent);
                        }
                        else if (DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED.equals(singleevent.type())) {
                            if (!deviceService.isAvailable(deviceid)){
                                log.info("DEVICE: Unreachable "+ singleevent);
                            } else {
                                log.info("DEVICE: Reachable "+ singleevent);
                            }
                        } else {
                            log.info("DEVICE: Something " + singleevent);
                        }
                    }
                } else if (singleevent instanceof LinkEvent) {
                    if (checkMembership(((Link) singleevent.subject()).src().deviceId())){
                        if (LinkEvent.Type.LINK_ADDED.equals(singleevent.type())) {
                            log.info("LINK: Added " + singleevent);
                        }
                        else if (LinkEvent.Type.LINK_REMOVED.equals(singleevent.type())) {
                            log.info("LINK: Removed " + singleevent);
                        } else {
                            log.info("LINK: Something " + singleevent);
                        }
                    }
                } else {
                        log.info("Unknown event: "+singleevent);
                    }
                }
            }
        }

}
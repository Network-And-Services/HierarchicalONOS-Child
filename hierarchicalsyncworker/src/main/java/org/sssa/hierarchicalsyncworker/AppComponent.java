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

package org.sssa.hierarchicalsyncworker;

import java.util.ArrayList;
//import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.event.Event;
//import org.onosproject.mastership.MastershipEvent;
import org.onosproject.mastership.MastershipInfo;
import org.onosproject.mastership.MastershipService;
//import org.onosproject.mastership.MastershipListener;
import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.topology.TopologyEvent;
import org.onosproject.net.topology.TopologyListener;
import org.onosproject.net.topology.TopologyService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
import java.util.Dictionary;
import java.util.Properties;
import org.osgi.service.component.ComponentContext;
import static org.onlab.util.Tools.get;

//@Modified
public void modified(ComponentContext context) {
    Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
    if (context != null) {
        someProperty = get(properties, "someProperty");
    }
    log.info("Reconfigured");
}
*/

@Component(immediate = true,
           service = {AppComponent.class})
           /*
           property = {
               "someProperty=Some Default String Value",
           })
           */
public class AppComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Some configurable property. */
    //private String someProperty;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected MastershipService mastershipService;

    //@Reference(cardinality = ReferenceCardinality.MANDATORY)
    //protected ComponentConfigService cfgService;

    private final TopologyListener myTopologyEventListener= new InternalTopologyListener();

    @Activate
    protected void activate() {
        //cfgService.registerProperties(getClass());
        topologyService.addListener(myTopologyEventListener);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        //cfgService.unregisterProperties(getClass(), false);
        topologyService.removeListener(myTopologyEventListener);
        log.info("Stopped");
    }

    private boolean checkMembership(DeviceId device){
        MastershipInfo info = mastershipService.getMastershipFor(device);
        if(info.master().isEmpty()){
            log.info("Mastership not yet synced for "+ device + " ROLE: " + deviceService.getRole(device));
            //TODO: Add the event to a pending queue, for future reprocessing.
        } else {
            return deviceService.getRole(device) == MastershipRole.MASTER;
        }
        return false;
    }

    /**
    private ArrayList<DeviceId> getControlledDevices(){
        ArrayList<DeviceId> controlledDevices = new ArrayList<DeviceId>();
        for (Device device : deviceService.getDevices()){
            if (checkMembership(device.id())){
                controlledDevices.add(device.id());
            }
        }
        return controlledDevices;
    }
    

    private class InternalMastershipListener implements MastershipListener{
        @Override
        public void event(MastershipEvent event) {
            if (event.type().equals(MastershipEvent.Type.MASTER_CHANGED)){
                if (event.subject()
                //TODO: add or remove it from our local controlled devices
            }
        }
    }
    */

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
    
    private ArrayList<Device> reqDeviceList(){
        ArrayList<Device> deviceList = new ArrayList<Device>();
        for (Device device : deviceService.getAvailableDevices()){
            deviceList.add(device);
            //TODO: generate devices to be sent
        }
        return deviceList;
    }

    private ArrayList<Link> reqLinkList(){
        ArrayList<Link> linkList = new ArrayList<Link>();
        for (Link link : linkService.getLinks()){
            linkList.add(link);
            //TODO: generate devices to be sent
        }
        return linkList;
    }
}
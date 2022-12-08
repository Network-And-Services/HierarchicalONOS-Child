/**
 * Copyright 2016-present Open Networking Foundation
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

package org.onosproject.hierarchicalsyncworker.impl;

import org.onosproject.cluster.*;
import org.onosproject.hierarchicalsyncworker.api.EventConversionService;
import org.onosproject.hierarchicalsyncworker.api.GrpcEventStorageService;
import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.Port;
import org.onosproject.net.device.*;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;
@Component(immediate = true)
public class EventListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public int eventCounter = 0;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected EventConversionService eventConversionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LeadershipService leadershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcEventStorageService grpcEventStorageService;

    private final DeviceListener deviceListener = new InternalDeviceListener();
    private final LinkListener linkListener = new InternalLinkListener();
    private final LeadershipEventListener leadershipListener = new InternalLeadershipListener();
    protected ExecutorService eventExecutor;
    private static final String PUBLISHER_TOPIC = "WORK_QUEUE_PUBLISHER";
    private boolean topicLeader;
    private NodeId localNodeId;

    private boolean initialsync;
    @Activate
    protected void activate() {
        initialsync = true;
        eventExecutor = newSingleThreadScheduledExecutor(groupedThreads("onos/onosEvents", "events-%d", log));
        leadershipService.addListener(leadershipListener);
        localNodeId = clusterService.getLocalNode().id();
        topicLeader = false;
        leadershipService.runForLeadership(PUBLISHER_TOPIC);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        if(topicLeader){
            deviceService.removeListener(deviceListener);
            linkService.removeListener(linkListener);
        }
        leadershipService.removeListener(leadershipListener);
        leadershipService.withdraw(PUBLISHER_TOPIC);
        eventExecutor.shutdownNow();
        eventExecutor = null;

        log.info("Stopped");
    }

    private class InternalLeadershipListener implements LeadershipEventListener {
        @Override
        public void event(LeadershipEvent event) {
            if(event.subject().topic().equals(PUBLISHER_TOPIC)){
                boolean amItheLeader = Objects.equals(localNodeId,leadershipService.getLeader(PUBLISHER_TOPIC));
                if (initialsync && amItheLeader){
                    initialSync();
                }
                initialsync = false;
                if (amItheLeader != topicLeader){
                    topicLeader = amItheLeader;
                    if(topicLeader){
                        deviceService.addListener(deviceListener);
                        linkService.addListener(linkListener);
                    } else {
                        deviceService.removeListener(deviceListener);
                        linkService.removeListener(linkListener);
                    }
                    log.info("Leadership changed to: "+  amItheLeader);
                }
            }
        }
    }

    private void initialSync(){
        log.debug("STARTING INITIAL SYNC");
        for (Device device : deviceService.getAvailableDevices()){
            eventExecutor.execute(() -> {
                grpcEventStorageService.publishEvent(
                        eventConversionService.convertEvent(0, new DeviceEvent(DeviceEvent.Type.DEVICE_ADDED, device)));
            });
            for (Port port : deviceService.getPorts(device.id())) {
                eventExecutor.execute(() -> {
                    grpcEventStorageService.publishEvent(
                            eventConversionService.convertEvent(0,new DeviceEvent(DeviceEvent.Type.PORT_ADDED, device, port)));
                });
            }
        }
        for (Link link : linkService.getLinks()){
            eventExecutor.execute(() -> {grpcEventStorageService.publishEvent(
                    eventConversionService.convertEvent(0, new LinkEvent(LinkEvent.Type.LINK_ADDED, link)));});
        }
    }

    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {

            //TODO: Adjust this Temporal filter
            if (event.type().equals(DeviceEvent.Type.PORT_STATS_UPDATED)){
                return;
            }

            if (event.type().equals(DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED)){
                if (deviceService.isAvailable(event.subject().id())){
                    event = new DeviceEvent(DeviceEvent.Type.DEVICE_ADDED, event.subject());
                } else {
                    event = new DeviceEvent(DeviceEvent.Type.DEVICE_REMOVED, event.subject());
                }
            }
            DeviceEvent finalEvent = event;
            eventExecutor.execute(() -> {
                grpcEventStorageService.publishEvent(eventConversionService.convertEvent(Instant.now().toEpochMilli(),finalEvent));
            });
            log.debug("Pushed event {} to grpc storage", event);

        }
    }

    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            eventExecutor.execute(() -> {
                grpcEventStorageService.publishEvent(eventConversionService.convertEvent(Instant.now().toEpochMilli(), event));
            });
            log.debug("Pushed event {} to grpc storage", event);

        }
    }
}
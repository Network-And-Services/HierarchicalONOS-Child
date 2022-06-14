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
import org.onosproject.hierarchicalsyncworker.api.dto.OnosEvent;
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

    @Activate
    protected void activate() {

        eventExecutor = newSingleThreadScheduledExecutor(groupedThreads("onos/onosEvents", "events-%d", log));
        deviceService.addListener(deviceListener);
        linkService.addListener(linkListener);
        leadershipService.addListener(leadershipListener);

        localNodeId = clusterService.getLocalNode().id();
        topicLeader = false;
        leadershipService.runForLeadership(PUBLISHER_TOPIC);

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        deviceService.removeListener(deviceListener);
        linkService.removeListener(linkListener);
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
                if (amItheLeader != topicLeader){
                    topicLeader = amItheLeader;
                    log.info("Leadership changed to: "+  amItheLeader);
                }
            }
        }
    }

    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {

            if (!topicLeader) {
                log.debug("Not a Leader, cannot publish!");
                return;
            }

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
            printE2E();
            OnosEvent onosEvent = eventConversionService.convertEvent(event);
            eventExecutor.execute(() -> {
                grpcEventStorageService.publishEvent(onosEvent);
            });
            log.debug("Pushed event {} to grpc storage", onosEvent);

        }
    }

    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            if (!topicLeader) {
                log.info("Not a Leader, cannot publish!");
                return;
            }
            printE2E();
            OnosEvent onosEvent = eventConversionService.convertEvent(event);
            eventExecutor.execute(() -> {
                grpcEventStorageService.publishEvent(onosEvent);
            });
            log.debug("Pushed event {} to grpc storage", onosEvent);

        }
    }

    public void printE2E(){
        long now = Instant.now().toEpochMilli();
        log.error("EVENT Captured: "+now);
    }

}
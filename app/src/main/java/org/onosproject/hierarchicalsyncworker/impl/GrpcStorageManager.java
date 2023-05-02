/*
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

import com.google.protobuf.ByteString;
import org.onosproject.cluster.*;
import org.onosproject.grpc.net.device.models.DeviceEventProto;
import org.onosproject.hierarchicalsyncworker.api.GrpcClientService;
import org.onosproject.hierarchicalsyncworker.api.GrpcEventStorageService;
import org.onosproject.hierarchicalsyncworker.api.dto.OnosEvent;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WorkQueue;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import static org.onosproject.hierarchicalsyncworker.service.OsgiPropertyConstants.CLUSTER_NAME_DEFAULT;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;

@Component(service = GrpcEventStorageService.class)
public class GrpcStorageManager implements GrpcEventStorageService {
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LeadershipService leadershipService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ClusterService clusterService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcClientService grpcClientService;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final LeadershipEventListener leadershipListener = new InternalLeadershipListener();
    private static final String GRPC_WORK_QUEUE = "GRPC_WORK_QUEUE_WORKER";
    private final String contention = "PUBLISHER_WORKER";
    private NodeId localNodeId;
    protected ExecutorService eventExecutor;
    private WorkQueue<OnosEvent> queue;
    private boolean topicLeader;

    @Activate
    protected void activate() {
        localNodeId = clusterService.getLocalNode().id();
        leadershipService.addListener(leadershipListener);
        eventExecutor = newSingleThreadScheduledExecutor(groupedThreads("onos/onosEventsPublisher", "events-%d", log));
        queue = storageService.<OnosEvent>getWorkQueue(GRPC_WORK_QUEUE,
                                                       Serializer.using(KryoNamespaces.API,
                                                                        OnosEvent.class,
                                                                        OnosEvent.Type.class));
        leadershipService.runForLeadership(contention);
        log.info("Started");
    }

    public void runTasker(){
        queue.registerTaskProcessor(this::sendEvent, 1, eventExecutor);
        log.info("Starting tasker");
    }

    public void stopTasker(){
        queue.stopProcessing();
        log.info("Stopping tasker");
    }

    @Deactivate
    protected void deactivate() {
        leadershipService.removeListener(leadershipListener);
        if(topicLeader){
            stopTasker();
        }
        queue = null;
        leadershipService.withdraw(contention);
        eventExecutor.shutdownNow();
        eventExecutor = null;
        log.info("Stopped");
    }

    @Override
    public void publishEvent(OnosEvent e) {
        queue.addOne(e);
        log.debug("Published {} Event to Distributed Work Queue", e.type());
    }

    private void sendEvent(OnosEvent onosEvent) {
        if (onosEvent != null) {
            Hierarchical.Response response = null;
            while (response == null){
                response = grpcClientService.sendOverGrpc(onosEvent);
            }
            log.info("Event Type - {}, sent successfully.",
                    onosEvent.type());
        }
    }

    private class InternalLeadershipListener implements LeadershipEventListener {
        @Override
        public void event(LeadershipEvent event) {
            if(event.subject().topic().equals(contention)){
                boolean amItheLeader = Objects.equals(localNodeId,leadershipService.getLeader(contention));
                if (amItheLeader != topicLeader){
                    topicLeader = amItheLeader;
                    log.info("Leadership changed to: "+  amItheLeader);
                    if (topicLeader){
                        runTasker();
                    } else {
                        stopTasker();
                    }
                }
            }
        }
    }

}

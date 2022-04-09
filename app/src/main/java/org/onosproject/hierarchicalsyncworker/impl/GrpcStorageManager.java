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

import org.onosproject.hierarchicalsyncworker.api.GrpcEventStorageService;
import org.onosproject.hierarchicalsyncworker.api.dto.OnosEvent;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Task;
import org.onosproject.store.service.WorkQueue;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component(service = GrpcEventStorageService.class)
public class GrpcStorageManager implements GrpcEventStorageService {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String GRPC_WORK_QUEUE = "Grpc-Work-Queue";

    private WorkQueue<OnosEvent> queue;

    @Activate
    protected void activate() {
        queue = storageService.<OnosEvent>getWorkQueue(GRPC_WORK_QUEUE,
                                                       Serializer.using(KryoNamespaces.API,
                                                                        OnosEvent.class,
                                                                        OnosEvent.Type.class));

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        queue = null;
        log.info("Stopped");
    }

    @Override
    public void publishEvent(OnosEvent e) {
        queue.addOne(e);
        log.debug("Published {} Event to Distributed Work Queue", e.type());
    }

    @Override
    public OnosEvent consumeEvent() {
        Task<OnosEvent> task = null;

        CompletableFuture<Task<OnosEvent>> future = queue.take();
        try {
            task = future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.warn("consumeEvent()", e);
        }

        if (task != null) {
            queue.complete(task.taskId());
            log.info("Consumed {} Event from Distributed Work Queue with id {}",
                     task.payload().type(), task.taskId());
            return task.payload();
        }

        return null;
    }

}

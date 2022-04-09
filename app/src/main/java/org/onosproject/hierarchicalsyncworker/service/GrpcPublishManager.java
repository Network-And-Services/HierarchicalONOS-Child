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

package org.onosproject.hierarchicalsyncworker.service;

import org.onosproject.hierarchicalsyncworker.api.GrpcPublisherService;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a Kafka Producer.
 */
@Component(service = { GrpcPublisherService.class})
public class GrpcPublishManager implements GrpcPublisherService {
    private GrpcClientWorker clientWorker;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        start();
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        stop();
        log.info("Stopped");
    }


    public void start() {

        if (clientWorker != null) {
            log.info("Client Grpc has already started");
            return;
        }

        clientWorker = new GrpcClientWorker();

        log.info("Client Grpc has started.");
    }


    public void stop() {
        if (clientWorker != null) {
            clientWorker.deactivate();
            clientWorker = null;
        }
        log.info("Client Grpc has Stopped");
    }


    public void restart() {
        stop();
        start();
    }

    @Override
    public Hierarchical.Response send(Hierarchical.Request record) {
        return clientWorker.sendOverGrpc(record);
    }
}

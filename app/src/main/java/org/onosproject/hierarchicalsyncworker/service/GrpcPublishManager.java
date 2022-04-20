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
import org.onosproject.hierarchicalsyncworker.api.GrpcClientService;
import org.onosproject.hierarchicalsyncworker.api.GrpcPublisherService;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onosproject.hierarchicalsyncworker.service.OsgiPropertyConstants.MASTER_CLUSTER_ADDRESSES_DEFAULT;
/**
 * Implementation of a Kafka Producer.
 */
@Component(immediate = true, service = GrpcPublisherService.class)
public class GrpcPublishManager implements GrpcPublisherService {
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected GrpcClientService grpcClientService;

    private final Logger log = LoggerFactory.getLogger(getClass());
    @Activate
    protected void activate() {
        grpcClientService.start(MASTER_CLUSTER_ADDRESSES_DEFAULT);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        grpcClientService.stop();
        log.info("Stopped");
    }

    @Override
    public Hierarchical.Response send(Hierarchical.Request record) {
        if (!grpcClientService.isRunning()){
            grpcClientService.stop();
            grpcClientService.start(MASTER_CLUSTER_ADDRESSES_DEFAULT);
        }
        return grpcClientService.sendOverGrpc(record);
    }
}

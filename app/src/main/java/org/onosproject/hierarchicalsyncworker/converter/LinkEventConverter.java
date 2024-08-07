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
package org.onosproject.hierarchicalsyncworker.converter;

import com.google.protobuf.GeneratedMessageV3;
import org.onosproject.event.Event;
import org.onosproject.grpc.net.link.models.LinkEnumsProto.LinkEventTypeProto;
import org.onosproject.grpc.net.link.models.LinkEnumsProto.LinkStateProto;
import org.onosproject.grpc.net.link.models.LinkEnumsProto.LinkTypeProto;
import org.onosproject.grpc.net.link.models.LinkEventProto.LinkNotificationProto;
import org.onosproject.grpc.net.models.ConnectPointProtoOuterClass.ConnectPointProto;
import org.onosproject.grpc.net.models.LinkProtoOuterClass.LinkProto;
import org.onosproject.hierarchicalsyncworker.proto.Hierarchical;
import org.onosproject.hierarchicalsyncworker.service.OsgiPropertyConstants;
import org.onosproject.net.link.LinkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class LinkEventConverter implements EventConverter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public GeneratedMessageV3 convertToProtoMessage(Event<?, ?> event) {

        LinkEvent linkEvent = (LinkEvent) event;

        if (!linkEventTypeSupported(linkEvent)) {
            log.error("Unsupported Onos Event {}. There is no matching "
                              + "proto Event type", linkEvent.type().toString());
            return null;
        }
        LinkNotificationProto linkNotificationProto = buildDeviceProtoMessage(linkEvent);
        return Hierarchical.LinkRequest.newBuilder().setRequest(linkNotificationProto).
                setClusterid(OsgiPropertyConstants.CLUSTER_NAME_DEFAULT).build();
    }

    private boolean linkEventTypeSupported(LinkEvent event) {
        LinkEventTypeProto[] kafkaLinkEvents = LinkEventTypeProto.values();
        for (LinkEventTypeProto linkEventType : kafkaLinkEvents) {
            if (linkEventType.name().equals(event.type().name())) {
                return true;
            }
        }
        return false;
    }

    private LinkNotificationProto buildDeviceProtoMessage(LinkEvent linkEvent) {
        LinkNotificationProto notification = LinkNotificationProto.newBuilder()
                .setLinkEventType(getProtoType(linkEvent))
                .setLink(LinkProto.newBuilder()
                                 .setState(LinkStateProto.valueOf(linkEvent.subject().state().name()))
                                 .setType(LinkTypeProto.valueOf(linkEvent.subject().type().name()))
                                 .setDst(ConnectPointProto.newBuilder()
                                                 .setDeviceId(convertToVirtualDeviceID(linkEvent.subject().dst()
                                                                      .deviceId()))
                                                 .setPortNumber(linkEvent.subject().dst().port()
                                                                        .toString()))
                                 .setSrc(ConnectPointProto.newBuilder()
                                                 .setDeviceId(convertToVirtualDeviceID(linkEvent.subject().src()
                                                         .deviceId()))
                                                 .setPortNumber(linkEvent.subject().src().port()
                                                                        .toString())))
                .build();

        return notification;
    }

    private LinkEventTypeProto getProtoType(LinkEvent event) {
        LinkEventTypeProto generatedEventType = null;
        LinkEventTypeProto[] kafkaEvents = LinkEventTypeProto.values();
        for (LinkEventTypeProto linkEventType : kafkaEvents) {
            if (linkEventType.name().equals(event.type().name())) {
                generatedEventType = linkEventType;
            }
        }

        return generatedEventType;
    }
}

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
import static org.onosproject.hierarchicalsyncworker.converter.OnosEvent.Type.DEVICE;
import static org.onosproject.hierarchicalsyncworker.converter.OnosEvent.Type.LINK;

import java.util.HashMap;
import java.util.Map;

import org.onosproject.hierarchicalsyncworker.converter.DeviceEventConverter;
import org.onosproject.hierarchicalsyncworker.converter.EventConverter;
import org.onosproject.hierarchicalsyncworker.converter.LinkEventConverter;
import org.onosproject.hierarchicalsyncworker.converter.OnosEvent.Type;



/**
 * Returns the appropriate converter object based on the ONOS event type.
 *
 */
public final class ConversionFactory {

    // Store converters for all supported events
    private Map<Type, org.onosproject.hierarchicalsyncworker.converter.EventConverter> converters =
            new HashMap<Type, org.onosproject.hierarchicalsyncworker.converter.EventConverter>() {
                {
                    put(DEVICE, new DeviceEventConverter());
                    put(LINK, new LinkEventConverter());
                }
            };

    // Exists to defeat instantiation
    private ConversionFactory() {
    }

    private static class SingletonHolder {
        private static final org.onosproject.hierarchicalsyncworker.converter.ConversionFactory INSTANCE =
                new org.onosproject.hierarchicalsyncworker.converter.ConversionFactory();
    }

    /**
     * Returns a static reference to the Conversion Factory.
     *
     * @return singleton object
     */
    public static org.onosproject.hierarchicalsyncworker.converter.ConversionFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Returns an Event converter object for the specified ONOS event type.
     *
     * @param event ONOS event type
     * @return Event Converter object
     */
    public EventConverter getConverter(Type event) {
        return converters.get(event);
    }

}
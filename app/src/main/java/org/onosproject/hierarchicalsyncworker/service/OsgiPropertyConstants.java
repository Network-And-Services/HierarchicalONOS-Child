/*
 * Copyright 2018-present Open Networking Foundation
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

/**
 * Constants for default values of configurable properties.
 */
public final class OsgiPropertyConstants {
    //TODO: To be handled with configService
    private OsgiPropertyConstants() {
    }
    static final String MASTER_CLUSTER_ADDRESSES = "masterAddresses";
    //static final String[] MASTER_CLUSTER_ADDRESSES_DEFAULT = {"10.30.2.217:5908", "10.30.2.217:5909"};

    static final String[] MASTER_CLUSTER_ADDRESSES_DEFAULT = {"172.168.7.6:5908", "172.168.7.7:5908"};

}
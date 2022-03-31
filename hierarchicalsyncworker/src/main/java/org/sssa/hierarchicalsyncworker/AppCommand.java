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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.region.Region;
import org.onosproject.net.region.RegionService;

/**
 * Sample Apache Karaf CLI command.
 */



@Service
@Command(scope = "onos", name = "sample",
         description = "Sample Apache Karaf CLI command")
public class AppCommand extends AbstractShellCommand {

    @Override
    protected void doExecute() {
        RegionService regionService = get(RegionService.class);
        log.info(String.valueOf(regionService.getRegions().size()));
        for (Region i : regionService.getRegions()){
            log.info(i.toString());
        }
    }
}

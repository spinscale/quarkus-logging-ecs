/*
 * Copyright [2020] [Alexander Reelsen]
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
 *
 */

package de.spinscale.quarkus.logging.ecs.deployment;

import de.spinscale.quarkus.logging.ecs.runtime.EcsLoggingConfig;
import de.spinscale.quarkus.logging.ecs.runtime.EcsLoggingRecorder;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.LogConsoleFormatBuildItem;

public class QuarkusEcsLoggingSteps {

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public LogConsoleFormatBuildItem setUpFormatter(EcsLoggingRecorder recorder, EcsLoggingConfig config) {
        return new LogConsoleFormatBuildItem(recorder.initializeEcsLogging(config));
    }

}

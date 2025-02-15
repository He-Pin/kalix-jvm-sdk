/*
 * Copyright 2021 Lightbend Inc.
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

package kalix.javasdk.logging;

/**
 * This Logback JSON layout uses the name `severity` (instead of `level`).
 *
 * <p>Eg. Stackdriver expects the log severity to be in a field called `severity`.
 *
 * <p>IMPORTANT: This class depends on the "logback-json-classic" library (organization
 * "ch.qos.logback.contrib") and the Jackson layout support requires "logback-jackson" (organization
 * "ch.qos.logback.contrib") which need to be added as dependencies.
 */
public final class LogbackJsonLayout extends ch.qos.logback.contrib.json.classic.JsonLayout {

  public LogbackJsonLayout() {
    setIncludeLevel(false);
  }

  @Override
  public void addCustomDataToJsonMap(
      java.util.Map<String, Object> map, ch.qos.logback.classic.spi.ILoggingEvent event) {
    add("severity", true, String.valueOf(event.getLevel()), map);
  }
}

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

package com.example.wiring.workflowentities;

import com.example.wiring.eventsourcedentities.counter.Counter;
import com.example.wiring.eventsourcedentities.counter.CounterEvent;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Id("id")
@TypeId("failing-counter")
@RequestMapping("/failing-counter/{id}")
public class FailingCounterEntity extends EventSourcedEntity<Counter, CounterEvent> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final EventSourcedEntityContext context;

  public FailingCounterEntity(EventSourcedEntityContext context) {
    this.context = context;
  }

  @Override
  public Counter emptyState() {
    return new Counter(0);
  }

  @PostMapping("/increase/{value}")
  public Effect<Integer> increase(@PathVariable Integer value) {
    if (value % 3 != 0) {
      return effects().error("wrong value: " + value);
    } else {
      return effects().emitEvent(new CounterEvent.ValueIncreased(value)).thenReply(c -> c.value());
    }
  }

  @GetMapping
  public Effect<Integer> get() {
    return effects().reply(currentState().value());
  }

  @EventHandler
  public Counter handleIncrease(CounterEvent.ValueIncreased increased) {
    return currentState().onValueIncreased(increased);
  }

  @EventHandler
  public Counter handleMultiply(CounterEvent.ValueMultiplied multiplied) {
    return currentState().onValueMultiplied(multiplied);
  }

  @EventHandler
  public Counter handleSet(CounterEvent.ValueSet valueSet) {
    return currentState().onValueSet(valueSet);
  }
}

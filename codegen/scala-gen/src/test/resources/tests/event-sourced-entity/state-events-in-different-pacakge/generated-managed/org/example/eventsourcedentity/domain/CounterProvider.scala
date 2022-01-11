package org.example.eventsourcedentity.domain

import com.akkaserverless.scalasdk.eventsourcedentity.EventSourcedEntityContext
import com.akkaserverless.scalasdk.eventsourcedentity.EventSourcedEntityOptions
import com.akkaserverless.scalasdk.eventsourcedentity.EventSourcedEntityProvider
import com.google.protobuf.Descriptors
import com.google.protobuf.empty.EmptyProto
import org.example.eventsourcedentity.counter_api
import org.example.eventsourcedentity.events.counter_events.CounterEventsProto
import org.example.eventsourcedentity.state.counter_state.CounterState
import org.example.eventsourcedentity.state.counter_state.CounterStateProto

import scala.collection.immutable.Seq

// This code is managed by Akka Serverless tooling.
// It will be re-generated to reflect any changes to your protobuf definitions.
// DO NOT EDIT

object CounterProvider {
  def apply(entityFactory: EventSourcedEntityContext => Counter): CounterProvider =
    new CounterProvider(entityFactory, EventSourcedEntityOptions.defaults)
}
class CounterProvider private(entityFactory: EventSourcedEntityContext => Counter, override val options: EventSourcedEntityOptions)
  extends EventSourcedEntityProvider[CounterState, Counter] {

  def withOptions(newOptions: EventSourcedEntityOptions): CounterProvider =
    new CounterProvider(entityFactory, newOptions)

  override final val serviceDescriptor: Descriptors.ServiceDescriptor =
    counter_api.CounterApiProto.javaDescriptor.findServiceByName("CounterService")

  override final val entityType: String = "counter"

  override final def newRouter(context: EventSourcedEntityContext): CounterRouter =
    new CounterRouter(entityFactory(context))

  override final val additionalDescriptors: Seq[Descriptors.FileDescriptor] =
    CounterStateProto.javaDescriptor ::
    counter_api.CounterApiProto.javaDescriptor ::
    EmptyProto.javaDescriptor ::
    CounterEventsProto.javaDescriptor :: Nil
}


package com.example.replicated.multimap.multi_map_api

import com.akkaserverless.scalasdk.replicatedentity.ReplicatedEntity
import com.akkaserverless.scalasdk.replicatedentity.ReplicatedEntityContext
import com.akkaserverless.scalasdk.replicatedentity.ReplicatedMultiMap
import com.example.replicated.multimap.domain.multi_map_domain.SomeKey
import com.example.replicated.multimap.domain.multi_map_domain.SomeValue
import com.example.replicated.multimap.multi_map_api
import com.google.protobuf.empty.Empty

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** A replicated entity. */
class MultiMapServiceEntity(context: ReplicatedEntityContext) extends AbstractMultiMapServiceEntity {


  /** Command handler for "Put". */
  def put(currentData: ReplicatedMultiMap[SomeKey, SomeValue], putValue: PutValue): ReplicatedEntity.Effect[Empty] =
    effects.error("The command handler for `Put` is not implemented, yet")

}

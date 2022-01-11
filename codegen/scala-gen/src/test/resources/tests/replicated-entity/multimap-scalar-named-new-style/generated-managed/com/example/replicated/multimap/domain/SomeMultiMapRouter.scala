package com.example.replicated.multimap.domain

import com.akkaserverless.javasdk.impl.replicatedentity.ReplicatedEntityRouter.CommandHandlerNotFound
import com.akkaserverless.scalasdk.impl.replicatedentity.ReplicatedEntityRouter
import com.akkaserverless.scalasdk.replicatedentity.CommandContext
import com.akkaserverless.scalasdk.replicatedentity.ReplicatedEntity
import com.akkaserverless.scalasdk.replicatedentity.ReplicatedMultiMap
import com.example.replicated.multimap.multi_map_api

// This code is managed by Akka Serverless tooling.
// It will be re-generated to reflect any changes to your protobuf definitions.
// DO NOT EDIT

/**
 * A replicated entity handler that is the glue between the Protobuf service `MultiMapService`
 * and the command handler methods in the `SomeMultiMap` class.
 */
class SomeMultiMapRouter(entity: SomeMultiMap)
  extends ReplicatedEntityRouter[ReplicatedMultiMap[String, Double], SomeMultiMap](entity) {

  override def handleCommand(
      commandName: String,
      data: ReplicatedMultiMap[String, Double],
      command: Any,
      context: CommandContext): ReplicatedEntity.Effect[_] = {

    commandName match {
      case "Put" =>
        entity.put(data, command.asInstanceOf[multi_map_api.PutValue])

      case _ =>
        throw new CommandHandlerNotFound(commandName)
    }
  }
}

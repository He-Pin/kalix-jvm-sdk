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

import com.example.wiring.actions.echo.Message;
import com.example.wiring.workflowentities.FraudDetectionResult.TransferRejected;
import com.example.wiring.workflowentities.FraudDetectionResult.TransferRequiresManualAcceptation;
import com.example.wiring.workflowentities.FraudDetectionResult.TransferVerified;
import kalix.javasdk.client.ComponentClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.workflow.Workflow;


import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Id("transferId")
@TypeId("transfer-workflow-with-fraud-detection")
@RequestMapping("/transfer-with-fraud-detection/{transferId}")
public class TransferWorkflowWithFraudDetection extends Workflow<TransferState> {

  private final String fraudDetectionStepName = "fraud-detection";
  private final String withdrawStepName = "withdraw";
  private final String depositStepName = "deposit";

  private ComponentClient componentClient;

  public TransferWorkflowWithFraudDetection(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Override
  public WorkflowDef<TransferState> definition() {
    var fraudDetection =
        step(fraudDetectionStepName)
            .asyncCall(Transfer.class, this::checkFrauds)
            .andThen(FraudDetectionResult.class, this::processFraudDetectionResult);

    var withdraw =
        step(withdrawStepName)
            .call(Withdraw.class, cmd ->
                componentClient.forValueEntity(cmd.from).call(WalletEntity::withdraw).params(cmd.amount))
            .andThen(String.class, this::moveToDeposit);

    var deposit =
        step(depositStepName)
            .call(Deposit.class, cmd -> componentClient.forValueEntity(cmd.to).call(WalletEntity::deposit).params(cmd.amount))
            .andThen(String.class, this::finishWithSuccess);

    return workflow()
        .addStep(fraudDetection)
        .addStep(withdraw)
        .addStep(deposit);
  }

  @PutMapping
  public Effect<Message> startTransfer(@RequestBody Transfer transfer) {
    if (transfer.amount <= 0) {
      return effects().error("Transfer amount should be greater than zero");
    } else {
      if (currentState() == null) {
        return effects()
            .updateState(new TransferState(transfer, "started"))
            .transitionTo(fraudDetectionStepName, transfer)
            .thenReply(new Message("transfer started"));
      } else {
        return effects().reply(new Message("transfer already started"));
      }
    }
  }

  @PatchMapping("/accept")
  public Effect<Message> acceptTransfer() {
    if (currentState() == null) {
      return effects().reply(new Message("transfer not started"));
    } else if (!currentState().accepted && !currentState().finished) {
      var withdrawInput = new Withdraw(currentState().transfer.from, currentState().transfer.amount);
      return effects()
          .updateState(currentState().accepted())
          .transitionTo(withdrawStepName, withdrawInput)
          .thenReply(new Message("transfer accepted"));
    } else {
      return effects().reply(new Message("transfer cannot be accepted"));
    }
  }

  @GetMapping
  public Effect<TransferState> getTransferState() {
    if (currentState() == null) {
      return effects().error("transfer not started");
    } else {
      return effects().reply(currentState());
    }
  }

  private Effect.TransitionalEffect<Void> finishWithSuccess(String response) {
    var state = currentState().withLastStep(depositStepName);
    return effects().updateState(state).end();
  }

  private Effect.TransitionalEffect<Void> moveToDeposit(String response) {
    var state = currentState().withLastStep(withdrawStepName);

    var depositInput = new Deposit(currentState().transfer.to, currentState().transfer.amount);

    return effects()
        .updateState(state)
        .transitionTo(depositStepName, depositInput);
  }

  private CompletionStage<FraudDetectionResult> checkFrauds(Transfer transfer) {
    if (transfer.amount >= 1000 && transfer.amount < 1000000) {
      return completedFuture(new TransferRequiresManualAcceptation(transfer));
    } else if (transfer.amount >= 1000000) {
      return completedFuture(new TransferRejected(transfer));
    } else {
      return completedFuture(new TransferVerified(transfer));
    }
  }

  private Effect.TransitionalEffect<Void> processFraudDetectionResult(FraudDetectionResult result) {
    var state = currentState().withLastStep(fraudDetectionStepName);

    if (result instanceof TransferVerified) {
      var transferVerified = (TransferVerified) result;
      var withdrawInput = new Withdraw(transferVerified.transfer.from, transferVerified.transfer.amount);

      return effects()
          .updateState(state)
          .transitionTo(withdrawStepName, withdrawInput);

    } else if (result instanceof TransferRequiresManualAcceptation) {
      return effects()
          .updateState(state)
          .pause();
    } else if (result instanceof TransferRejected) {
      return effects()
          .updateState(state.finished())
          .end();
    } else {
      throw new IllegalStateException("not supported response" + result);
    }
  }
}

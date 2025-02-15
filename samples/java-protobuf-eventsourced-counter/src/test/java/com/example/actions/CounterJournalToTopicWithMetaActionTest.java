package com.example.actions;

import akka.stream.javadsl.Source;
import com.example.actions.CounterJournalToTopicWithMetaAction;
import com.example.actions.CounterJournalToTopicWithMetaActionTestKit;
import com.example.actions.CounterTopicApi;
import com.example.domain.CounterDomain;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class CounterJournalToTopicWithMetaActionTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    CounterJournalToTopicWithMetaActionTestKit service = CounterJournalToTopicWithMetaActionTestKit.of(CounterJournalToTopicWithMetaAction::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void onIncreasedTest() {
    CounterJournalToTopicWithMetaActionTestKit testKit = CounterJournalToTopicWithMetaActionTestKit.of(CounterJournalToTopicWithMetaAction::new);
    // ActionResult<CounterTopicApi.Increased> result = testKit.onIncreased(CounterDomain.ValueIncreased.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void onDecreasedTest() {
    CounterJournalToTopicWithMetaActionTestKit testKit = CounterJournalToTopicWithMetaActionTestKit.of(CounterJournalToTopicWithMetaAction::new);
    // ActionResult<CounterTopicApi.Decreased> result = testKit.onDecreased(CounterDomain.ValueDecreased.newBuilder()...build());
  }

}

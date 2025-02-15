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

package kalix.scalasdk.testkit

import io.grpc.Status

import scala.reflect.ClassTag

/**
 * Represents the result of an EventSourcedEntity handling a command when run in through the testkit.
 *
 * <p>Not for user extension, returned by the generated testkit.
 *
 * @tparam R
 *   The type of reply that is expected from invoking command handler
 */
trait EventSourcedResult[R] {

  /** @return true if the call had an effect with a reply, false if not */
  def isReply: Boolean

  /**
   * The reply object from the handler if there was one. If the call had an effect without any reply an exception is
   * thrown
   */
  def reply: R

  /** @return true if the call was forwarded, false if not */
  def isForward: Boolean

  /**
   * An object with details about the forward. If the result was not a forward an exception is thrown
   */
  def forwardedTo: DeferredCallDetails[_, R]

  /** @return true if the call was an error, false if not */
  def isError: Boolean

  /** The error description. If the result was not an error an exception is thrown */
  def errorDescription: String

  /** The error status code. If the result was not an error an exception is thrown. */
  def errorStatusCode: Status.Code

  /** @return The updated state. If the state was not updated an exeption is thrown */
  def updatedState: Any

  def didEmitEvents: Boolean

  /** @return All the events that were emitted by handling this command. */
  def events: Seq[Any]

  /**
   * Look at the next event and verify that it is of type E or fail if not or if there is no next event. If successful
   * this consumes the event, so that the next call to this method looks at the next event from here.
   *
   * @return
   *   The next event if it is of type E, for additional assertions.
   */
  def nextEvent[E](implicit expectedClass: ClassTag[E]): E

  /** @return The list of side effects */
  def sideEffects: Seq[DeferredCallDetails[_, _]]
}

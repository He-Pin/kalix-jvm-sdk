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

package kalix.javasdk.impl

import akka.actor.ActorSystem
import akka.actor.ClassicActorSystemProvider
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import kalix.protocol.discovery.IdentificationInfo
import kalix.protocol.discovery.ProxyInfo
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicReference

object ProxyInfoHolder extends ExtensionId[ProxyInfoHolder] with ExtensionIdProvider {
  override def get(system: ActorSystem): ProxyInfoHolder = super.get(system)

  override def get(system: ClassicActorSystemProvider): ProxyInfoHolder = super.get(system)

  override def createExtension(system: ExtendedActorSystem): ProxyInfoHolder =
    new ProxyInfoHolder(system)
  override def lookup: ExtensionId[_ <: Extension] = this
}

class ProxyInfoHolder(system: ExtendedActorSystem) extends Extension {

  private val log = LoggerFactory.getLogger(classOf[ProxyInfoHolder])

  private val _proxyHostname = new AtomicReference[String]()
  private val _proxyPort = new AtomicReference[Int](-1)
  @volatile private var _identificationInfo: Option[IdentificationInfo] = None

  def setProxyInfo(proxyInfo: ProxyInfo): Unit = {

    val chosenProxyName =
      if (proxyInfo.internalProxyHostname.isEmpty) {
        // for backward compatibility with proxy 1.0.14 or older
        proxyInfo.proxyHostname
      } else {
        proxyInfo.internalProxyHostname
      }

    // don't set if already overridden (by testkit)
    _proxyHostname.compareAndSet(null, chosenProxyName)
    _proxyPort.compareAndSet(-1, proxyInfo.proxyPort)
    _identificationInfo = proxyInfo.identificationInfo

    log.debug("Proxy hostname: [{}]", chosenProxyName)
    log.debug("Proxy port to: [{}]", proxyInfo.proxyPort)
    log.debug("Identification name: [{}]", proxyInfo.identificationInfo)
  }

  def proxyHostname: Option[String] = Option(_proxyHostname.get())

  def identificationInfo: Option[IdentificationInfo] = _identificationInfo

  def proxyPort: Option[Int] = {
    // If portOverride is filled, we choose it. Otherwise we use the announced one.
    // Note: with old proxy versions `proxyInfo.proxyPort` will default to 0
    val chosenPort = _proxyPort.get()

    // We should never return the default Int 0, so we make it a None
    // This can happen if somehow this method called before we receive the ProxyInfo
    // or if an old version of the proxy is being used
    if (chosenPort != 0) Some(chosenPort)
    else None
  }

  def localIdentificationHeader: Option[(String, String)] =
    identificationInfo.collect {
      case IdentificationInfo(header, token, _, _, _) if header.nonEmpty && token.nonEmpty => (header, token)
    }

  def remoteIdentificationHeader: Option[(String, String)] =
    identificationInfo.collect {
      case IdentificationInfo(_, _, header, name, _) if header.nonEmpty && name.nonEmpty => (header, name)
    }

  /**
   * Change port disregarding what is announced in ProxyInfo This is required for the testkit because the port to use is
   * defined by testcontainers
   *
   * INTERNAL API
   */
  private[kalix] def overridePort(port: Int): Unit =
    _proxyPort.set(port)

  /**
   * INTERNAL API
   */
  private[kalix] def overrideProxyHost(host: String): Unit =
    _proxyHostname.set(host)
}

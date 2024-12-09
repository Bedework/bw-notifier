/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.notifier.cnctrs;

/** A cache for connectors and instances. When values are successfully retrieved
 * from the cache the successfulFetch method should be called to update the last
 * fetch time.
 *
 * <p>These caches will be attached to subscriptions.
 *
 * <p>The system map periodically flush caches and will do so according to the
 * time since a cache was last successfully used.
 *
 * @author Mike Douglass
 */
public class ConnectorCache {
  private final String name;

  private long lastFetch;

  /**
   * @param name - name for the cache
   */
  public ConnectorCache(final String name) {
    this.name = name;
  }

  /**
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * @return last fetch time
   */
  public long getLastFetch() {
    return lastFetch;
  }

  /**
   */
  public void successfulFetch() {
    lastFetch = System.currentTimeMillis();
  }
}

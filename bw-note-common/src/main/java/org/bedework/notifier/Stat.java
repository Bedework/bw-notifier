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
package org.bedework.notifier;

import java.io.Serializable;

/** Provide a way to get named values.
 *
 * @author douglm
 */
public class Stat implements Serializable {
  private String name;
  private String value;

  /**
   * @param name of stat
   * @param value value
   */
  public Stat(final String name,
                     final String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * @param name of stat
   * @param value value
   */
  public Stat(final String name,
                     final long value) {
    this.name = name;
    this.value = String.valueOf(value);
  }

  /**
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * @return value
   */
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    sb.append(getName());
    sb.append(" = ");
    sb.append(getValue());
    sb.append("\n");

    return sb.toString();
  }
}

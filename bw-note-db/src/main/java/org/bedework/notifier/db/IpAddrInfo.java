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
package org.bedework.notifier.db;

import org.bedework.util.misc.ToString;

/** Let us block or allow addresses.
 *
 *  @version 1.0
 */
public class IpAddrInfo extends DbItem<IpAddrInfo> {
  private int order;

  private String addr;

  private boolean block;

  /** Constructor
   */
  public IpAddrInfo() {
    super();
  }

  /** Set the order - allows ordering of values
   *
   * @param val    int order
   */
  public void setOrder(final int val) {
    order = val;
  }

  /** Get the order
   *
   * @return int   order
   */
  public int getOrder() {
    return order;
  }

  /** Set the addr - a specific address or subnet in CIDR notation
   *
   * @param val    String addr
   */
  public void setAddr(final String val) {
    addr = val;
  }

  /** Get the addr
   *
   * @return String   name
   */
  public String getAddr() {
    return addr;
  }

  /** Set the block flag
   *
   * @param val    boolean block
   */
  public void setBlock(final boolean val) {
    block = val;
  }

  /** Get the value
   *
   *  @return boolean   block
   */
  public boolean getBlock() {
    return block;
  }

  /* ====================================================================
   *                        Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final IpAddrInfo that) {
    if (that == this) {
      return 0;
    }

    if (that == null) {
      return -1;
    }

    if (getOrder() < that.getOrder()) {
      return -1;
    }

    if (getOrder() > that.getOrder()) {
      return 1;
    }

    return 0;
  }

  @Override
  public int hashCode() {
    return getOrder();
  }

  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
    ts.append("order=", getOrder());
    ts.append("addr", getAddr());
    ts.append("block", getBlock());
  }

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}

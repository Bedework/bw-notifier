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
package org.bedework.notifier.outbound.common;

import org.bedework.caldav.util.notifications.ProcessorType;
import org.bedework.caldav.util.notifications.ProcessorsType;
import org.bedework.notifier.exception.NoteException;
import org.bedework.notifier.notifications.Note;
import org.bedework.util.http.HttpUtil;

import net.fortuna.ical4j.model.property.DtStamp;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

/** Some useful methods..
 *
 * @author Mike Douglass
 *
 */
public abstract class AbstractAdaptor<Conf extends AdaptorConf>
        implements Adaptor<Conf> {
	private transient Logger log;

  protected boolean debug;

	private final static AtomicLong nextId = new AtomicLong();

	private final Long id;

	protected Conf conf;

	protected AbstractAdaptor() {
		debug = getLogger().isDebugEnabled();
    id = nextId.incrementAndGet();
	}

	public long getId() {
		return id;
	}

	public void setConf(final Conf conf) {
		this.conf = conf;
	}

	public Conf getConfig() {
		return conf;
	}

	public String getType() {
		return conf.getType();
	}

  /**
   * @param note the notification to process
   * @return true if processed OK
   * @throws org.bedework.notifier.exception.NoteException
   */
	public boolean process(final Note note) throws NoteException {
		switch (note.getKind()) {
		case sharingInvitation:
			return processSharingInvitation(note);
		case subscribeInvitation:
			return processSubscribeInvitation(note);
		case resourceChange:
			return processResourceChange(note);
		}
		return false;
	}

  /**
   * @param note the notification
   * @return true if processed OK
   * @throws org.bedework.notifier.exception.NoteException
   */
	public abstract boolean processSharingInvitation(final Note note) throws NoteException;

  /**
   * @param note the notification
   * @return true if processed OK
   * @throws org.bedework.notifier.exception.NoteException
   */
	public abstract boolean processSubscribeInvitation(final Note note) throws NoteException;

  /**
   * @param note the notification
   * @return true if processed OK
   * @throws org.bedework.notifier.exception.NoteException
   */
	public abstract boolean processResourceChange(final Note note) throws NoteException;

	/* ====================================================================
	 *                   Protected methods
	 * ==================================================================== */

  protected String getDtstamp() {
    return new DtStamp().getValue();
  }

  protected ProcessorType getProcessorStatus(final Note note) {
    ProcessorsType pst = note.getNotification().getProcessors();
    ProcessorType pt = null;

    if (pst != null) {
      for (final ProcessorType notePt: pst.getProcessor()) {
        // TODO Define standard types?
        if (notePt.getType().equals("email")) {
          pt = notePt;
          break;
        }
      }
    }

    if (pt != null) {
      return pt;
    }

    if (pst == null) {
      pst = new ProcessorsType();
      note.getNotification().setProcessors(pst);
    }
    pt = new ProcessorType();
    pt.setType("email");

    pst.getProcessor().add(pt);

    return pt;
  }

  protected boolean processed(final ProcessorType pt) {
    if (pt.getStatus() == null) {
      return false;
    }

    try {
      final int scode = HttpUtil.getHttpStatus(pt.getStatus()).getStatusCode();

      return scode == HttpServletResponse.SC_OK;
    } catch (final Throwable t) {
      warn("Bad status: " + pt.getStatus());
      return false;
    }
  }

  protected String stripMailTo(final String address) {
    return address.replaceAll("^mailto:", "");
  }

  protected void info(final String msg) {
		getLogger().info(msg);
	}

	protected void trace(final String msg) {
		getLogger().debug(msg);
	}

	protected void error(final Throwable t) {
		getLogger().error(this, t);
	}

	protected void error(final String msg) {
		getLogger().error(msg);
	}

	protected void warn(final String msg) {
		getLogger().warn(msg);
	}

	/* Get a logger for messages
	 */
	protected Logger getLogger() {
		if (log == null) {
			log = Logger.getLogger(this.getClass());
		}

		return log;
	}
}

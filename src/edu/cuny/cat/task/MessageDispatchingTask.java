/*
 * JCAT - TAC Market Design Competition Platform
 * Copyright (C) 2006-2010 Jinzhong Niu, Kai Cai
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package edu.cuny.cat.task;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.util.Utils;

/**
 * <p>
 * A task of dispatching a CATP message.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public abstract class MessageDispatchingTask extends DispatchingTask {

	static Logger logger = Logger.getLogger(MessageDispatchingTask.class);

	protected CatpMessage message;

	protected String clientId;

	public MessageDispatchingTask(final CatpMessage message, final String clientId) {
		this.message = message;
		this.clientId = clientId;
	}

	public CatpMessage getMessage() {
		return message;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += " (" + clientId + ")";
		s += "\n" + Utils.indent(message.getStartLine());
		return s;
	}
}

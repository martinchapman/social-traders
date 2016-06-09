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

package edu.cuny.cat.server;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatpMessage;

/**
 * <p>
 * defines the various possible status of a catp client.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class ClientState {

	/**
	 * codes that indicate the status of a catp client
	 */

	/**
	 * status is unknown
	 */
	public final static int UNKNOWN = -1;

	/**
	 * new connection ready for messaging
	 */
	public final static int READY = 0;

	/**
	 * connection closed normally
	 */
	public final static int CONN_CLOSED = 1;

	/**
	 * being active
	 */
	public final static int OK = 4;

	/**
	 * common error occurred
	 */
	public final static int ERROR = 5;

	/**
	 * fatal error occurred and cannot be restored
	 */
	public final static int FATAL = 6;

	/**
	 * the code
	 */
	protected int code;

	/**
	 * description of the cause
	 */
	protected String description;

	static Logger logger = Logger.getLogger(ClientState.class);

	public ClientState(final int code) {
		this(code, null);
	}

	public ClientState(final int code, final String description) {
		this.code = code;
		this.description = description;
	}

	public int getCode() {
		return code;
	}

	public static String getCodeDesc(final int code) {
		switch (code) {
		case UNKNOWN:
			return "UNKNOWN";
		case OK:
			return CatpMessage.OK;
		case ERROR:
			return CatpMessage.ERROR;
		case FATAL:
			return "FATAL";
		case CONN_CLOSED:
			return "CONN_CLOSED";
		case READY:
			return "READY";
		default:
			ClientState.logger.error("Invalid code for client state !");
			return null;
		}
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return ClientState.getCodeDesc(code);
	}
}

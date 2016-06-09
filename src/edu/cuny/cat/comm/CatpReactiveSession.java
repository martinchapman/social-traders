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

package edu.cuny.cat.comm;

/**
 * <p>
 * This class processes a request/response session initiated by the other party
 * over a capt connection.
 * </p>
 * 
 * <p>
 * For a reactive party, several different sessions may be expected at a moment.
 * Detection on which sessions a coming request belongs to is done by trying
 * {@link #processRequest(CatpRequest)}, which should set {@link #processed} to
 * true if a session is certain it is the right one to process the request, or
 * false otherwise. {@link CatException} may be thrown thrown by the right
 * session if anything wrong with the request.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */

public abstract class CatpReactiveSession extends Session<CatpMessage>
		implements Cloneable {

	protected String reqType;

	protected String typeHeader;

	/**
	 * indicates whether or not this session has processed the request and sent a
	 * response.
	 * 
	 * @see #processRequest(CatpRequest)
	 */
	protected boolean processed;

	public CatpReactiveSession(final Connection<CatpMessage> connection,
			final String reqType) {
		this(connection, reqType, null);
	}

	public CatpReactiveSession(final Connection<CatpMessage> connection,
			final String reqType, final String typeHeader) {
		super(connection);
		this.reqType = reqType;
		this.typeHeader = typeHeader;
		processed = false;
	}

	public void processRequest(final CatpRequest request) throws CatException {
		if (request == null) {

			setProcessed(true);

			if (typeHeader != null) {
				throw new CatpMessageErrorException("Empty request received while "
						+ reqType + " " + typeHeader + " request expected!");
			} else {
				throw new CatpMessageErrorException("Empty request received while "
						+ reqType + " request expected !");
			}
		}

		if (request.getType().equalsIgnoreCase(reqType)) {

			// NOTE: assume at any time only one session is associated with a type of
			// request; otherwise it should be false
			setProcessed(true);

			if (typeHeader != null) {
				if (!typeHeader.equalsIgnoreCase(request.getHeader(CatpMessage.TYPE))) {

					setProcessed(false);

					throw new CatpMessageErrorException("Unexpected " + reqType
							+ request.getHeader(CatpMessage.TYPE) + " request received !");
				}
			}
		} else {
			throw new CatpMessageErrorException("Unexpected " + request.getType()
					+ " request received !");
		}
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(final boolean processed) {
		this.processed = processed;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}

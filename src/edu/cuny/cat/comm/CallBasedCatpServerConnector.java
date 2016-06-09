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

import org.apache.log4j.Logger;

/**
 * <p>
 * The implementation of {@link ServerConnector} when
 * {@link CallBasedInfrastructureImpl} is used.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class CallBasedCatpServerConnector implements
		ServerConnector<CatpMessage> {

	static Logger logger = Logger.getLogger(CallBasedCatpServerConnector.class);

	protected CallBasedInfrastructureImpl infrast;

	protected boolean closed;

	public CallBasedCatpServerConnector() {
		infrast = CallBasedInfrastructureImpl.getInstance();
		closed = false;
	}

	public Connection<CatpMessage> accept() throws ConnectionException {
		return infrast.acceptClient(this);
	}

	public void close() throws ConnectionException {
		closed = true;
		infrast.freeServerConnector(this);
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

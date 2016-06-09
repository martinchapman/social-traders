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
 * The implementation of {@link ClientConnector} when
 * {@link CallBasedInfrastructureImpl} is used.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class CallBasedCatpClientConnector implements
		ClientConnector<CatpMessage> {

	static Logger logger = Logger.getLogger(CallBasedCatpClientConnector.class);

	CallBasedInfrastructureImpl infrast;

	public CallBasedCatpClientConnector() {
		infrast = CallBasedInfrastructureImpl.getInstance();
	}

	/**
	 * @return an instance of {@link CallBasedCatpConnection}.
	 * @throws ConnectionException
	 */
	public Connection<CatpMessage> connect() throws ConnectionException {
		return infrast.connectToServer(this);
	}
}

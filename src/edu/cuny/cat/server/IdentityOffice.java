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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.util.IdAllocator;

/**
 * A facility allocates all types of unique IDs.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.16 $
 */
public class IdentityOffice {

	static Logger logger = Logger.getLogger(IdentityOffice.class);

	protected Map<String, IdAllocator> idAllocators;

	public IdentityOffice() {
		idAllocators = Collections
				.synchronizedMap(new HashMap<String, IdAllocator>());
	}

	public String createIdentity(final String type) {
		// if (isValidClientType(type) || isValidActionType(type)) {
		return type + "_" + newId(type);
		// } else {
		// String s = "Invalid type : " + type + " !";
		// logger.error(s);
		// }
	}

	public static boolean isValidClientType(final String type) {
		return (type.toLowerCase().startsWith(CatpMessage.SELLER.toLowerCase())
				|| type.toLowerCase().startsWith(CatpMessage.BUYER.toLowerCase()) || type
				.toLowerCase().startsWith(CatpMessage.SPECIALIST.toLowerCase()));
	}

	public static boolean isValidActionType(final String type) {
		return (type.toLowerCase().startsWith(CatpMessage.ASK.toLowerCase())
				|| type.toLowerCase().startsWith(CatpMessage.BID.toLowerCase()) || type
				.toLowerCase().startsWith(CatpMessage.TRANSACTION.toLowerCase()));
	}

	private synchronized long newId(final String type) {
		IdAllocator idAllocator = idAllocators.get(type);
		if (idAllocator == null) {
			idAllocator = new IdAllocator();
			idAllocators.put(type, idAllocator);
		}

		return idAllocator.nextId();
	}
}

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

package edu.cuny.cat;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.RegistrationEvent;

/**
 * A registry for a trader client.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class TraderRegistry extends ClientRegistry {

	protected static Logger logger = Logger.getLogger(TraderRegistry.class);

	@Override
	protected void processRegistration(RegistrationEvent event) {
		if (!event.getTraderId().equals(id)) {
			TraderRegistry.logger
					.error("Unexpected registration event for other traders received !");
		}

		super.processRegistration(event);
	}
}

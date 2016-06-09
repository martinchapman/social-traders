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

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * <p>
 * A main class for starting jcat game server and traders at the same time.
 * </p>
 * 
 * @see GameServer
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */
public class GameServerWithTraders extends GameServer {

	static Logger logger = Logger.getLogger(GameServerWithTraders.class);

	/**
	 * for starting server and traders
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {

		System.out.println(Game.getBanner());

		try {

			Game.setupObjectRegistry();

			Game.setupPreferences(null, args);

			Game.makeSureUnsynchronousInfrastructure();

			final Collection<? extends TraderClient> traders = Game.createTraders();

			final GameServer server = Game.createServer();

			Game.startTraders(traders);

			Game.startServer(server);

		} catch (final Exception e) {
			e.printStackTrace();
			Game.cleanupObjectRegistry();
			System.exit(1);
		}
	}
}

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

import edu.cuny.cat.server.GameController;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * Main class of jcat game server, used when the server is launched separatedly
 * from the game clients; otherwise the {@link Game} class should be used.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */
public class GameServer implements Parameterizable {

	static Logger logger = Logger.getLogger(GameServer.class);

	protected GameController controller;

	public GameServer() {
		controller = new GameController();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		controller.setup(parameters, base);
		controller.initialize();
	}

	public void start() {
		controller.start();
	}

	public GameController getController() {
		return controller;
	}

	/**
	 * for starting server alone, separating from clients
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {

		System.out.println(Game.getBanner());

		try {

			Game.setupObjectRegistry();
			Game.setupPreferences(null, args);
			final GameServer server = Game.createServer();
			Game.startServer(server);

		} catch (final Exception e) {
			e.printStackTrace();
			Game.cleanupObjectRegistry();
			System.exit(1);
		}
	}
}

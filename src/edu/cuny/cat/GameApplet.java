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

import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import edu.cuny.cat.server.GameController;
import edu.cuny.cat.ui.GuiConsole;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * Main class for jcat when running as an applet.
 * 
 * jcat applet do not use command line to load a parameter file as jcat
 * application; instead it requires a parameter <tt>parameter.file</tt>
 * specified in its containing webpage.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */

public class GameApplet extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String PARAM_FILE = "parameter.file";

	private static Logger logger = Logger.getLogger(GameApplet.class);

	@Override
	public void init() {

		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createGUI();
				}
			});
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void createGUI() {

		final JLabel statusLabel = new JLabel("initializing ...");
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1,
				Color.black));
		getContentPane().add(statusLabel, BorderLayout.CENTER);

		String paramFile = getParameter(GameApplet.PARAM_FILE);
		if ((paramFile == null) || (paramFile.length() == 0)) {
			paramFile = Game.getDefaultParameterFile();
		}

		final URL url = ParameterDatabase.getURL(paramFile);
		if (url == null) {
			showStatus("Failed to load parameter database " + paramFile);
		} else {

			try {

				Game.setupObjectRegistry();

				Game.setupPreferences(url, null);

				final Parameter base = new Parameter(Game.P_CAT);

				// always use the graphical game console
				Galaxy.getInstance().getTyped(Game.P_CAT, ParameterDatabase.class).set(
						base.push(Game.P_SERVER).push(GameController.P_CONSOLE),
						GuiConsole.class.getName());

				final Collection<? extends TraderClient> traders = Game.createTraders();
				final Collection<? extends MarketClient> markets = Game.createMarkets();

				final GameServer server = Game.createServer();

				Game.startTraders(traders);
				Game.startMarkets(markets);
				Game.startServer(server);

			} catch (final Exception e) {
				e.printStackTrace();
				showStatus(e.toString() + " occurred.");
			}

			GameApplet.logger.info("initialized.");
		}
	}

	@Override
	public void start() {
		GameApplet.logger.info("starting ...");
	}

	@Override
	public void stop() {
		GameApplet.logger.info("stopping ...");
	}

	@Override
	public void destroy() {
		GameApplet.logger.info("unloading...");
		Game.cleanupObjectRegistry();
	}
}

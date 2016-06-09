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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatpInfrastructure;
import edu.cuny.cat.comm.CatpServerUnavailableException;
import edu.cuny.cat.comm.SocketBasedInfrastructureImpl;
import edu.cuny.cat.server.IdentityOffice;
import edu.cuny.event.EventEngine;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * Main class of jcat when jcat is run as a Java application to launch a game,
 * including a {@link GameServer} and multiple {@link GameClient}s.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i></i><tt>cat.server</tt><br>
 * <font size=-1></font></td>
 * <td valign=top>(the parameter base for GameServer)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i></i><tt>cat.agent.n</tt><br>
 * <font size=-1>int >=0</font></td>
 * <td valign=top>(the number of subpopulations of trader clients)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i></i><tt>cat.agent.</tt><i>n</i><br>
 * <font size=-1>class, being or inheriting TraderClient</font></td>
 * <td valign=top>(the class of trader clients for the <i>n</i>th trader
 * subpopulation)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i></i><tt>cat.specialist.n</tt><br>
 * <font size=-1>int >=0</font></td>
 * <td valign=top>(the number of subpopulations of specialist clients)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i></i><tt>cat.specialist.</tt><i>n</i><br>
 * <font size=-1>class, being or inheriting MarketClient</font></td>
 * <td valign=top>(the class of market clients for the <i>n</i>th specialist
 * subpopulation)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i></i><tt>cat.specialist.</tt><i>optional.dir</i><br>
 * <font size=-1>string</font></td>
 * <td valign=top>(the directory to look for the configurations of optional
 * market clients)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i></i><tt>cat.specialist.</tt><i>optional.base</i><br>
 * <font size=-1>string</font></td>
 * <td valign=top>(the base of parameters used to specify the configurations of
 * optional market clients in the parameter files located in
 * <code>cat.specialist.optional.dir</code>)</td>
 * </tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.62 $
 */

public final class Game {

	public static final int minorVersion = 17;

	public static final int majorVersion = 0;

	public static final String NAME = "JCAT";

	public static final String EMAIL = "jcat-discuss@lists.sourceforge.net";

	public static final String BANNER = ""
			// + "--------------------------------------------------------------\n"
			+ Game.NAME + " " + Game.getVersion()
			+ " - (C) 2006-2010 Jinzhong Niu, Kai Cai. \n\n" + Game.NAME
			+ " comes with ABSOLUTELY NO WARRANTY. This is free software,\n"
			+ "and you are welcome to redistribute it under certain conditions;\n"
			+ "see the GNU General Public license for more details.\n" + "\n"
			+ "This is alpha test software.  Please report any bugs, issues\n"
			+ "or suggestions to " + Game.EMAIL + ".\n" + "\n" + "Acknowledgements\n"
			+ " " + Game.NAME
			+ " contains classes from the following open-source packages: \n"
			+ " * JASA (Java Auction Simulator API), (C) 2001-2006 Steve Phelps\n"
			+ " * JAF (Java Application Framework), (C) 1999-2007 Jinzhong Niu\n"
			+ " * ECJ (Evolutionary Computation in Java), (C) 2005 Sean Luke\n"
			// + "--------------------------------------------------------------\n"
			+ "\n";

	public static final String P_CAT = "cat";

	public static final String P_VERSION = "version";

	public static final String P_NAME = "name";

	public static final String P_INFRASTRUCTURE = "infrastructure";

	public static final String P_GUI_APPENDER = "appender";

	public static final String P_SERVER = "server";

	public static final String P_AGENT = "agent";

	public static final String P_SPECIALIST = "specialist";

	public static final String P_OPTIONAL = "optional";

	public static final String P_DIR = "dir";

	public static final String P_BASE = "base";

	public static final String P_NUM = "n";

	// used to generate temporary names for clients
	protected static IdentityOffice identityOffice;

	protected static ClientSynchronizer clientSynchronizer;

	static Logger logger = Logger.getLogger(Game.class);

	public static String getDefaultParameterFile() {
		return "params/cat.params";
	}

	/**
	 * retrieves the name of the parameter file from command line arguments; or
	 * the default file is used if no argument is present.
	 * 
	 * @param args
	 *          command line arguments
	 * @return the name of the parameter file
	 */
	public static String getParameterFile(final String[] args) {

		if (args.length < 1) {
			return Game.getDefaultParameterFile();
		} else {
			return args[0];
		}
	}

	/**
	 * initializes {@link edu.cuny.util.Galaxy}, {@link GameServer},
	 * {@link MarketClient}s, and {@link TraderClient}s.
	 * 
	 * @param args
	 *          command line arguments
	 */
	public static void main(final String[] args) {

		System.out.println(Game.getBanner());

		try {

			Game.setupObjectRegistry();

			Game.setupPreferences(null, args, true);

			/*
			 * separating creation and starting of clients and the server aims to
			 * display logging of initialization of server later than that of clients;
			 * in the meanwhile, clients won't start and try to connect until the
			 * server is ready, and the server won't start the game until all (local)
			 * clients are already connected.
			 */

			final Collection<? extends TraderClient> traders = Game.createTraders();

			final Collection<? extends MarketClient> markets = Game.createMarkets();

			final Collection<? extends MarketClient> optionalMarkets = Game
					.createOptionalMarkets();

			final GameServer server = Game.createServer();

			Game.startTraders(traders);

			Game.startMarkets(markets);

			Game.startMarkets(optionalMarkets);

			Game.cleanUpAfterInitialization();

			Game.startServer(server);

		} catch (final Exception e) {
			e.printStackTrace();
			Game.cleanupObjectRegistry();
			System.exit(1);
		}
	}

	public static void setupObjectRegistry() {

		// backup the old default system
		Galaxy.getInstance().put(Game.P_CAT, String.class,
				Galaxy.getInstance().getDefaultSystem());

		Galaxy.getInstance().setDefaultSystem(Game.P_CAT);

		final EventEngine eventEngine = new EventEngine();
		eventEngine.start();
		Galaxy.getInstance().put(Game.P_CAT, EventEngine.class, eventEngine);

		final GlobalPRNG globalPRNG = new GlobalPRNG();
		Galaxy.getInstance().put(Game.P_CAT, GlobalPRNG.class, globalPRNG);
	}

	public static void cleanupObjectRegistry() {
		Galaxy.getInstance().getTyped(Game.P_CAT, EventEngine.class).stop();

		// restore the old default system
		Galaxy.getInstance().setDefaultSystem(
				Galaxy.getInstance().getTyped(Game.P_CAT, String.class));

		Galaxy.getInstance().remove(Game.P_CAT);

	}

	public static void setupPreferences(final URL url, final String args[]) {
		Game.setupPreferences(url, args, true);
	}

	/**
	 * does the following:
	 * <ul>
	 * <li>initializes log4j with the parameter file</li>
	 * <li>loads a parameter file into a {@link ParameterDatabase}</li>
	 * <li>invokes {@link #setupPreferences(ParameterDatabase)} to setup
	 * preferences using the <code>ParameterDatabase</code>;
	 * </ul>
	 * 
	 * @param url
	 *          where the parameter file is located.
	 * @param args
	 *          command line arguments transferred from <code>main()</code>.
	 * @param startLogging
	 *          configures log4j for logging if true, or not otherwise.
	 */
	public static void setupPreferences(URL url, final String args[],
			final boolean startLogging) {

		try {

			// when running in applet, url is not null.

			if (url == null) {
				url = ParameterDatabase.getURL(Game.getParameterFile(args));
			}

			if (url == null) {
				Utils.fatalError("Failed to load parameter database: "
						+ Game.getParameterFile(args));
			}

			// log4j

			if (startLogging) {
				org.apache.log4j.PropertyConfigurator.configure(url);
			}

			// parameter database

			Game.logger.info("loading preferences ...");
			Game.logger.info("\n");

			ParameterDatabase parameters = null;
			if (args == null) {
				parameters = new ParameterDatabase(url);
			} else {
				parameters = new ParameterDatabase(url, args);
			}

			Game.setupPreferences(parameters);

		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * sets up the preferences of JCAT using the parameter database.
	 * 
	 * @param parameters
	 */
	public static void setupPreferences(final ParameterDatabase parameters) {

		final Parameter base = new Parameter(Game.P_CAT);

		Galaxy.getInstance().put(Game.P_CAT, ParameterDatabase.class, parameters);

		// software version

		parameters.set(base.push(Game.P_VERSION), Game.getVersion());
		parameters.set(base.push(Game.P_NAME), Game.getName());

		// random number generators

		final GlobalPRNG prng = Galaxy.getInstance().getTyped(Game.P_CAT,
				GlobalPRNG.class);
		prng.setup(parameters, base);

		// catp infrastructure

		final CatpInfrastructure infrast = parameters.getInstanceForParameter(base
				.push(Game.P_INFRASTRUCTURE), null, CatpInfrastructure.class);
		if (infrast instanceof Parameterizable) {
			((Parameterizable) infrast).setup(parameters, base
					.push(Game.P_INFRASTRUCTURE));
		}
		Galaxy.getInstance().put(Game.P_CAT, CatpInfrastructure.class, infrast);

		/*
		 * when communication is synchronous, use a single random number engine
		 * since there is only one thread.
		 */
		if (infrast.isSynchronous()) {
			prng.setUseMultiEngine(false);
		}

		//

		Game.identityOffice = new IdentityOffice();
		Game.clientSynchronizer = new ClientSynchronizer();

	}

	/**
	 * creates an instance of {@link GameServer} and initializes it with parameter
	 * database in the object galaxy.
	 */
	public static GameServer createServer() {

		Game.logger.info("\n");
		Game.logger.info("creating server ...");
		Game.logger.info("\n");

		final ParameterDatabase parameters = Galaxy.getInstance().getTyped(
				Game.P_CAT, ParameterDatabase.class);
		final Parameter base = new Parameter(Game.P_CAT);

		final GameServer server = new GameServer();
		server.setup(parameters, base.push(Game.P_SERVER));

		return server;
	}

	public static void startServer(final GameServer server) {
		Game.logger.info("\n");
		Game.logger.info("starting server ...");
		Game.logger.info("\n");

		server.start();
	}

	/**
	 * creates multiple traders, each an instance of {@link TraderClient},
	 * initializes them with parameter database in the object galaxy, and starts
	 * them.
	 * 
	 * @throws InstantiationException
	 *           if fails to instantiate trader clients
	 * @throws IllegalAccessException
	 */
	public static Collection<? extends TraderClient> createTraders()
			throws InstantiationException, IllegalAccessException {

		Game.logger.info("\n");
		Game.logger.info("creating traders ...");
		Game.logger.info("\n");

		final ParameterDatabase parameters = Galaxy.getInstance().getTyped(
				Game.P_CAT, ParameterDatabase.class);
		final Parameter base = new Parameter(Game.P_CAT);

		final Parameter typeParam = base.push(Game.P_AGENT);

		final int numTypes = parameters.getInt(typeParam.push(Game.P_NUM), null, 1);

		final Collection<TraderClient> traderColl = new ArrayList<TraderClient>();

		for (int t = 0; t < numTypes; t++) {

			final Parameter typeParamT = typeParam.push("" + t);

			final Class<? extends TraderClient> traderClass = parameters
					.getClassForParameter(typeParamT, null, TraderClient.class);

			final int numAgents = parameters.getInt(typeParamT.push(Game.P_NUM),
					null, 0);

			Game.logger.info("agent population " + t + "\n\t" + numAgents
					+ " agent(s) of type " + parameters.getString(typeParamT, null));

			final TraderClient traders[] = new TraderClient[numAgents];
			for (int i = 0; i < numAgents; i++) {
				traders[i] = traderClass.newInstance();
				traders[i].setup(parameters, typeParamT);
				if (i == 0) {
					if (traders[i].getId() != null) {
						Game.logger.info(Utils.indent(Utils.indent("* " + GameClient.P_ID
								+ ": " + traders[i].getId())));
					}
					Game.logger.info(Utils.indent(Utils.indent(traders[i].getAgent()
							.toString())));
				}

				if (traders[i].getId() == null) {
					traders[i].setId(traders[i].getType());
				}

				if (numAgents > 1) {
					traders[i].setId(Game.identityOffice.createIdentity(traders[i]
							.getId()));
				}

				traderColl.add(traders[i]);
			}
		}

		Game.logger.info("\n");

		return traderColl;
	}

	public static void startTraders(
			final Collection<? extends TraderClient> traderColl) {
		if (traderColl == null) {
			return;
		}

		Game.logger.info("\n");
		Game.logger.info("starting traders ...");
		Game.logger.info("\n");

		final TraderClient traders[] = traderColl.toArray(new TraderClient[0]);
		for (final TraderClient trader : traders) {
			if ((Galaxy.getInstance().getTyped(Game.P_CAT, CatpInfrastructure.class))
					.isSynchronous()) {
				Game.clientSynchronizer.countMe();
				trader.run();
				Game.clientSynchronizer.waitForClients();
			} else {
				/* start a separate thread only when communication is asynchronous */
				new Thread(trader).start();
			}
		}
	}

	/**
	 * creates multiple market/specialist clients using the default parameter base
	 * for specialists.
	 * 
	 * @throws InstantiationException
	 *           if fails to instantiate market clients
	 * @throws IllegalAccessException
	 * 
	 * @see #createMarkets(Parameter)
	 */
	public static Collection<? extends MarketClient> createMarkets()
			throws InstantiationException, IllegalAccessException {
		final Parameter base = new Parameter(Game.P_CAT);
		final Parameter typeParam = base.push(Game.P_SPECIALIST);

		return Game.createMarkets(typeParam);
	}

	/**
	 * creates multiple market/specialist clients, each an instance of
	 * {@link MarketClient}, initializes them with parameter database in
	 * {@link edu.cuny.util.ObjectRegistry} using the given parameter base.
	 * 
	 * @throws InstantiationException
	 *           if fails to instantiate market clients
	 * @throws IllegalAccessException
	 */
	public static Collection<? extends MarketClient> createMarkets(
			final Parameter base) throws InstantiationException,
			IllegalAccessException {

		final ParameterDatabase parameters = Galaxy.getInstance().getTyped(
				Game.P_CAT, ParameterDatabase.class);

		return Game.createMarkets(parameters, base);
	}

	/**
	 * creates multiple market/specialist clients, each an instance of
	 * {@link MarketClient}, initializes them with the given parameter database
	 * and using the given parameter base.
	 * 
	 * @param parameters
	 * @param base
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Collection<? extends MarketClient> createMarkets(
			final ParameterDatabase parameters, final Parameter base)
			throws InstantiationException, IllegalAccessException {

		Game.logger.info("creating markets defined at " + base.toString() + " ...");
		Game.logger.info("\n");

		final int numTypes = parameters.getInt(base.push(Game.P_NUM), null, 1);

		final Collection<MarketClient> marketColl = new ArrayList<MarketClient>();

		for (int t = 0; t < numTypes; t++) {

			final Parameter typeParamT = base.push("" + t);

			final Class<? extends MarketClient> marketClass = parameters
					.getClassForParameter(typeParamT, null, MarketClient.class);

			final int numMarkets = parameters.getInt(typeParamT.push(Game.P_NUM),
					null, 0);

			Game.logger.info("specialist population " + t + "\n\t" + numMarkets
					+ " specialist(s) of type " + parameters.getString(typeParamT, null));

			final MarketClient markets[] = new MarketClient[numMarkets];
			for (int i = 0; i < numMarkets; i++) {
				markets[i] = marketClass.newInstance();
				markets[i].setup(parameters, typeParamT);
				if (i == 0) {
					if (markets[i].getId() != null) {
						Game.logger.info(Utils.indent(Utils.indent("* " + GameClient.P_ID
								+ ": " + markets[i].getId())));
					}
					Game.logger.info(Utils.indent(Utils.indent(markets[i].getAuctioneer()
							.toString())));
				}

				if (markets[i].getId() == null) {
					markets[i].setId(markets[i].getType());
				}

				if (numMarkets > 1) {
					markets[i].setId(Game.identityOffice.createIdentity(markets[i]
							.getId()));
				}

				marketColl.add(markets[i]);
			}
		}

		Game.logger.info("\n");

		return marketColl;
	}

	/**
	 * creates multiple market/specialist clients, each an instance of
	 * {@link MarketClient}, based on parameter files in the specified directory
	 * and its subdirectories. Each of these parameter files define a set of
	 * market clients using the specified parameter base.
	 * 
	 * For example
	 * 
	 * <pre>
	 * cat.specialist.optional.dir = params/elites
	 * cat.specialist.optional.base = elites
	 * </pre>
	 * 
	 * specifies to look for parameter files in the directory
	 * <code>params/elites</code> and all market clients are configured using the
	 * parameter base <code>elites</code>.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static Collection<? extends MarketClient> createOptionalMarkets()
			throws InstantiationException, IllegalAccessException {
		final Parameter optionalParam = new Parameter(Game.P_CAT).push(
				Game.P_SPECIALIST).push(Game.P_OPTIONAL);

		final ParameterDatabase parameters = Galaxy.getInstance().getTyped(
				Game.P_CAT, ParameterDatabase.class);

		if (!parameters.exists(optionalParam.push(Game.P_DIR))
				&& !parameters.exists(optionalParam.push(Game.P_BASE))) {
			// no optional markets defined
			Game.logger.info("no optional market defined at " + optionalParam);
			return null;
		}

		final File dir = parameters.getFile(optionalParam.push(Game.P_DIR), null);
		if ((dir == null) || !dir.exists() || !dir.isDirectory()) {
			Game.logger.fatal("Directory for optional markets does NOT exist !");
			Game.logger.fatal(dir.toString());
			Utils.fatalError();
			return null;
		} else {
			final String optionalBase = parameters.getString(optionalParam
					.push(Game.P_BASE), null);
			Game.logger.info("\n");
			if (optionalBase == null) {
				Game.logger.info("No optional markets configured.");
				Game.logger.info("\n");
				return null;
			} else {
				Game.logger.info("creating optional markets defined in " + dir + " at "
						+ optionalBase + " ...");

				final Collection<MarketClient> marketColl = new ArrayList<MarketClient>();

				final Collection<File> files = FileUtils.listFiles(dir,
						new SuffixFileFilter(".params"), DirectoryFileFilter.INSTANCE);

				final ParameterDatabase root = new ParameterDatabase();
				ParameterDatabase parametersPerFile = null;
				Collection<? extends MarketClient> marketCollPerFile = null;
				final Parameter base = new Parameter(optionalBase);
				for (final File file : files) {
					try {
						Game.logger.info("reading " + file.toString());
						parametersPerFile = new ParameterDatabase(file);
					} catch (final FileNotFoundException e) {
						e.printStackTrace();
						continue;
					} catch (final IOException e) {
						e.printStackTrace();
						continue;
					}
					root.addParent(parameters);
					root.addParent(parametersPerFile);

					marketCollPerFile = Game.createMarkets(root, base);
					marketColl.addAll(marketCollPerFile);
					root.removeParents();
				}

				return marketColl;
			}
		}
	}

	public static void startMarkets(
			final Collection<? extends MarketClient> marketColl) {
		if (marketColl == null) {
			return;
		}

		Game.logger.info("\n");
		Game.logger.info("starting markets ...");
		Game.logger.info("\n");

		final MarketClient markets[] = marketColl.toArray(new MarketClient[0]);
		for (final MarketClient market : markets) {
			if (Galaxy.getInstance().getTyped(Game.P_CAT, CatpInfrastructure.class)
					.isSynchronous()) {
				Game.clientSynchronizer.countMe();
				market.run();
				Game.clientSynchronizer.waitForClients();
			} else {
				/* start a separate thread only when communication is asynchronous */
				new Thread(market).start();
			}
		}
	}

	public static void cleanUpAfterInitialization() {
		Game.identityOffice = null;
		Game.clientSynchronizer.dispose();
		Game.clientSynchronizer = null;
	}

	public static void makeSureUnsynchronousInfrastructure()
			throws CatpServerUnavailableException {
		final CatpInfrastructure infra = Galaxy.getInstance().getTyped(Game.P_CAT,
				CatpInfrastructure.class);
		if ((infra == null) || infra.isSynchronous()) {
			final String err = Game.P_CAT
					+ "."
					+ Game.P_INFRASTRUCTURE
					+ " is not configured or should be configured to use "
					+ SocketBasedInfrastructureImpl.class.getName()
					+ " since you are trying to run clients separately from the game server !";
			throw new CatpServerUnavailableException(err);
		}
	}

	public static String getBanner() {
		return Game.BANNER;
	}

	public static String getVersion() {
		return Game.majorVersion + "." + Game.minorVersion;
	}

	public static String getName() {
		return Game.NAME;
	}
}

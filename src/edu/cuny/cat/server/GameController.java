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

import java.util.Observable;

import org.apache.log4j.Logger;

import edu.cuny.cat.Game;
import edu.cuny.cat.comm.CatpInfrastructure;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.AvailableMarketsAnnouncedEvent;
import edu.cuny.cat.event.AvailableTradersAnnouncedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayInitPassEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.stat.CombiGameReport;
import edu.cuny.cat.stat.GameReport;
import edu.cuny.cat.stat.ReportVariableBoard;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A class used at {@link edu.cuny.cat.GameServer} as a control hub,
 * initializing various components, event passing, etc.
 * </p>
 * 
 * <p>
 * The game console and clock controller configured should match to support
 * different ways of running cat games.
 * <ul>
 * <li>To run a game with graphical interface, use
 * 
 * <pre>
 * cat.server.console = edu.cuny.cat.ui.GuiConsole
 * cat.server.clockcontroller = edu.cuny.cat.server.EventBasedClockController
 * </pre>
 * 
 * <li>To run a game using telnet, use
 * 
 * <pre>
 * cat.server.console = edu.cuny.cat.server.TelnetConsole
 * cat.server.clockcontroller = edu.cuny.cat.server.EventBasedClockController
 * </pre>
 * 
 * <li>To run a game that starts and stops automatically without external
 * intervention, use
 * 
 * <pre>
 * cat.server.console = edu.cuny.cat.ui.SilentConsole
 * </pre>
 * 
 * Plus
 * 
 * <pre>
 * cat.server.clockcontroller = edu.cuny.cat.server.AutoClockController
 * </pre>
 * 
 * for asynchronous communication mode, or
 * 
 * <pre>
 * cat.server.clockcontroller = edu.cuny.cat.server.SynchronousClockController
 * </pre>
 * 
 * for synchronous communication mode.
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.registry</tt><br>
 * <font size=-1>class, implementing {@link edu.cuny.cat.registry.Registry}
 * </font></td>
 * <td valign=top>(the type of registry facility to restore game activities for
 * run-time access)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.console</tt><br>
 * <font size=-1>class, implementing {@link edu.cuny.cat.server.Console}</font></td>
 * <td valign=top>(the console for user to control the game)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.clockcontroller</tt><br>
 * <font size=-1>class, implementing {@link edu.cuny.cat.server.ClockController}
 * </font></td>
 * <td valign=top>(the way to control the game clock, including how to start and
 * stop the clock)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.report</tt><br>
 * <font size=-1>class, implementing {@link edu.cuny.cat.stat.GameReport}</font>
 * </td>
 * <td valign=top>(the type of report facility to log game activities)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.gc</tt><br>
 * <font size=-1>boolean (<code>false</code> by default)</font></td>
 * <td valign=top>(whether running garbage collector following
 * {@link edu.cuny.cat.event.DayClosedEvent})</td>
 * </tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.64 $
 */
public final class GameController extends Observable implements
		Parameterizable, AuctionEventListener {

	static Logger logger = Logger.getLogger(GameController.class);

	public static final String P_REGISTRY = "registry";

	public static final String P_CONSOLE = "console";

	public static final String P_REPORT = "report";

	public static final String P_GC = "gc";

	protected ConnectionManager connManager;

	protected TimeoutController timeoutController;

	protected Registry registry;

	protected static GameController instance;

	protected GameClock clock;

	protected ClockController clockController;

	protected ShoutValidator shoutValidator;

	protected TransactionValidator transactionValidator;

	protected ChargeValidator chargeValidator;

	protected Console console;

	protected ValuerFactory valuerFactory;

	protected boolean showWinnerList;

	protected GameReport report;

	protected ClientIdentityController identityController;

	protected ClientBehaviorController behaviorController;

	protected SecurityManager securityManager;

	protected boolean dailyGC = false;

	public GameController() {
		if (GameController.instance != null) {
			GameController.logger.error(getClass().getSimpleName()
					+ " can only be instantiated once !");
		}
		GameController.instance = this;

		clock = new GameClock();
		clock.addAuctionEventListener(this);

		shoutValidator = new ShoutValidator();
		transactionValidator = new TransactionValidator();
		chargeValidator = new ChargeValidator();

		valuerFactory = new ValuerFactory();

		connManager = new ConnectionManager();

		timeoutController = new TimeoutController();

		identityController = new ClientIdentityController();

		behaviorController = new ClientBehaviorController();

		securityManager = new SecurityManager();
	}

	public static GameController getInstance() {
		return GameController.instance;
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		GameController.logger.info("\n");

		clock.setup(parameters, base);
		clock.initialize();
		GameController.logger.info(Utils.indent(clock.toString()));

		timeoutController.setup(parameters, base);
		GameController.logger.info(Utils.indent(timeoutController.toString()));

		behaviorController.setup(parameters, base);
		GameController.logger.info(Utils.indent(behaviorController.toString()));

		securityManager.setup(parameters, base.push(SecurityManager.P_DEF_BASE));
		GameController.logger.info(Utils.indent(securityManager.toString()));

		shoutValidator.setup(parameters, base.push(ShoutValidator.P_DEF_BASE));
		chargeValidator.setup(parameters, base.push(ChargeValidator.P_DEF_BASE));

		valuerFactory.setup(parameters, base.push(ValuerFactory.P_DEF_BASE));
		GameController.logger.info(Utils.indent(valuerFactory.toString()));

		registry = parameters.getInstanceForParameter(base
				.push(GameController.P_REGISTRY), null, Registry.class);
		if (registry instanceof Parameterizable) {
			((Parameterizable) registry).setup(parameters, base
					.push(GameController.P_REGISTRY));
		}
		GameController.logger.info(Utils.indent(registry.toString()));

		// this needs to be after setup of registry
		identityController.setup(parameters, base
				.push(ClientIdentityController.P_DEF_BASE));

		report = parameters.getInstanceForParameter(base
				.push(GameController.P_REPORT), null, GameReport.class);
		if (report instanceof Parameterizable) {
			((Parameterizable) report).setup(parameters, base
					.push(GameController.P_REPORT));
		}
		GameController.logger.info(Utils.indent(report.toString()));

		console = parameters.getInstanceForParameter(base
				.push(GameController.P_CONSOLE), null, Console.class);
		if (console instanceof Parameterizable) {
			((Parameterizable) console).setup(parameters, base
					.push(GameController.P_CONSOLE));
		}
		GameController.logger.info(Utils.indent(console.toString()));

		clockController = parameters.getInstanceForParameter(base
				.push(ClockController.P_DEF_BASE), null, ClockController.class);
		if (clockController instanceof Parameterizable) {
			((Parameterizable) clockController).setup(parameters, base
					.push(ClockController.P_DEF_BASE));
		}

		// TODO: to merge console and clock controller classes
		// override incompatible clock controller
		if (console.isInteractive()
				&& !(clockController instanceof EventBasedClockController)) {
			/* override the configuration of clock controller */
			clockController = new EventBasedClockController();
		} else if (!console.isInteractive()
				&& (clockController instanceof EventBasedClockController)) {
			clockController = new SynchronousClockController();
		}
		GameController.logger.info(Utils.indent(clockController.toString()));

		dailyGC = parameters.getBoolean(base.push(GameController.P_GC), null,
				dailyGC);
	}

	/**
	 * to initialize the game controller and make the server ready for clients to
	 * check in.
	 */
	public void initialize() {
		registry.start();
		console.start();
		connManager.start();
	}

	/**
	 * to start the game.
	 */
	public void start() {
		// When {@link SychronousClockController} is used, starting it here, after
		// initialize(), makes sure all local clients are ready to play!

		// give control to clock controller
		clockController.start();
	}

	/**
	 * to stop the game.
	 */
	public void stop() {
		// TODO: do more cleanup
		connManager.terminate();
		console.stop();
		registry.stop();

		Galaxy.getInstance().getDefaultTyped(CatpInfrastructure.class).cleanUp();
	}

	public void dispose() {
		GameController.instance = null;
	}

	public void exit() {
		dispose();

		Game.cleanupObjectRegistry();

		GameController.logger.info("server quitted.");
		System.exit(0);
	}

	public GameClock getClock() {
		return clock;
	}

	public ClockController getClockController() {
		return clockController;
	}

	public ShoutValidator getShoutValidator() {
		return shoutValidator;
	}

	public TransactionValidator getTransactionValidator() {
		return transactionValidator;
	}

	public ChargeValidator getChargeValidator() {
		return chargeValidator;
	}

	public ConnectionManager getConnectionManager() {
		return connManager;
	}

	public TimeoutController getTimeController() {
		return timeoutController;
	}

	public ClientBehaviorController getBehaviorController() {
		return behaviorController;
	}

	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	public Registry getRegistry() {
		return registry;
	}

	public Console getConsole() {
		return console;
	}

	public ValuerFactory getValuerFactory() {
		return valuerFactory;
	}

	public GameReport getReport() {
		return report;
	}

	public <R extends GameReport> R getReport(final Class<R> reportClass) {
		if (reportClass.isInstance(report)) {
			return reportClass.cast(report);
		} else if (report instanceof CombiGameReport) {
			return ((CombiGameReport) report).getReport(reportClass);
		}

		return null;
	}

	public void addReport(final GameReport newReport) {
		if (report == null) {
			report = newReport;
		} else if (report instanceof CombiGameReport) {
			((CombiGameReport) report).addReport(newReport);
		} else {
			final CombiGameReport reports = new CombiGameReport();
			reports.addReport(report);
			reports.addReport(newReport);
			report = reports;
		}
	}

	/**
	 * sends auction events to registry, console, and reports.
	 * 
	 * @param event
	 */
	public void processEventInsideServer(final AuctionEvent event) {

		if (event instanceof DayOpeningEvent) {
			// to send init events to game reports before a day opens.
			DayInitPassEvent dipEvent = null;
			for (int i = DayInitPassEvent.START_PASS; i <= DayInitPassEvent.END_PASS; i++) {
				dipEvent = new DayInitPassEvent(i);
				dipEvent.setTime(event.getTime());
				notifyInsideListener(report, dipEvent);
				notifyInsideListener(console, dipEvent);
			}
		}

		// the order matters !
		notifyInsideListener(registry, event);
		notifyInsideListener(connManager, event);
		notifyInsideListener(report, event);
		notifyInsideListener(console, event);

		if (event instanceof GameStartingEvent) {
			behaviorController.reset();
		} else if (event instanceof DayOpeningEvent) {

			// clears all the daily report variables
			ReportVariableBoard.getInstance().reset();

		} else if (event instanceof DayClosedEvent) {

			// to send stat events to game reports after a day closes.
			DayStatPassEvent dspEvent = null;
			for (int i = DayStatPassEvent.START_PASS; i <= DayStatPassEvent.END_PASS; i++) {
				dspEvent = new DayStatPassEvent(i);
				dspEvent.setTime(event.getTime());
				notifyInsideListener(report, dspEvent);
				notifyInsideListener(console, dspEvent);
			}
		} else if (event instanceof SimulationOverEvent) {
			report.produceUserOutput();
		}

		/* should be the last one to notify */
		notifyInsideListener(clockController, event);
	}

	/**
	 * notifies a listener of an auction event.
	 * 
	 * @param listener
	 * @param event
	 */
	protected void notifyInsideListener(final AuctionEventListener listener,
			final AuctionEvent event) {

		// wraps the processing in a try-and-catch statement so as to avoid
		// systematic failure
		try {
			if (listener != null) {
				listener.eventOccurred(event);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			GameController.logger.error(e);
		}
	}

	/**
	 * listens to game clock's notifications
	 */
	public void eventOccurred(final AuctionEvent event) {

		// Note: the following order matters

		if (dailyGC && (event instanceof DayClosedEvent)) {
			GameController.logger.debug("garbage collecting ...\n");
			System.gc();
			System.runFinalization();
		}

		// sends events to modules inside the server
		processEventInsideServer(event);

		// sends events to clients
		connManager.dispatchEvent(event);

		/*
		 * Moved the dispatching of POST TRADER and POST SPECIALIST from {@link
		 * ConnectionAdaptor.GameStartingSession.processResponse()} and {@link
		 * ConnectionAdaptor.PostTraderSession.processResponse()} here to benefit
		 * from two things when there are many clients:
		 * 
		 * 1. When a sync timeout occurs, GameStartedEvent may be possibly
		 * dispatched earlier than POST SPECIALIST, leading to incorrect order of
		 * events to clients.
		 * 
		 * 2. This will add a single task to the event dispatcher instead of a
		 * number of tasks with each for a single client.
		 */
		if (event instanceof GameStartingEvent) {

			// TODO: to put real list of traders in the event?
			final AvailableTradersAnnouncedEvent ataEvent = new AvailableTradersAnnouncedEvent(
					null);
			ataEvent.setTime(clock.getTime());
			connManager.dispatchEvent(ataEvent);

			// TODO: to put real list of specialists in the event?
			final AvailableMarketsAnnouncedEvent amaEvent = new AvailableMarketsAnnouncedEvent(
					null);
			amaEvent.setTime(clock.getTime());
			connManager.dispatchEvent(amaEvent);
		}
	}
}

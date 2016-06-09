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

import java.util.LinkedList;

import org.apache.log4j.Logger;

import uk.ac.liv.cat.socialnetwork.util.PlaySound;

import edu.cuny.cat.Game;
import edu.cuny.cat.comm.CatpInfrastructure;
import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameOverEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundClosingEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.SyncTask;

/**
 * <p>
 * This class implements a clock for a cat game.
 * </p>
 * 
 * <p>
 * The clock runs tick (second) by tick. A certain number of ticks makes a
 * trading round, and a certain number of rounds makes a trading day. A cat game
 * lasts one or more days. There is a game initialization period before the
 * first day begins in a game and a day initialization before the first round
 * begins in a day. Days are separated by day breaks and rounds by round breaks.
 * </p>
 * 
 * <p>
 * This game clock also allows a simulation of multiple games in a row by
 * specifying <code>iterations > 1</code>.
 * </p>
 * 
 * <p>
 * The clock triggers time-related game events and notifies listeners of them,
 * including {@link SimulationStartedEvent}, {@link SimulationOverEvent},
 * {@link GameStartingEvent}, {@link GameStartedEvent}, {@link GameOverEvent},
 * {@link DayOpeningEvent}, {@link DayOpenedEvent}, {@link DayClosedEvent},
 * {@link RoundOpenedEvent}, {@link RoundClosingEvent}, and
 * {@link RoundClosedEvent}.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.iterations</tt><br>
 * <font size=-1>int >=1 (1 by default)</font></td>
 * <td valign=top>(number of games to run in a row, solely used in a simulation)
 * </td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.gamelen</tt><br>
 * <font size=-1>int >=1 (20 by default)</font></td>
 * <td valign=top>(number of days in a game)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.daylen</tt><br>
 * <font size=-1>int >=1 (50 by default)</font></td>
 * <td valign=top>(number of rounds in a day)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.roundlen</tt><br>
 * <font size=-1>int >=1 (10000 by default)</font></td>
 * <td valign=top>(number of ticks in a round)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.roundclosinglen</tt><br>
 * <font size=-1>int >=1 (500 by default)</font></td>
 * <td valign=top>(number of ticks for a closing round to be closed)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.gamebreak</tt><br>
 * <font size=-1>int >=1 (1000 by default)</font></td>
 * <td valign=top>(the length of game break in terms of ticks)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.daybreak</tt><br>
 * <font size=-1>int >=1 (5 by default)</font></td>
 * <td valign=top>(the length of a day break in terms of ticks)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.roundbreak</tt><br>
 * <font size=-1>int >=1 (100 by default)</font></td>
 * <td valign=top>(the length of a round break in terms of ticks)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.gameinit</tt><br>
 * <font size=-1>int >=1 (2000 by default)</font></td>
 * <td valign=top>(the length of game initialization period in terms of ticks)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.dayinit</tt><br>
 * <font size=-1>int >=1 (2000 by default)</font></td>
 * <td valign=top>(the length of a day initialization in terms of ticks)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.synctimeout</tt><br>
 * <font size=-1>int >=1 (60000 by default)</font></td>
 * <td valign=top>(the number of seconds the clock waits for all clients to
 * respond to a timing event)</td>
 * </tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.53 $
 */
public class GameClock implements Parameterizable, Runnable {

	private static final String P_ITERATIONS = "iterations";

	private static final String P_GAMELEN = "gamelen";

	private static final String P_DAYLEN = "daylen";

	private static final String P_ROUNDLEN = "roundlen";

	private static final String P_ROUNDCLOSINGLEN = "roundclosinglen";

	private static final String P_GAMEBREAK = "gamebreak";

	private static final String P_DAYBREAK = "daybreak";

	private static final String P_ROUNDBREAK = "roundbreak";

	private static final String P_GAMEINIT = "gameinit";

	private static final String P_DAYINIT = "dayinit";

	private static final String P_SYNCTIMEOUT = "synctimeout";

	protected static final int DEFAULT_SYNCTIMEOUT = 60000;

	protected int syncTimeout = GameClock.DEFAULT_SYNCTIMEOUT;

	protected int iterations = 1;

	protected int gameLen = 20;

	protected int dayLen = 50;

	protected int roundLen = 10000;

	protected int gameBreak = 1000;

	protected int dayBreak = 5;

	protected int roundClosingLen = 500;

	protected int roundBreak = 1000;

	protected int gameInitLen = 2000;

	protected int dayInitLen = 2000;

	protected int day;

	protected int round;

	protected long round_start;

	protected LinkedList<AuctionEventListener> eventListeners;

	protected Thread thread;

	protected boolean paused;

	protected SyncTask syncTask;

	protected SyncTask syncRegisterTask;

	protected SyncTask syncFeeTask;

	protected SyncTask syncClientListTask;

	protected Registry registry;

	private long adjustmentStart;

	private long syncAdjustment;

	protected CatpInfrastructure infrast;

	static Logger logger = Logger.getLogger(GameClock.class);

	public GameClock() {
		infrast = Galaxy.getInstance().getDefaultTyped(CatpInfrastructure.class);

		eventListeners = new LinkedList<AuctionEventListener>();

		day = -1;
		round = -1;
		round_start = -1;

		paused = false;

		syncTask = new SyncTask(GameClock.class, syncTimeout);
		syncFeeTask = new SyncTask(GameClock.class, syncTimeout);
		syncFeeTask.setTag(CatpMessage.FEE);
		syncClientListTask = new SyncTask(GameClock.class, syncTimeout);
		syncClientListTask.setTag(CatpMessage.CLIENT);
		syncRegisterTask = new SyncTask(GameClock.class, syncTimeout);
		syncRegisterTask.setTag(CatpMessage.REGISTER);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		iterations = parameters.getIntWithDefault(
				base.push(GameClock.P_ITERATIONS), null, iterations);
		gameLen = parameters.getIntWithDefault(base.push(GameClock.P_GAMELEN),
				null, gameLen);
		dayLen = parameters.getIntWithDefault(base.push(GameClock.P_DAYLEN), null,
				dayLen);
		roundLen = parameters.getIntWithDefault(base.push(GameClock.P_ROUNDLEN),
				null, roundLen);
		roundClosingLen = parameters.getIntWithDefault(base
				.push(GameClock.P_ROUNDCLOSINGLEN), null, roundClosingLen);
		gameBreak = parameters.getIntWithDefault(base.push(GameClock.P_GAMEBREAK),
				null, gameBreak);
		dayBreak = parameters.getIntWithDefault(base.push(GameClock.P_DAYBREAK),
				null, dayBreak);
		roundBreak = parameters.getIntWithDefault(
				base.push(GameClock.P_ROUNDBREAK), null, roundBreak);
		gameInitLen = parameters.getIntWithDefault(base.push(GameClock.P_GAMEINIT),
				null, gameInitLen);
		dayInitLen = parameters.getIntWithDefault(base.push(GameClock.P_DAYINIT),
				null, dayInitLen);
		syncTimeout = parameters.getIntWithDefault(base
				.push(GameClock.P_SYNCTIMEOUT), null, syncTimeout);
	}

	public void initialize() {
		syncTask.setTimeout(syncTimeout);
		syncFeeTask.setTimeout(syncTimeout);
		syncClientListTask.setTimeout(syncTimeout);
		syncRegisterTask.setTimeout(syncTimeout);
	}

	protected void dispose() {
		syncTask.terminate();
		syncRegisterTask.terminate();
		syncClientListTask.terminate();
		syncFeeTask.terminate();

		clearListeners();
	}

	public boolean isActive() {
		return (thread != null) && thread.isAlive();
	}

	/**
	 * Makes this clock start to tick, not meaning the game is starting.
	 * 
	 */
	public void run() {

		registry = GameController.getInstance().getRegistry();

		AuctionEvent event = new SimulationStartedEvent();
		fireEvent(event);

		for (int i = 0; i < iterations; i++) {
			GameClock.logger.info("\n\n");
			GameClock.logger.info("Game " + i + "  (" + iterations + ")\n");
			game();
			sleep(gameBreak);
		}

		event = new SimulationOverEvent();
		fireEvent(event);

		dispose();
	}

	private void game() {

		game_start();

		day = 0;
		while (day < gameLen) {

			pause_if_requested();

			day();
			day_break();
			day++;
		}

		pause_if_requested();

		game_over();

	}

	private void game_start() {

		syncTask.setTag(CatpMessage.GAMESTARTING);
		syncTask.setCount(registry.getNumOfWorkingClients());

		// synchronize on POST TRADERS/SPECIALISTS messages
		syncClientListTask.setCount(registry.getNumOfWorkingClients() * 2);

		AuctionEvent event = new GameStartingEvent(dayLen, roundLen);
		event.setTime(new int[] { day, 0, 0 });
		fireEvent(event);

		syncTask.sync();
		syncClientListTask.sync();

		sleep(gameInitLen);

		syncTask.setTag(CatpMessage.GAMESTARTED);
		syncTask.setCount(registry.getNumOfWorkingClients());

		event = new GameStartedEvent();
		event.setTime(new int[] { day, dayLen - 1, 0 });
		fireEvent(event);

		syncTask.sync();

		// logger.info("\n\nGame started.\n");
	}

	private void game_over() {
		syncTask.setTag(CatpMessage.GAMEOVER);
		syncTask.setCount(registry.getNumOfWorkingClients());

		final AuctionEvent event = new GameOverEvent();
		event.setTime(new int[] { day, 2, 0 });
		fireEvent(event);
		// logger.info("Game over.\n");

		new PlaySound("ohyeah.wav").start();

		syncTask.sync();
	}

	private void day() {

		day_open();

		// round_break();

		round = 0;
		while (round < dayLen) {

			pause_if_requested();

			round();
			round_break();
			round++;
		}

		pause_if_requested();

		day_close();
	}

	private void day_open() {

		GameClock.logger.info("  Game day " + day + "  (" + gameLen + ")\n");

		syncTask.setTag(CatpMessage.DAYOPENING);
		syncTask.setCount(registry.getNumOfWorkingClients());

		// sync on specialist's posting price list
		syncFeeTask.setCount(registry.getNumOfWorkingSpecialists());
		AuctionEvent event = new DayOpeningEvent();
		event.setTime(new int[] { day, -1, -1 });
		fireEvent(event);

		syncTask.sync();
		syncFeeTask.sync();

		GameClock.logger.info("\t" + registry.getClientStatInfo() + "\n");

		sleep(dayInitLen);

		// sync on posting price lists to clients
		syncFeeTask.setCount(registry.getNumOfActiveSpecialists()
				* registry.getNumOfWorkingClients());
		syncTask.setTag(CatpMessage.DAYOPENED);
		syncTask.setCount(registry.getNumOfWorkingClients());
		syncRegisterTask.setCount(registry.getNumOfWorkingTraders());

		event = new DayOpenedEvent();
		event.setTime(new int[] { day, -1, -1 });
		fireEvent(event);

		syncFeeTask.sync();
		syncTask.sync();
		syncRegisterTask.sync();

		if (!(Galaxy.getInstance().getTyped(Game.P_CAT, CatpInfrastructure.class))
				.isSynchronous()) {
			// to give enough time for clients to complete subscription for info
			sleep(5 * registry.getNumOfWorkingTraders());
		}
	}

	private void day_close() {
		syncTask.setTag(CatpMessage.DAYCLOSED);
		syncTask.setCount(registry.getNumOfWorkingClients());

		final AuctionEvent event = new DayClosedEvent();
		event.setTime(new int[] { day, -1, -1 });
		fireEvent(event);

		syncTask.sync();
	}

	private void day_break() {
		sleep(dayBreak);
	}

	private void resetTicksAdjustment() {
		syncAdjustment = 0;
		adjustmentStart = -1;
	}

	private synchronized void adjustTicksStart() {
		adjustmentStart = System.currentTimeMillis();
	}

	private synchronized void adjustTicksEnd() {
		syncAdjustment += System.currentTimeMillis() - adjustmentStart;
		adjustmentStart = -1;
	}

	private void round() {
		round_start = System.currentTimeMillis();
		resetTicksAdjustment();

		syncTask.setTag(CatpMessage.ROUNDOPENED);
		syncTask.setCount(registry.getNumOfWorkingClients());

		AuctionEvent event = new RoundOpenedEvent();
		event.setTime(new int[] { day, round, -1 });
		fireEvent(event);
		GameClock.logger.info("    Game round " + round + "  (" + dayLen + ")\n");

		adjustTicksStart();
		syncTask.sync();
		adjustTicksEnd();

		sleep(roundLen);

		syncTask.setTag(CatpMessage.ROUNDCLOSING);
		syncTask.setCount(registry.getNumOfWorkingClients());

		event = new RoundClosingEvent();
		event.setTime(new int[] { day, round, -1 });
		fireEvent(event);

		adjustTicksStart();
		syncTask.sync();

		sleep(roundClosingLen);

		syncTask.setTag(CatpMessage.ROUNDCLOSED);
		syncTask.setCount(registry.getNumOfWorkingClients());

		event = new RoundClosedEvent();
		event.setTime(new int[] { day, round, -1 });
		fireEvent(event);

		syncTask.sync();
		adjustTicksEnd();
	}

	private void round_break() {
		sleep(roundBreak);
	}

	private void sleep(final int milliseconds) {
		try {
			if (!infrast.isSynchronous()) {
				Thread.sleep(milliseconds);
			}
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int[] getTime() {
		return new int[] { getDay(), getRound(), getTick() };
	}

	public String getTimeText() {
		return "Day " + getDay() + ", Round " + getRound() + ", Tick " + getTick();
	}

	public int getDay() {
		return day;
	}

	public int getRound() {
		return round;
	}

	public synchronized int getTick() {
		int tick = -1;
		if (round_start < 0) {
			tick = 0;
		} else {
			if (adjustmentStart < 0) {
				tick = (int) (System.currentTimeMillis() - round_start - syncAdjustment);
			} else {
				tick = (int) (adjustmentStart - round_start - syncAdjustment);
			}
		}

		if (tick >= roundLen) {
			tick = roundLen;
		}

		return tick;
	}

	public int getGameLen() {
		return gameLen;
	}

	public int getDayLen() {
		return dayLen;
	}

	public int getRoundLen() {
		return roundLen;
	}

	public int getDayBreak() {
		return dayBreak;
	}

	public int getRoundBreak() {
		return roundBreak;
	}

	public void addAuctionEventListener(final AuctionEventListener listener) {
		eventListeners.add(listener);
	}

	public void removeAuctionEventListener(final AuctionEventListener listener) {
		eventListeners.remove(listener);
	}

	public void clearListeners() {
		eventListeners.clear();
	}

	protected void fireEvent(final AuctionEvent event) {
		for (final AuctionEventListener listener : eventListeners) {
			listener.eventOccurred(event);
		}
	}

	public void start() {
		if (thread != null) {
			GameClock.logger.error("Thread for the game clock already exists !");
		} else {
			thread = new Thread(this);
			thread.start();
		}
	}

	/**
	 * invoked to pause the clock if pause Pause request will be detected before a
	 * new day or a new round opens.
	 * 
	 */
	private void pause_if_requested() {
		if (thread != null) {
			synchronized (thread) {
				while (paused) {
					try {
						thread.wait();
					} catch (final InterruptedException e) {
						e.printStackTrace();
						GameClock.logger.error(e);
					}
				}
			}
		}
	}

	/**
	 * notifies the clock to pause.
	 * 
	 */
	public void pause() {
		if (thread != null) {
			synchronized (thread) {
				paused = true;
			}
		}
	}

	/**
	 * notifies the clock to resume running.
	 * 
	 */
	public void resume() {
		if (thread != null) {
			synchronized (thread) {
				paused = false;
				thread.notify();
			}
		}
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + gameLen + " days; " + dayLen
				+ " rounds; " + roundLen + " ticks; " + GameClock.P_SYNCTIMEOUT + ":"
				+ syncTimeout + ")";
	}
}

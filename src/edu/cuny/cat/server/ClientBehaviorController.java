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

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;

/**
 * <p>
 * allows the game server to take actions after a client does something a
 * certain number of times, which may cause the game server unstable. Currently,
 * two behaviors are monitored, reconnection and transaction request made
 * outside rounds.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.reconnection</tt><br>
 * <font size=-1>int (5 by default)</font></td>
 * <td valign=top>(the maximal number of times a client may reconnect to the
 * game server after dropping out)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.request_at_wrong_time</tt><br>
 * <font size=-1>int (10 by default)</font></td>
 * <td valign=top>(the number of requests made by a client at a wrong time that
 * would lead to a daily ban.)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>behavior</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class ClientBehaviorController implements Parameterizable, Resetable {

	/**
	 * problem-causing behavior types
	 */

	public final static String RECONNECTION = "reconnection";

	public final static String REQUEST_AT_WRONG_TIME = "request_at_wrong_time";

	/**
	 * penalty types
	 */

	public final static String DAY_BANNING_PENALTY = "day_banning";

	public final static String CONNECTION_BANNING_PENALTY = "connection_banning";

	public final static String OTHER_PENALTY = "other";

	/**
	 * parameters
	 */

	public final static String P_RECONNECTION = ClientBehaviorController.RECONNECTION;

	public final static String P_REQUEST_AT_WRONG_TIME = ClientBehaviorController.REQUEST_AT_WRONG_TIME;

	public final static String P_DEF_BASE = "behavior";

	/**
	 * default times of bad behaviors to incur a penalty
	 */
	public final static int DEFAULT_MAX_NUMBER_OF_RECONNECTION = 5;

	public final static int DEFAULT_MAX_NUMBER_OF_REQUEST_AT_WRONG_TIME = 10;

	protected int maxReconnection;

	protected int maxWrongRequest;

	protected Map<String, Map<String, Counter>> behaviorRecords;

	protected Map<String, Map<String, Counter>> penaltyRecords;

	static Logger logger = Logger.getLogger(ClientBehaviorController.class);

	public ClientBehaviorController() {
		behaviorRecords = Collections
				.synchronizedMap(new HashMap<String, Map<String, Counter>>());
		penaltyRecords = Collections
				.synchronizedMap(new HashMap<String, Map<String, Counter>>());
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(ClientBehaviorController.P_DEF_BASE);

		maxReconnection = parameters.getIntWithDefault(base
				.push(ClientBehaviorController.P_RECONNECTION), defBase
				.push(ClientBehaviorController.P_RECONNECTION),
				ClientBehaviorController.DEFAULT_MAX_NUMBER_OF_RECONNECTION);
		maxWrongRequest = parameters.getIntWithDefault(base
				.push(ClientBehaviorController.P_REQUEST_AT_WRONG_TIME), defBase
				.push(ClientBehaviorController.P_REQUEST_AT_WRONG_TIME),
				ClientBehaviorController.DEFAULT_MAX_NUMBER_OF_REQUEST_AT_WRONG_TIME);

	}

	/**
	 * updates records after a bad behavior is observed.
	 * 
	 * @param clientId
	 *          the client that did the observed the activity
	 * @param behaviorType
	 *          the type of behavior that may lead to penalties
	 */
	public synchronized void observe(final String clientId,
			final String behaviorType) {
		Map<String, Counter> behaviorRecord = behaviorRecords.get(behaviorType);
		if (behaviorRecord == null) {
			behaviorRecord = new HashMap<String, Counter>();
			behaviorRecords.put(behaviorType, behaviorRecord);
		}

		Counter counter = behaviorRecord.get(clientId);
		if (counter == null) {
			counter = new Counter(0);
			behaviorRecord.put(clientId, counter);
		}

		counter.inc();

		if (counter.getTimes() >= getPenaltyLevel(behaviorType)) {
			addPenalty(clientId, getPenaltyType(behaviorType));
			counter.reset();
		}
	}

	/**
	 * add one unit of the specified penalty.
	 * 
	 * @param clientId
	 * @param penaltyType
	 */
	public void addPenalty(final String clientId, final String penaltyType) {
		Map<String, Counter> penaltyRecord = penaltyRecords.get(penaltyType);

		if (penaltyRecord == null) {
			penaltyRecord = new HashMap<String, Counter>();
			penaltyRecords.put(penaltyType, penaltyRecord);
		}

		final Counter counter = penaltyRecord.get(clientId);
		if (counter != null) {
			counter.inc();
		} else {
			penaltyRecord.put(clientId, new Counter(1));
		}
	}

	/**
	 * 
	 * @param clientId
	 * @param penaltyType
	 * @return units of the specified penalty pending on the client. Non-positive
	 *         values mean no penalty to impose.
	 */
	public int getPenalty(final String clientId, final String penaltyType) {
		final Map<String, Counter> penaltyRecord = penaltyRecords.get(penaltyType);

		if (penaltyRecord == null) {
			return -1;
		} else {
			final Counter counter = penaltyRecord.get(clientId);
			if (counter != null) {
				return counter.getTimes();
			} else {
				return -1;
			}
		}
	}

	/**
	 * 
	 * @param behaviorType
	 * @return the type of penalty to impose due to the specified behavior.
	 * 
	 * @see #getPenaltyLevel(String)
	 */
	public String getPenaltyType(final String behaviorType) {
		if (behaviorType == ClientBehaviorController.RECONNECTION) {
			return ClientBehaviorController.CONNECTION_BANNING_PENALTY;
		} else if (behaviorType == ClientBehaviorController.REQUEST_AT_WRONG_TIME) {
			return ClientBehaviorController.DAY_BANNING_PENALTY;
		} else {
			return ClientBehaviorController.OTHER_PENALTY;
		}
	}

	/**
	 * 
	 * @param behaviorType
	 * @return the times of behaviors to incur a unit of penalty.
	 * 
	 * @see #getPenaltyType(String)
	 */
	private int getPenaltyLevel(final String behaviorType) {
		if (behaviorType == ClientBehaviorController.RECONNECTION) {
			return maxReconnection;
		} else if (behaviorType == ClientBehaviorController.REQUEST_AT_WRONG_TIME) {
			return maxWrongRequest;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * fulfills the specified penalty one on a game client.
	 * 
	 * @param clientId
	 *          the client to be punished
	 * @param penaltyType
	 *          the type of penalty to fulfill
	 */
	public void penaltyExecuted(final String clientId, final String penaltyType) {
		final Map<String, Counter> penaltyRecord = penaltyRecords.get(penaltyType);

		if (penaltyRecord != null) {
			final Counter counter = penaltyRecord.get(clientId);
			if (counter != null) {
				counter.dec();
			} else {
				ClientBehaviorController.logger
						.error("Penalty record for penalty-client pair expected to exist !");
			}
		} else {
			ClientBehaviorController.logger
					.error("Penalty record expected to exist !");
		}
	}

	public void reset() {
		behaviorRecords.clear();
		penaltyRecords.clear();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " maxReconnection:" + maxReconnection
				+ " maxWrongRequest:" + maxWrongRequest;
	}

	class Counter {
		public int times;

		public Counter(final int initValue) {
			times = initValue;
		}

		public void reset() {
			times = 0;
		}

		public void inc() {
			times++;
		}

		public void dec() {
			times--;
		}

		public int getTimes() {
			return times;
		}
	}
}

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

package edu.cuny.cat.market.accepting;

import org.apache.log4j.Logger;

import edu.cuny.ai.learning.SlidingWindowLearner;
import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.stat.ReportVariableBoard;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Utils;

/**
 * <p>
 * A variant of {@link EquilibriumBeatingAcceptingPolicy}, which was used by
 * PSUCAT in CAT 2007 competition. It uses a sliding average of transaction
 * prices to estimate the equilibrium price and uses the standard deviation to
 * set <code>delta</code> loosing the restriction.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class SlidingAverageBeatingAcceptingPolicy extends
		QuoteBeatingAcceptingPolicy {

	static Logger logger = Logger
			.getLogger(SlidingAverageBeatingAcceptingPolicy.class);

	public static final String P_LEARNER = "learner";

	public static final String P_DEF_BASE = "equilibrium_beating_accepting";

	public static final String SLIDING_AVERAGE_TRANSACTION_PRICE = "sliding.average.transaction.price";

	/**
	 * Reusable exceptions for performance
	 */
	protected static IllegalShoutException bidException = null;

	protected static IllegalShoutException askException = null;

	protected double expectedHighestAsk;

	protected double expectedLowestBid;

	protected SlidingWindowLearner learner;

	public SlidingAverageBeatingAcceptingPolicy() {
		learner = new SlidingWindowLearner();
		init0();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		learner.setup(parameters, base
				.push(SlidingAverageBeatingAcceptingPolicy.P_LEARNER));
		learner.initialize();
	}

	private void init0() {
		expectedHighestAsk = Double.MAX_VALUE;
		expectedLowestBid = 0;
	}

	@Override
	public void reset() {
		super.reset();
		init0();
		learner.reset();
	}

	@Override
	public void check(final Shout shout) throws IllegalShoutException {
		super.check(shout);

		if (shout.isAsk()) {
			if (shout.getPrice() > expectedHighestAsk) {
				askNotAnImprovementException();
			}
		} else {
			if (shout.getPrice() < expectedLowestBid) {
				bidNotAnImprovementException();
			}
		}
	}

	protected void bidNotAnImprovementException() throws IllegalShoutException {
		if (SlidingAverageBeatingAcceptingPolicy.bidException == null) {
			// Only construct a new exception the once (for improved performance)
			SlidingAverageBeatingAcceptingPolicy.bidException = new IllegalShoutException(
					"Bid cannot beat the estimated equilibrium!");
		}
		throw SlidingAverageBeatingAcceptingPolicy.bidException;
	}

	protected void askNotAnImprovementException() throws IllegalShoutException {
		if (SlidingAverageBeatingAcceptingPolicy.askException == null) {
			// Only construct a new exception the once (for improved performance)
			SlidingAverageBeatingAcceptingPolicy.askException = new IllegalShoutException(
					"Ask cannot beat the estimated equilibrium!");
		}
		throw SlidingAverageBeatingAcceptingPolicy.askException;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof TransactionExecutedEvent) {
			learner.train(((TransactionExecutedEvent) event).getTransaction()
					.getPrice());

			if (learner.goodEnough()) {

				expectedLowestBid = learner.act() - learner.getStdDev();
				expectedHighestAsk = learner.act() + learner.getStdDev();

				ReportVariableBoard
						.getInstance()
						.reportValue(
								SlidingAverageBeatingAcceptingPolicy.SLIDING_AVERAGE_TRANSACTION_PRICE,
								learner.act());
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += "\n" + Utils.indent(learner.toString());

		return s;
	}
}

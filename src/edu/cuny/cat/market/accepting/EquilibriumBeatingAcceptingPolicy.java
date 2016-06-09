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
/*
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

import edu.cuny.ai.learning.MimicryLearner;
import edu.cuny.ai.learning.SelfKnowledgable;
import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.stat.ReportVariableBoard;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * implements the shout-accepting rule under which a shout must be more
 * competitive than an estimated equilibrium.
 * </p>
 * 
 * <p>
 * The equilibrium is estimated through some learning algorithm, e.g.
 * sliding-window-average learning and widrowhoff learning, by training with
 * transaction prices.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.delta</tt><br>
 * <font size=-1>double >= 0 (0 by default)</font></td>
 * <td valign=top>(an absolute amount to relax the restriction on shouts)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.learner</tt><br>
 * <font size=-1>name of class, implementing {@link MimicryLearner}</font></td>
 * <td valign=top>(the learning algorithm for estimating the equilibrium price)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>equilibrium_beating_accepting</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.13 $
 */

public class EquilibriumBeatingAcceptingPolicy extends
		QuoteBeatingAcceptingPolicy {

	static Logger logger = Logger
			.getLogger(EquilibriumBeatingAcceptingPolicy.class);

	/**
	 * Reusable exceptions for performance
	 */
	protected static IllegalShoutException bidException = null;

	protected static IllegalShoutException askException = null;

	protected double expectedHighestAsk;

	protected double expectedLowestBid;

	/**
	 * A parameter used to adjust the equilibrium price estimate so as to relax
	 * the restriction.
	 */
	protected double delta = 0;

	public static final String P_DELTA = "delta";

	protected MimicryLearner learner;

	public static final String P_LEARNER = "learner";

	public static final String P_DEF_BASE = "equilibrium_beating_accepting";

	public static final String EST_EQUILIBRIUM_PRICE = "estimated.equilibrium.price";

	public EquilibriumBeatingAcceptingPolicy() {
		init0();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				EquilibriumBeatingAcceptingPolicy.P_DEF_BASE);

		delta = parameters.getDoubleWithDefault(base
				.push(EquilibriumBeatingAcceptingPolicy.P_DELTA), defBase
				.push(EquilibriumBeatingAcceptingPolicy.P_DELTA), delta);

		learner = parameters.getInstanceForParameter(base
				.push(EquilibriumBeatingAcceptingPolicy.P_LEARNER), defBase
				.push(EquilibriumBeatingAcceptingPolicy.P_LEARNER),
				MimicryLearner.class);
		if (learner instanceof Parameterizable) {
			((Parameterizable) learner).setup(parameters, base
					.push(EquilibriumBeatingAcceptingPolicy.P_LEARNER));
		}
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

		if (learner != null) {
			learner.reset();
		}
	}

	/**
	 * checks whether
	 * <p>
	 * shout
	 * </p>
	 * can beat the estimated equilibrium.
	 */
	@Override
	public void check(final Shout shout) throws IllegalShoutException {
		super.check(shout);

		if (shout.isBid()) {
			if (shout.getPrice() < expectedLowestBid) {
				bidNotAnImprovementException();
			}
		} else {
			if (shout.getPrice() > expectedHighestAsk) {
				askNotAnImprovementException();
			}
		}
	}

	protected void bidNotAnImprovementException() throws IllegalShoutException {
		if (EquilibriumBeatingAcceptingPolicy.bidException == null) {
			// Only construct a new exception the once (for improved performance)
			EquilibriumBeatingAcceptingPolicy.bidException = new IllegalShoutException(
					"Bid cannot beat the estimated equilibrium!");
		}
		throw EquilibriumBeatingAcceptingPolicy.bidException;
	}

	protected void askNotAnImprovementException() throws IllegalShoutException {
		if (EquilibriumBeatingAcceptingPolicy.askException == null) {
			// Only construct a new exception the once (for improved performance)
			EquilibriumBeatingAcceptingPolicy.askException = new IllegalShoutException(
					"Ask cannot beat the estimated equilibrium!");
		}
		throw EquilibriumBeatingAcceptingPolicy.askException;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof TransactionExecutedEvent) {
			learner.train(((TransactionExecutedEvent) event).getTransaction()
					.getPrice());

			if ((learner instanceof SelfKnowledgable)
					&& ((SelfKnowledgable) learner).goodEnough()) {

				expectedLowestBid = learner.act() - delta;
				expectedHighestAsk = learner.act() + delta;

				ReportVariableBoard.getInstance().reportValue(
						EquilibriumBeatingAcceptingPolicy.EST_EQUILIBRIUM_PRICE,
						learner.act());
			}
		}
	}

	public void setDelta(final double delta) {
		this.delta = delta;
	}

	public double getDelta() {
		return delta;
	}

	public MimicryLearner getLearner() {
		return learner;
	}

	public void setLearner(final MimicryLearner learner) {
		this.learner = learner;
	}

	@Override
	public String toString() {
		String s = super.toString() + " "
				+ EquilibriumBeatingAcceptingPolicy.P_DELTA + ":" + delta;
		s += "\n" + Utils.indent(learner.toString());
		return s;
	}
}

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

package edu.cuny.cat.trader.strategy;

import java.io.Serializable;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Uniform;
import edu.cuny.ai.learning.Learner;
import edu.cuny.ai.learning.MimicryLearner;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.GameStartedEvent;
import edu.cuny.cat.event.PrivateValueAssignedEvent;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A trading strategy that learns to adapt the price to offer. It uses a
 * {@link edu.cuny.ai.learning.MimicryLearner} to learn a profit margin, which
 * together with the trader's private value determines the price to offer.
 * </p>
 * 
 * <p>
 * It also brings randomness to some extent by perturbing the price to be
 * offered.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.scaling</tt><br>
 * <font size=-1>double &gt;= 0 (by default 0.05)</font></td>
 * <td valign=top>(the maximum of perturbation on price)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.learner</tt><br>
 * <font size=-1>name of class, implementing
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(the learning algorithm to adapt price)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>momentum_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.25 $
 */

public abstract class MomentumStrategy extends AdaptiveStrategyImpl implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected MimicryLearner learner;

	protected double currentPrice;

	protected Shout lastShout = null;

	/**
	 * A parameter used to scale the randomly drawn price adjustment perturbation
	 * values.
	 */
	protected double scaling = 0.05;

	protected boolean lastShoutAccepted;

	protected double lastShoutPrice;

	protected AbstractContinousDistribution initialMarginDistribution = new Uniform(
			0.05, 0.35, Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class)
					.getEngine());

	protected AbstractContinousDistribution relativePerterbationDistribution;

	protected AbstractContinousDistribution absolutePerterbationDistribution;

	public static final String P_DEF_BASE = "momentum_strategy";

	public static final String P_SCALING = "scaling";

	public static final String P_LEARNER = "learner";

	static Logger logger = Logger.getLogger(MomentumStrategy.class);

	public MomentumStrategy() {
		this(null);
	}

	public MomentumStrategy(final AbstractTradingAgent agent) {
		super(agent);

		relativePerterbationDistribution = new Uniform(0, scaling, Galaxy
				.getInstance().getDefaultTyped(GlobalPRNG.class).getEngine());

		// the value used in the constructor depends on the value range in
		// valuation policy, therefore despite 0.05 used in Cliff's experiments (for
		// 0.50 to 2.00 valuation range), 5 is used here for the usual 50 to 200
		// range.
		absolutePerterbationDistribution = new Uniform(0, 5, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(MomentumStrategy.P_DEF_BASE);

		scaling = parameters.getDoubleWithDefault(base
				.push(MomentumStrategy.P_SCALING), defBase
				.push(MomentumStrategy.P_SCALING), scaling);

		learner = parameters.getInstanceForParameter(base
				.push(MomentumStrategy.P_LEARNER), defBase
				.push(MomentumStrategy.P_LEARNER), MimicryLearner.class);
		if (learner instanceof Parameterizable) {
			((Parameterizable) learner).setup(parameters, base
					.push(MomentumStrategy.P_LEARNER));
		}
		learner.initialize();
	}

	@Override
	public boolean modifyShout(final Shout.MutableShout shout) {
		shout.setPrice(currentPrice);
		return super.modifyShout(shout);
	}

	@Override
	public boolean requiresAuctionHistory() {
		return true;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof TransactionPostedEvent) {
			transactionPosted((TransactionPostedEvent) event);
		} else if (event instanceof ShoutPostedEvent) {
			shoutPosted((ShoutPostedEvent) event);
		} else if (event instanceof GameStartedEvent) {
			initMargin();
		} else if (event instanceof PrivateValueAssignedEvent) {
			updateCurrentPrice();
		} else if (event instanceof DayClosedEvent) {
			processDayClosed();
		}
	}

	protected void initMargin() {
		if (agent.isSeller()) {
			setMargin(initialMarginDistribution.nextDouble());
		} else {
			setMargin(-initialMarginDistribution.nextDouble());
		}
	}

	protected void processLastShout(Shout lastShout) {
		final Trader trader = lastShout.getTrader();
		lastShoutPrice = lastShout.getPrice();

		if (trader == null) {
			MomentumStrategy.logger.error("Unknown trader in posted shout at "
					+ agent.getTraderId() + " !");
			MomentumStrategy.logger.error("lastShout: " + lastShout);

			adjustMargin();
		} else if (trader.getId() == null) {
			MomentumStrategy.logger.error("Null id found in shout's trader at "
					+ agent.getTraderId() + "!");
			MomentumStrategy.logger.error("lastShout: " + lastShout);

			adjustMargin();
		} else {
			if (!trader.getId().equals(agent.getTraderId())) {
				// if not a shout placed by me
				adjustMargin();
			}
		}

		lastShout = null;
	}

	protected void shoutPosted(final ShoutPostedEvent event) {
		if (lastShout != null) {
			processLastShout(lastShout);
		}

		lastShout = event.getShout();
		lastShoutAccepted = false;
	}

	protected void transactionPosted(final TransactionPostedEvent event) {
		final Transaction transaction = event.getTransaction();
		final Shout ask = transaction.getAsk();
		final Shout bid = transaction.getBid();

		if (lastShout == null) {
			// The only possibility when this may happen is that this
			// transaction follows immediately another transaction. This is likely
			// in CH markets, but not possible in CDA markets where ZIP is designed to
			// work.

			lastShoutAccepted = false;
		} else {
			lastShoutAccepted = (lastShout.isAsk() && ask.equals(lastShout))
					|| (lastShout.isBid() && bid.equals(lastShout));

			processLastShout(lastShout);
		}
	}

	/**
	 * if there is a shout placed as the last activity, since no more shout or
	 * transaction is coming, this last shout needs to be processed just as other
	 * shouts with following shouts were done.
	 */
	protected void processDayClosed() {
		if (lastShout != null) {
			processLastShout(lastShout);
		}
	}

	public void setLearner(final Learner learner) {
		this.learner = (MimicryLearner) learner;
	}

	public Learner getLearner() {
		return learner;
	}

	public void setMargin(final double margin) {
		learner.setOutputLevel(margin);
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public Shout getLastShout() {
		return lastShout;
	}

	public boolean isLastShoutAccepted() {
		return lastShoutAccepted;
	}

	public void setScaling(final double scaling) {
		this.scaling = scaling;
	}

	public double getScaling() {
		return scaling;
	}

	private void updateCurrentPrice() {
		currentPrice = calculatePrice(learner.act());
	}

	protected double calculatePrice(final double margin) {
		if ((agent.isBuyer() && (margin <= 0.0) && (margin > -1.0))
				|| (agent.isSeller() && (margin >= 0.0))) {
			return agent.getPrivateValue() * (1 + margin);
		} else {
			return currentPrice;
		}
	}

	protected double targetMargin(final double targetPrice) {
		final double privValue = agent.getPrivateValue();
		double targetMargin = 0;
		targetMargin = (targetPrice - privValue) / privValue;

		return targetMargin;
	}

	protected void adjustMargin(final double targetMargin) {
		learner.train(targetMargin);
		updateCurrentPrice();
	}

	protected double perterb(final double price) {
		final double relative = relativePerterbationDistribution.nextDouble();
		final double absolute = absolutePerterbationDistribution.nextDouble();
		return relative * price + absolute;
	}

	protected abstract void adjustMargin();

	@Override
	public String toString() {
		String s = super.toString();

		s += " " + MomentumStrategy.P_SCALING + ":" + scaling;
		s += "\n" + Utils.indent(learner.toString());

		return s;
	}
}
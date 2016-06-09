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

import cern.jet.random.Uniform;
import edu.cuny.cat.core.IllegalShoutException;
import edu.cuny.cat.core.Shout;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * controls how relatively shouts are accepted based merely on their type, i.e.
 * asks or bids. It mimics the continuum of auction space presented in
 * <i>Evolution of Market Mechanism Through a Continuous Space of
 * Auction-Types</i>, Dave Cliff, Information Infrastructure Laboratory HP
 * Laboratories Bristol, December 14th , 2001, which introduced Q_s, denoted as
 * q below.
 * </p>
 * 
 * <p>
 * TODO: However due to the difference between the market framework in this
 * package and that in Cliff's experiments, this policy does not have a similar
 * effect. An information revelation policy should be created to filter out some
 * sort of information so as to impose control on information revelation.
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.q</tt><br>
 * <font size=-1>double [0,1] (0.5 by default)</font></td>
 * <td valign=top>(the probability to expect the next shout to be from a seller,
 * i.e. an ask)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>shout_type_based_accepting</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 */

public class ShoutTypeBasedAcceptingPolicy extends QuoteBeatingAcceptingPolicy {

	static Logger logger = Logger.getLogger(ShoutTypeBasedAcceptingPolicy.class);

	/**
	 * Reusable exceptions for performance
	 */
	protected static IllegalShoutException bidException = null;

	protected static IllegalShoutException askException = null;

	protected Uniform uniform;

	/**
	 * A parameter used to control the probability of next shout being from a
	 * seller.
	 */
	protected double q = 0.5;

	public static final String P_Q = "q";

	public static final String P_DEF_BASE = "shout_type_based_accepting";

	public ShoutTypeBasedAcceptingPolicy() {
		uniform = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				ShoutTypeBasedAcceptingPolicy.P_DEF_BASE);

		q = parameters.getDoubleWithDefault(base
				.push(ShoutTypeBasedAcceptingPolicy.P_Q), defBase
				.push(ShoutTypeBasedAcceptingPolicy.P_Q), q);
	}

	@Override
	public void check(final Shout shout) throws IllegalShoutException {
		super.check(shout);

		final double d = uniform.nextDouble();
		if (d <= q) {
			if (shout.isBid()) {
				askExpectedException();
			}
		} else {
			if (shout.isAsk()) {
				bidExpectedException();
			}
		}
	}

	protected void askExpectedException() throws IllegalShoutException {
		if (ShoutTypeBasedAcceptingPolicy.bidException == null) {
			// Only construct a new exception the once (for improved performance)
			ShoutTypeBasedAcceptingPolicy.bidException = new IllegalShoutException(
					"Ask expected!");
		}
		throw ShoutTypeBasedAcceptingPolicy.bidException;
	}

	protected void bidExpectedException() throws IllegalShoutException {
		if (ShoutTypeBasedAcceptingPolicy.askException == null) {
			// Only construct a new exception the once (for improved performance)
			ShoutTypeBasedAcceptingPolicy.askException = new IllegalShoutException(
					"Bid expected!");
		}
		throw ShoutTypeBasedAcceptingPolicy.askException;
	}

	public void setQ(final double q) {
		this.q = q;
	}

	public double getQ() {
		return q;
	}

	@Override
	public String toString() {
		return super.toString() + " q:" + q;
	}
}

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

package edu.cuny.ai.learning;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * 
 * This is deprecated. Please use {@link NArmedBanditLearner} with
 * {@link SoftmaxActionChoosingPolicy} and {@link AdaptiveReturnUpdatingPolicy}
 * (with {@link WidrowHoffLearner}) instead. It is moved from the source and
 * left here for test purposes.
 * 
 * <p>
 * A learner that implements the softmax algorithm for n-armed bandit problem
 * described in Section 2.3, Sutton and Barto's RL book.
 * </p>
 * 
 * <p>
 * <code>temperature</code> is constant when <code>alpha</code> is 1, or
 * reducing down to <code>mintemperature</code> when <code>alpha</code> is less
 * than 1.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.discount</tt><br>
 * <font size=-1>double (0, 1) (not discounting by default)</font></td>
 * <td valign=top>(the discounting factor)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>softmax_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class SoftmaxLearnerWithDiscount extends SoftmaxLearner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(SoftmaxLearnerWithDiscount.class);

	public static final String P_DISCOUNT = "discount";

	/**
	 * the discounting factor used for weighted average
	 */
	protected double discount = Double.NaN;

	public SoftmaxLearnerWithDiscount() {
		super();
	}

	public SoftmaxLearnerWithDiscount(final int numActions,
			final double temperature) {
		super(numActions, temperature);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		final Parameter defBase = new Parameter(SoftmaxLearner.P_DEF_BASE);

		discount = parameters.getDoubleWithDefault(base
				.push(SoftmaxLearnerWithDiscount.P_DISCOUNT), defBase
				.push(SoftmaxLearnerWithDiscount.P_DISCOUNT), discount);
		if (!Double.isNaN(discount)) {
			if (discount > 1) {
				discount = 1;
			} else if (discount < 0) {
				discount = 0;
			}
		}
	}

	@Override
	public void reward(final double reward) {
		// update q
		if (Double.isNaN(discount)) {
			// sample average
			q[lastAction] = q[lastAction] + (reward - q[lastAction])
					/ (1 + times[lastAction]);
		} else {
			// weighted average
			q[lastAction] = q[lastAction] + (reward - q[lastAction]) * discount;
		}

		times[lastAction]++;

		updateProbabilities();
	}

	@Override
	public String toString() {
		return super.toString() + " discount:" + discount;
	}
}

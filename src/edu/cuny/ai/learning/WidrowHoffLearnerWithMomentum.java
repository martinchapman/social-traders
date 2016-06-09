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

package edu.cuny.ai.learning;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * An extension of {@link WidrowHoffLearner} that supports the discounted effect
 * of cumulative training signal, i.e. momentum.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.momentum</tt><br>
 * <font size=-1>double [0, 1] (0.5 by default)</font></td>
 * <td valign=top>(discount rate of cumulative training signal)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.11 $
 */

public class WidrowHoffLearnerWithMomentum extends WidrowHoffLearner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(WidrowHoffLearnerWithMomentum.class);

	public static final String P_MOMENTUM = "momentum";

	public static final double DEFAULT_MOMENTUM = 0.5;

	protected double momentum;

	/**
	 * cumulative discounted delta
	 */
	protected double gamma;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		momentum = parameters.getDoubleWithDefault(base
				.push(WidrowHoffLearnerWithMomentum.P_MOMENTUM), new Parameter(
				WidrowHoffLearner.P_DEF_BASE)
				.push(WidrowHoffLearnerWithMomentum.P_MOMENTUM),
				WidrowHoffLearnerWithMomentum.DEFAULT_MOMENTUM);

		if ((momentum < 0) || (momentum > 1)) {
			WidrowHoffLearnerWithMomentum.logger.fatal("Invalid momentum in "
					+ getClass().getSimpleName() + ": " + momentum);
		}
	}

	private void init1() {
		gamma = 0;
	}

	@Override
	public void reset() {
		super.reset();
		init1();
	}

	@Override
	public void train(final double target) {
		gamma = momentum * gamma + (1 - momentum) * delta(target);
		currentOutput += gamma;
	}

	public double delta(final double target, final double current) {
		delta = learningRate * (target - current);
		return delta;
	}

	@Override
	public void randomInitialise() {
		super.randomInitialise();
		gamma = 0;
		momentum = randomParamDistribution.nextDouble();
	}

	public double getMomentum() {
		return momentum;
	}

	public void setMomentum(final double momentum) {
		this.momentum = momentum;
	}

	@Override
	public String toString() {
		return super.toString() + " momentum:" + momentum;
	}
}

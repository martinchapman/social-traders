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

import java.io.Serializable;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.io.DataWriter;

/**
 * An implementation of the Widrow-Hoff learning algorithm for 1-dimensional
 * training sets.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.learningrate</tt><br>
 * <font size=-1>double (0, 1] (0.85 by default)</font></td>
 * <td valign=top>(rate of learning)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>widrowhoff_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.18 $
 */

public class WidrowHoffLearner extends AbstractLearner implements
		MimicryLearner, Prototypeable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(WidrowHoffLearner.class);

	public static final String P_DEF_BASE = "widrowhoff_learner";

	public static final String P_LEARNINGRATE = "learningrate";

	public static final double DEFAULT_LEARNING_RATE = 0.85;

	/**
	 * The learning rate.
	 */
	protected double learningRate;

	/**
	 * The current output level.
	 */
	protected double currentOutput;

	/**
	 * The current amount of adjustment to the output.
	 */
	protected double delta;

	protected AbstractContinousDistribution randomParamDistribution = null;

	public WidrowHoffLearner() {
		this(WidrowHoffLearner.DEFAULT_LEARNING_RATE);
	}

	public WidrowHoffLearner(final double learningRate) {
		this.learningRate = learningRate;

		randomParamDistribution = new Uniform(0.1, 0.4, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		learningRate = parameters.getDoubleWithDefault(base
				.push(WidrowHoffLearner.P_LEARNINGRATE), new Parameter(
				WidrowHoffLearner.P_DEF_BASE).push(WidrowHoffLearner.P_LEARNINGRATE),
				WidrowHoffLearner.DEFAULT_LEARNING_RATE);

		if ((learningRate <= 0) || (learningRate > 1)) {
			WidrowHoffLearner.logger.fatal("Invalid learning rate in "
					+ getClass().getSimpleName() + ": " + learningRate);
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		init1();
	}

	private void init1() {
		delta = 0;
		currentOutput = 0;
	}

	public void reset() {
		init1();
	}

	public Object protoClone() {
		final WidrowHoffLearner clone = new WidrowHoffLearner(learningRate);
		return clone;
	}

	public double act() {
		return currentOutput;
	}

	public void train(final double target) {
		currentOutput += delta(target);
	}

	public double delta(final double target) {
		delta = learningRate * (target - currentOutput);
		return delta;
	}

	public void setOutputLevel(final double currentOutput) {
		this.currentOutput = currentOutput;
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}

	@Override
	public double getLearningDelta() {
		return delta;
	}

	public void setLearningRate(final double learningRate) {
		this.learningRate = learningRate;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public void randomInitialise() {
		learningRate = randomParamDistribution.nextDouble();
	}

	public double getCurrentOutput() {
		return currentOutput;
	}

	public double getDelta() {
		return delta;
	}

	@Override
	public String toString() {
		return super.toString() + " learningRate:" + learningRate;
	}
}

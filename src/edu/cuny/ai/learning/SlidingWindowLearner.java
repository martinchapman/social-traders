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

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.FixedLengthQueue;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.io.DataWriter;

/**
 * maintains a sliding window over the trained data series and use the average
 * of data items falling into the window as the output learned.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.windowsize</tt><br>
 * <font size=-1>int >= 1 (4 by default)</font></td>
 * <td valign=top>(the size of sliding window)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>sliding_window_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.17 $
 */

public class SlidingWindowLearner extends AbstractLearner implements
		MimicryLearner, SelfKnowledgable, Prototypeable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected AbstractContinousDistribution randomParamDistribution = new Uniform(
			1, 10, Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class).getEngine());

	public static final int DEFAULT_WINDOW_SIZE = 4;

	/**
	 * A parameter used to adjust the size of the window
	 */
	protected int windowSize;

	public static final String P_WINDOWSIZE = "windowsize";

	/**
	 * The current output level.
	 */
	protected double currentOutput;

	public static final String P_DEF_BASE = "sliding_window_learner";

	protected FixedLengthQueue memory;

	public SlidingWindowLearner() {
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		windowSize = parameters.getIntWithDefault(base
				.push(SlidingWindowLearner.P_WINDOWSIZE), new Parameter(
				SlidingWindowLearner.P_DEF_BASE)
				.push(SlidingWindowLearner.P_WINDOWSIZE),
				SlidingWindowLearner.DEFAULT_WINDOW_SIZE);
	}

	@Override
	public void initialize() {
		super.initialize();

		memory = new FixedLengthQueue(windowSize);
	}

	public void reset() {
		if (memory != null) {
			memory.reset();
		}
	}

	public void randomInitialise() {
		windowSize = randomParamDistribution.nextInt();
	}

	public void setWindowSize(final int windowSize) {
		this.windowSize = windowSize;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public double act() {
		return currentOutput;
	}

	public double getStdDev() {
		return memory.getStdDev();
	}

	public double getVariance() {
		return memory.getVariance();
	}

	public void train(final double target) {
		memory.newData(target);
		currentOutput = memory.getMean();
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}

	public double getCurrentOutput() {
		return currentOutput;
	}

	/**
	 * no effect on FixedLengthQueue-based next output!
	 */
	public void setOutputLevel(final double currentOutput) {
		this.currentOutput = currentOutput;
	}

	@Override
	public double getLearningDelta() {
		return 0;
	}

	public Object protoClone() {
		final SlidingWindowLearner clone = new SlidingWindowLearner();
		clone.setWindowSize(windowSize);
		return clone;
	}

	public boolean goodEnough() {
		return memory.getN() >= windowSize;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += " windowSize:" + windowSize;
		return s;
	}
}

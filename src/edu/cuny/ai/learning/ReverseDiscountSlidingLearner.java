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

import java.io.Serializable;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * tracks a series of signals, whose impacts fade over time and overlap
 * linearly. The impacts are targets to be learned, and a limited length of
 * memory is adopted.
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
 * <tr>
 * <td valign=top><i>base</i><tt>.discount</tt><br>
 * <font size=-1>double(0, 1] (0.5 by default)</font></td>
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
 * <td valign=top><tt>reverse_discount_sliding_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class ReverseDiscountSlidingLearner extends AbstractLearner implements
		MimicryLearner, SelfKnowledgable, Prototypeable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_DEF_BASE = "reverse_discount_sliding_learner";

	public static final String P_WINDOWSIZE = "windowsize";

	public static final String P_DISCOUNT = "discount";

	protected AbstractContinousDistribution randomParamDistribution = new Uniform(
			1, 10, Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class).getEngine());

	public static final int DEFAULT_WINDOW_SIZE = 4;

	/**
	 * A parameter used to adjust the size of the window
	 */
	protected int windowSize;

	public static final double DEFAULT_DISCOUNT = 0.5;

	protected double discount;

	/**
	 * The current output level.
	 */
	protected double currentOutput;

	protected double memory[];

	/**
	 * the index of the latest signal's estimated utility in memory
	 */
	protected int current;

	static Logger logger = Logger.getLogger(ReverseDiscountSlidingLearner.class);

	public ReverseDiscountSlidingLearner() {
		this(ReverseDiscountSlidingLearner.DEFAULT_WINDOW_SIZE,
				ReverseDiscountSlidingLearner.DEFAULT_DISCOUNT);
	}

	public ReverseDiscountSlidingLearner(final int windowSize,
			final double discount) {
		this.windowSize = windowSize;
		this.discount = discount;
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(
				ReverseDiscountSlidingLearner.P_DEF_BASE);

		windowSize = parameters.getIntWithDefault(base
				.push(ReverseDiscountSlidingLearner.P_WINDOWSIZE), defBase
				.push(ReverseDiscountSlidingLearner.P_WINDOWSIZE),
				ReverseDiscountSlidingLearner.DEFAULT_WINDOW_SIZE);

		discount = parameters.getDoubleWithDefault(base
				.push(ReverseDiscountSlidingLearner.P_DISCOUNT), defBase
				.push(ReverseDiscountSlidingLearner.P_DISCOUNT),
				ReverseDiscountSlidingLearner.DEFAULT_DISCOUNT);

		if ((discount > 1) || (discount <= 0)) {
			ReverseDiscountSlidingLearner.logger.error("Invalid discount value !");
			discount = ReverseDiscountSlidingLearner.DEFAULT_DISCOUNT;
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		memory = new double[windowSize];
		init1();
	}

	private void init1() {
		current = 0;
	}

	public void reset() {
		for (int i = 0; i < memory.length; i++) {
			memory[i] = 0;
		}
		init1();
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

	public void setDiscount(final double discount) {
		this.discount = discount;
	}

	public double getDiscount() {
		return discount;
	}

	public double act() {
		return currentOutput;
	}

	public void train(final double value) {
		updateMemory(value);

		// the utility for the oldest signal in memory.
		currentOutput = memory[(current + 1) % memory.length];
	}

	protected void updateMemory(final double value) {

		current = (current + 1) % memory.length;
		memory[current] = value;
		double adjust = memory[current];
		for (int i = 0; i < memory.length - 1; i++) {
			adjust *= discount;
			memory[(current + memory.length - i - 1) % memory.length] += adjust;
		}
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}

	/**
	 * gets the current utility for the <i>n</i>th latest signal.
	 * 
	 * @param n
	 *          the index of the signal to be estimated.
	 * 
	 * @return the estimated utility.
	 */
	public double getOutput(final int n) {
		return memory[(current + memory.length - n) % memory.length];
	}

	/**
	 * @return the learned utility of the oldest signal
	 */
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
		final ReverseDiscountSlidingLearner clone = new ReverseDiscountSlidingLearner();
		clone.setWindowSize(windowSize);
		clone.setDiscount(discount);
		return clone;
	}

	public boolean goodEnough() {
		return true;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent("windowSize:" + windowSize + " discount:" + discount);
		return s;
	}
}

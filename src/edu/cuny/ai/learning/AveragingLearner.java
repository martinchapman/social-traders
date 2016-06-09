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

import edu.cuny.util.Prototypeable;
import edu.cuny.util.io.DataWriter;

/**
 * predicts with the average of inputs, weighing the inputs equally, in contrast
 * to the discounting weights to inputs over time in {@link WidrowHoffLearner}.
 * 
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class AveragingLearner extends AbstractLearner implements
		MimicryLearner, SelfKnowledgable, Prototypeable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of signals received.
	 */
	protected int num;

	/**
	 * The current output level.
	 */
	protected double currentOutput;

	public AveragingLearner() {
	}

	@Override
	public void initialize() {
		super.initialize();
		init1();
	}

	private void init1() {
		num = 0;
		currentOutput = 0;
	}

	public void reset() {
		init1();
	}

	public double act() {
		return currentOutput;
	}

	public void train(final double target) {
		/* use all-time average as output */
		currentOutput = currentOutput + (target - currentOutput) / (1 + num);
		num++;
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}

	public double getCurrentOutput() {
		return currentOutput;
	}

	public void setOutputLevel(final double currentOutput) {
		this.currentOutput = currentOutput;
	}

	@Override
	public double getLearningDelta() {
		return 0;
	}

	public void randomInitialise() {
		// empty
	}

	public boolean goodEnough() {
		return num > 0;
	}

	public Object protoClone() {
		final AveragingLearner clone = new AveragingLearner();
		return clone;
	}
}

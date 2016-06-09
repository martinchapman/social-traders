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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cuny.cat.MyTestCase;
import edu.cuny.util.MathUtil;

public class WidrowHoffLearnerTest extends MyTestCase {

	WidrowHoffLearner learner1;

	double score;

	static final double LEARNING_RATE = 0.8;

	static final double TARGET_VALUE = 0.12;

	static final int ITERATIONS = 100;

	public WidrowHoffLearnerTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		learner1 = new WidrowHoffLearner(WidrowHoffLearnerTest.LEARNING_RATE);
		learner1.initialize();
	}

	public void testConvergence() {
		train(WidrowHoffLearnerTest.ITERATIONS);
		Assert.assertTrue(MathUtil.approxEqual(learner1.act(),
				WidrowHoffLearnerTest.TARGET_VALUE, 0.01));
		Assert.assertTrue(MathUtil
				.approxEqual(learner1.getLearningDelta(), 0, 0.01));
	}

	public void testReset() {
		train(2);
		Assert.assertTrue(learner1.getLearningDelta() > 0.01);
		learner1.reset();
		Assert.assertTrue(MathUtil.approxEqual(learner1.getLearningDelta(), 0,
				0.00001));
	}

	protected void train(final int iterations) {
		for (int i = 0; i < iterations; i++) {
			learner1.train(WidrowHoffLearnerTest.TARGET_VALUE);
			System.out.println("Learning delta = " + learner1.getLearningDelta());
			System.out.println("Current output = " + learner1.act());
		}
	}

	public static Test suite() {
		return new TestSuite(WidrowHoffLearnerTest.class);
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(WidrowHoffLearnerTest.suite());
	}
}

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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.PRNGTestSeeds;
import edu.cuny.cat.MyTestCase;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.CumulativeDistribution;
import edu.cuny.util.Galaxy;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class ReverseDiscountSlidingLearnerTest extends MyTestCase {

	ReverseDiscountSlidingLearner learner;

	double score;

	static Logger logger = Logger
			.getLogger(ReverseDiscountSlidingLearnerTest.class);

	GlobalPRNG prng;

	public ReverseDiscountSlidingLearnerTest(final String name) {
		super(name);
		prng = Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class);
	}

	@Override
	public void setUp() {
		super.setUp();
		prng.initializeWithSeed(PRNGTestSeeds.UNIT_TEST_SEED);
		learner = new ReverseDiscountSlidingLearner(
				ReverseDiscountSlidingLearner.DEFAULT_WINDOW_SIZE,
				ReverseDiscountSlidingLearner.DEFAULT_DISCOUNT);
	}

	public void testZeroInput() {
		System.out.println("\n>>>>>>>>>\t " + "testZeroInput() \n");

		learner.initialize();
		for (int i = 0; i < 2 * ReverseDiscountSlidingLearner.DEFAULT_WINDOW_SIZE; i++) {
			learner.train(0);
			Assert.assertTrue(learner.act() == 0);
		}
	}

	public void testNoDiscount() {
		System.out.println("\n>>>>>>>>>\t " + "testNoDiscount() \n");

		final double data[] = { 1.3, 45, 5, 78.9, 0, 0.145, 42, 43, 568 };

		learner.setDiscount(1);
		learner.setWindowSize(data.length);
		learner.initialize();

		final CumulativeDistribution distribution = new CumulativeDistribution();

		for (final double element : data) {
			distribution.newData(element);
			learner.train(element);
		}
		Assert.assertTrue(distribution.getTotal() == learner.act());
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(ReverseDiscountSlidingLearnerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(ReverseDiscountSlidingLearnerTest.class);
	}
}
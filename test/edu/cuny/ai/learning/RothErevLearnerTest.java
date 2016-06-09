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
import edu.cuny.PRNGTestSeeds;
import edu.cuny.cat.MyTestCase;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.CumulativeDistribution;
import edu.cuny.util.Galaxy;

public class RothErevLearnerTest extends MyTestCase {

	static final int CORRECT_ACTION = 2;

	NPTRothErevLearner learner1;

	GlobalPRNG prng;

	public RothErevLearnerTest(final String name) {
		super(name);
		prng = Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class);
	}

	@Override
	public void setUp() {
		super.setUp();
		prng.initializeWithSeed(PRNGTestSeeds.UNIT_TEST_SEED);
		learner1 = new NPTRothErevLearner(10, 0.2, 0.2, 100.0);
	}

	public void testBasic() {
		System.out.println("\n>>>>>>>>>\t " + "testBasic() \n");

		learner1.setExperimentation(0.99);
		learner1.initialize();
		final CumulativeDistribution stats = new CumulativeDistribution("action");
		int correctActions = 0;
		for (int i = 0; i < 100; i++) {
			final int action = learner1.act();
			stats.newData(action);
			if (action == RothErevLearnerTest.CORRECT_ACTION) {
				learner1.reward(1.0);
				correctActions++;
			} else {
				learner1.reward(0);
			}
			RothErevLearnerTest.checkProbabilities(learner1);
		}
		System.out.println("final state of learner1 = " + learner1);
		System.out.println("learner1 score = " + correctActions + "%");
		System.out.println("learner1 peaks = " + learner1.countPeaks());
		System.out.println(stats);
	}

	public void testPeaks() {
		System.out.println("\n>>>>>>>>>\t " + "testPeaks() \n");

		learner1.initialize();

		final double q[] = { 12, 15, 12, 10, 16, 17, 0, 0, 0, 0 };
		learner1.setPropensities(q);
		final int peaks = learner1.countPeaks();
		System.out.println(learner1);
		System.out.println("Number of peaks = " + peaks);
		Assert.assertTrue(peaks == 2);
	}

	public void testDistribution() {
		System.out.println("\n>>>>>>>>>\t " + "testDistribution() \n");

		final double q[] = { 55, 5, 5, 5, 5, 5, 5, 5, 5, 5 };
		final CumulativeDistribution action1Data = new CumulativeDistribution(
				"action1");
		for (int r = 0; r < 10000; r++) {
			learner1 = new NPTRothErevLearner(10, 0.2, 0.2, 1);
			learner1.initialize();
			learner1.setPropensities(q);
			final CumulativeDistribution choiceData = new CumulativeDistribution(
					"choice");
			int action1Chosen = 0;
			for (int i = 0; i < 100; i++) {
				final int choice = learner1.act();
				choiceData.newData(choice);
				action1Chosen = 0;
				if (choice == 0) {
					action1Chosen = 1;
				}
				action1Data.newData(action1Chosen);
			}
		}
		System.out.println(action1Data);
		Assert.assertTrue((action1Data.getMean() <= 0.57)
				&& (action1Data.getMean() >= 0.53));
	}

	public static void checkProbabilities(final RothErevLearner l) {
		double prob = 0;
		for (int i = 0; i < l.getK(); i++) {
			prob += l.getProbability(i);
		}
		if ((prob > 1.001) || (prob < 0.999)) {
			throw new Error("Probabilities should sum to 1");
		}
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(RothErevLearnerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(RothErevLearnerTest.class);
	}
}
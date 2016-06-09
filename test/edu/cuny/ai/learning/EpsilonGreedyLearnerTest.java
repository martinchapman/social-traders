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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.ai.learning.prob.DymEqualRewardsNArmedBanditProb;
import edu.cuny.ai.learning.prob.DymVaryingRewardsNArmedBanditProb;
import edu.cuny.ai.learning.prob.NArmedBanditProb;
import edu.cuny.ai.learning.prob.StaticEqualRewardsNArmedBanditProb;
import edu.cuny.ai.learning.prob.StaticVaryingRewardsNArmedBanditProb;

/**
 * This is deprecated. It is left here for testing
 * {@link NArmedBanditLearnerTest}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class EpsilonGreedyLearnerTest extends ExposedStimuliResponseLearnerTest {

	static Logger logger = Logger.getLogger(EpsilonGreedyLearnerTest.class);

	public EpsilonGreedyLearnerTest(final String name) {
		super(name);
	}

	public void testRandom() {
		System.out.println("\n>>>>>>>>>\t " + "testRandom() \n");

		final EpsilonGreedyLearner learner = new EpsilonGreedyLearner();
		learner.setAlpha(1);
		learner.setInitialEpsilon(1);
		learner.initialize();

		ExposedStimuliResponseLearnerTest.checkEqualChoosing(learner,
				new StaticEqualRewardsNArmedBanditProb());
		ExposedStimuliResponseLearnerTest.checkEqualChoosing(learner,
				new StaticVaryingRewardsNArmedBanditProb());
		ExposedStimuliResponseLearnerTest.checkEqualChoosing(learner,
				new DymEqualRewardsNArmedBanditProb());
		ExposedStimuliResponseLearnerTest.checkEqualChoosing(learner,
				new DymVaryingRewardsNArmedBanditProb());
	}

	public static double[] calculateExpectedFrequenciesWithBias(
			final NArmedBanditProb prob, final int times, final double epsilon) {
		final double otherActionExpectedFreq = times * epsilon
				/ prob.getNumOfArms();
		final double bestActionExpectedFreq = otherActionExpectedFreq + times
				* (1 - epsilon);
		final double expectedFrequencies[] = new double[prob.getNumOfArms()];
		for (int i = 0; i < prob.getNumOfArms(); i++) {
			if (i == prob.getBestArm()) {
				expectedFrequencies[i] = bestActionExpectedFreq;
			} else {
				expectedFrequencies[i] = otherActionExpectedFreq;
			}
		}

		return expectedFrequencies;
	}

	public void testBiasedSelection() {
		System.out.println("\n>>>>>>>>>\t " + "testBiasedSelection() \n");
		final double epsilon = 0.5;

		final EpsilonGreedyLearner learner = new EpsilonGreedyLearner();
		learner.setAlpha(1);
		learner.setInitialEpsilon(epsilon);
		learner.initialize();

		ExposedStimuliResponseLearnerTest.checkEqualChoosing(learner,
				new StaticEqualRewardsNArmedBanditProb());
		ExposedStimuliResponseLearnerTest.checkEqualChoosing(learner,
				new DymEqualRewardsNArmedBanditProb());

		final int times = 1000000;
		NArmedBanditProb prob = new StaticVaryingRewardsNArmedBanditProb();
		double expectedFrequencies[] = EpsilonGreedyLearnerTest
				.calculateExpectedFrequenciesWithBias(prob, times, epsilon);
		ExposedStimuliResponseLearnerTest.checkBiasedChoosing(learner, prob,
				expectedFrequencies, times);

		prob = new DymVaryingRewardsNArmedBanditProb();
		expectedFrequencies = EpsilonGreedyLearnerTest
				.calculateExpectedFrequenciesWithBias(prob, times, epsilon);
		ExposedStimuliResponseLearnerTest.checkBiasedChoosing(learner, prob,
				expectedFrequencies, times);
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(EpsilonGreedyLearnerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(EpsilonGreedyLearnerTest.class);
	}
}
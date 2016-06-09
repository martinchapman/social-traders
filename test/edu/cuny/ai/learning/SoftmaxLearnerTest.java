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

public class SoftmaxLearnerTest extends ExposedStimuliResponseLearnerTest {

	static Logger logger = Logger.getLogger(SoftmaxLearnerTest.class);

	public SoftmaxLearnerTest(final String name) {
		super(name);
	}

	public void testRandom() {
		System.out.println("\n>>>>>>>>>\t " + "testRandom() \n");

		final SoftmaxLearner learner = new SoftmaxLearner();
		learner.setAlpha(1);
		// it seems 1000.0 is big enough to emulate a random softmax choosing
		learner.setInitialTemperature(1000.0);
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

	/**
	 * TODO: to replace with softmax calculation.
	 * 
	 * @param prob
	 * @param times
	 * @param temperature
	 * @return
	 */
	public static double[] calculateExpectedFrequenciesWithBias(
			final NArmedBanditProb prob, final int times, final double temperature) {
		final double otherActionExpectedFreq = times * temperature
				/ prob.getNumOfArms();
		final double bestActionExpectedFreq = otherActionExpectedFreq + times
				* (1 - temperature);
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

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(SoftmaxLearnerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(SoftmaxLearnerTest.class);
	}
}
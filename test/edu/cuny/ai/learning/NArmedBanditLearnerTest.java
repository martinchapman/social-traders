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
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class NArmedBanditLearnerTest extends ExposedStimuliResponseLearnerTest {

	static Logger logger = Logger.getLogger(NArmedBanditLearnerTest.class);

	public NArmedBanditLearnerTest(final String name) {
		super(name);
	}

	protected void checkAveragingRandom(final ActionChoosingPolicy choosingPolicy) {

		final NArmedBanditLearner learner = new NArmedBanditLearner();

		learner.setActionChoosingPolicy(choosingPolicy);

		final AdaptiveReturnUpdatingPolicy updatingPolicy = new AdaptiveReturnUpdatingPolicy();
		updatingPolicy.setParentLearner(learner);
		updatingPolicy.setLearnerTemplate(new AveragingLearner());
		updatingPolicy.initialize();
		learner.setReturnUpdatingPolicy(updatingPolicy);

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

	public void testAveragingRandom() {
		System.out.println("\n>>>>>>>>>\t " + "testAveragingRandom() \n");

		final ActionChoosingPolicy choosingPolicy = new RandomActionChoosingPolicy();
		checkAveragingRandom(choosingPolicy);
	}

	public void testAveragingEpsilonRandom() {
		System.out.println("\n>>>>>>>>>\t " + "testAveragingEpsilonRandom() \n");

		final EpsilonGreedyActionChoosingPolicy choosingPolicy = new EpsilonGreedyActionChoosingPolicy();
		choosingPolicy.setAlpha(1);
		choosingPolicy.setInitialEpsilon(1);
		choosingPolicy.initialize();

		checkAveragingRandom(choosingPolicy);
	}

	public void testAveragingSoftmaxRandom() {
		System.out.println("\n>>>>>>>>>\t " + "testAveragingSoftmaxRandom() \n");

		final SoftmaxActionChoosingPolicy choosingPolicy = new SoftmaxActionChoosingPolicy();
		choosingPolicy.setAlpha(1);

		// it seems 1000.0 is big enough to emulate a random softmax choosing
		choosingPolicy.setInitialTemperature(1000.0);
		// choosingPolicy.setInitialTemperature(Double.MAX_VALUE);
		choosingPolicy.initialize();

		checkAveragingRandom(choosingPolicy);
	}

	public void testAveragingEpsilonBiased() {
		System.out.println("\n>>>>>>>>>\t " + "testAveragingEpsilonBiased() \n");

		final NArmedBanditLearner learner = new NArmedBanditLearner();

		final double epsilon = 0.5;

		final EpsilonGreedyActionChoosingPolicy choosingPolicy = new EpsilonGreedyActionChoosingPolicy();
		choosingPolicy.setAlpha(1);
		choosingPolicy.setInitialEpsilon(epsilon);
		choosingPolicy.initialize();
		learner.setActionChoosingPolicy(choosingPolicy);

		final AdaptiveReturnUpdatingPolicy updatingPolicy = new AdaptiveReturnUpdatingPolicy();
		updatingPolicy.setParentLearner(learner);
		updatingPolicy.setLearnerTemplate(new AveragingLearner());
		updatingPolicy.initialize();
		learner.setReturnUpdatingPolicy(updatingPolicy);

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

		learner.reset();
		prob = new DymVaryingRewardsNArmedBanditProb();
		expectedFrequencies = EpsilonGreedyLearnerTest
				.calculateExpectedFrequenciesWithBias(prob, times, epsilon);
		ExposedStimuliResponseLearnerTest.checkBiasedChoosing(learner, prob,
				expectedFrequencies, times);
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(NArmedBanditLearnerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(NArmedBanditLearnerTest.class);
	}
}
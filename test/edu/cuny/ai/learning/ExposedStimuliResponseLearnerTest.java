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

import edu.cuny.PRNGTestSeeds;
import edu.cuny.ai.learning.prob.NArmedBanditProb;
import edu.cuny.cat.MyTestCase;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;

/**
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class ExposedStimuliResponseLearnerTest extends MyTestCase {

	static Logger logger = Logger
			.getLogger(ExposedStimuliResponseLearnerTest.class);

	GlobalPRNG prng;

	public ExposedStimuliResponseLearnerTest(final String name) {
		super(name);
		prng = Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class);
	}

	@Override
	public void setUp() {
		super.setUp();
		prng.initializeWithSeed(PRNGTestSeeds.UNIT_TEST_SEED);
	}

	protected static double[] runLearner(final StimuliResponseLearner learner,
			final NArmedBanditProb prob, final int times) {
		return ExposedStimuliResponseLearnerTest.runLearner(learner, prob, times,
				times);
	}

	/**
	 * runs the learner to play with the given n-armed bandit problem for the
	 * given number of times.
	 * 
	 * @param learner
	 * @param prob
	 * @param trainingTimes
	 * @param OfficialTimes
	 * @return the number of times actions in the n-armed problem are taken
	 */
	protected static double[] runLearner(final StimuliResponseLearner learner,
			final NArmedBanditProb prob, final int trainingTimes,
			final int OfficialTimes) {
		learner.setNumberOfActions(prob.getNumOfArms());
		learner.initialize();
		learner.reset();

		// to train the learner first without counting the frequencies
		for (int i = 0; i < trainingTimes; i++) {
			final int act = learner.act();
			learner.reward(prob.pull(act));
		}

		// start to count after the learner is trained
		final double frequencies[] = new double[prob.getNumOfArms()];
		for (int i = 0; i < OfficialTimes; i++) {
			final int act = learner.act();
			frequencies[act]++;
			learner.reward(prob.pull(act));
		}

		return frequencies;
	}

	/**
	 * the learner is expected to solve the problem by choosing actions at an
	 * equal frquency.
	 * 
	 * @param learner
	 * @param prob
	 */
	protected static void checkEqualChoosing(
			final ExposedStimuliResponseLearner learner, final NArmedBanditProb prob) {

		final int times = 500000;
		final double frequencies[] = ExposedStimuliResponseLearnerTest.runLearner(
				learner, prob, times, times);

		final int avg = times / frequencies.length;

		MyTestCase.checkEquals(
				"Actions should have been chosen approximately equally often.",
				frequencies, avg, 0.01);
		MyTestCase.checkEquals(
				"Returns of actions should approach to the expected value.", learner
						.getReturns(), prob.getExpectedReturns(), 0.01);
	}

	/**
	 * The learner is expected to solve the problem by choosing actions biasing
	 * towards optimal actions.
	 * 
	 * @param learner
	 * @param prob
	 */
	protected static void checkBiasedChoosing(
			final ExposedStimuliResponseLearner learner, final NArmedBanditProb prob,
			final double expectedFrequencies[], final int times) {

		final double frequencies[] = ExposedStimuliResponseLearnerTest.runLearner(
				learner, prob, times, times);

		// logger.info("prob: " + prob);
		// logger.info("frequencies: " + CatpMessage.concatenate(frequencies));
		// logger.info("returns: " + CatpMessage.concatenate(learner.getReturns()));
		// logger.info("expectedFrequencies: " +
		// CatpMessage.concatenate(expectedFrequencies));

		// final int avg = times / frequencies.length;
		MyTestCase.checkEquals(
				"Optimal action should have been chosen more often than others.",
				frequencies, expectedFrequencies, 0.01);
		MyTestCase.checkEquals(
				"Returns of actions should approach to the expected value.", learner
						.getReturns(), prob.getExpectedReturns(), 0.01);
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(ExposedStimuliResponseLearnerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(ExposedStimuliResponseLearnerTest.class);
	}
}
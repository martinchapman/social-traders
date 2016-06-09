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
 * @version $Revision: 1.12 $
 */

public class QLearnerTest extends MyTestCase {

	QLearner learner1;

	double score;

	static final double EPSILON = 0.05;

	static final double LEARNING_RATE = 0.8;

	static final double DISCOUNT_RATE = 0.9;

	static final int NUM_ACTIONS = 10;

	static final int CORRECT_ACTION = 2;

	static final int NUM_TRIALS = 20000;

	static Logger logger = Logger.getLogger(QLearnerTest.class);

	GlobalPRNG prng;

	public QLearnerTest(final String name) {
		super(name);
		prng = Galaxy.getInstance().getDefaultTyped(GlobalPRNG.class);
	}

	@Override
	public void setUp() {
		super.setUp();
		prng.initializeWithSeed(PRNGTestSeeds.UNIT_TEST_SEED);
		createLearner();
	}

	protected void createLearner() {
		learner1 = new QLearner(1, QLearnerTest.NUM_ACTIONS, QLearnerTest.EPSILON,
				QLearnerTest.LEARNING_RATE, QLearnerTest.DISCOUNT_RATE);
		score = 0;
	}

	public void testBestAction() {
		System.out.println("\n>>>>>>>>>\t " + "testBestAction() \n");

		learner1.setEpsilon(0.0);
		learner1.initialize();

		final CumulativeDistribution stats = new CumulativeDistribution("action");
		int correctActions = 0;
		for (int i = 0; i < QLearnerTest.NUM_TRIALS; i++) {
			final int action = learner1.act();
			Assert.assertTrue(action == learner1.bestAction(0));
			stats.newData(action);
			if (action == QLearnerTest.CORRECT_ACTION) {
				learner1.newState(1.0, 0);
				correctActions++;
			} else {
				learner1.newState(0.0, 0);
			}
		}
		System.out.println("final state of learner1 = " + learner1);
		System.out.println("learner1 score = " + score(correctActions) + "%");
		System.out.println(stats);
	}

	public void testMinimumScore() {
		System.out.println("\n>>>>>>>>>\t " + "testMinimumScore() \n");

		learner1.initialize();

		final CumulativeDistribution stats = new CumulativeDistribution("action");
		int correctActions = 0;
		int bestActionChosen = 0;
		for (int i = 0; i < QLearnerTest.NUM_TRIALS; i++) {
			final int action = learner1.act();
			stats.newData(action);
			Assert.assertTrue(action == learner1.getLastActionChosen());
			final int bestAction = learner1.bestAction(0);
			if (bestAction == action) {
				bestActionChosen++;
			}
			if (action == QLearnerTest.CORRECT_ACTION) {
				learner1.newState(1.0, 0);
				correctActions++;
			} else {
				learner1.newState(0.0, 0);
			}
		}
		System.out.println("final state of learner1 = " + learner1);
		final double score = score(correctActions);
		final double bestActionPercent = score(bestActionChosen);
		System.out.println("learner1 score = " + score + "%");
		System.out.println(stats);
		System.out.println("chose best action " + bestActionPercent
				+ "% of the time.");
		Assert.assertTrue(score > 80);
		Assert.assertTrue(1 - (bestActionPercent / 100) <= QLearnerTest.EPSILON);
	}

	public void testStates() {
		System.out.println("\n>>>>>>>>>\t " + "testStates() \n");

		checkStates();
	}

	protected void checkStates() {

		final int[] correctChoices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		int correctActions = 0;
		learner1.setStatesAndActions(correctChoices.length,
				QLearnerTest.NUM_ACTIONS);
		learner1.initialize();
		int s = 3;
		for (int i = 0; i < QLearnerTest.NUM_TRIALS; i++) {
			final int action = learner1.act();
			double reward = 0;
			if (action == correctChoices[s]) {
				reward = 1.0;
				correctActions++;
				if (++s > 9) {
					s = 0;
				}
			}
			learner1.newState(reward, s);
			Assert.assertTrue(learner1.getState() == s);
		}
		score = score(correctActions);
		System.out.println("score = " + score + "%");
		System.out.println("learner1 = " + learner1);
		Assert.assertTrue(score >= 70);
	}

	public void testReset() {
		System.out.println("\n>>>>>>>>>\t " + "testReset() \n");

		// use the same random number generator
		prng.setUseMultiEngine(false);

		prng.initializeWithSeed(1);

		createLearner();
		learner1.initialize();
		System.out.println("virgin learner1 = " + learner1);
		checkStates();
		final double score1 = score;
		System.out.println("score1 = " + score1);

		prng.initializeWithSeed(1);
		createLearner();
		learner1.initialize();
		learner1.reset();
		System.out.println("reseted learner1 = " + learner1);
		checkStates();
		final double score2 = score;
		System.out.println("score2 = " + score2);

		Assert.assertTrue(score1 == score2);
	}

	public double score(final int numCorrect) {
		return ((double) numCorrect / (double) QLearnerTest.NUM_TRIALS) * 100;
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(QLearnerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(QLearnerTest.class);
	}
}
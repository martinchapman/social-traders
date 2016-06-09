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

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuny.random.DiscreteProbabilityDistribution;
import edu.cuny.util.CumulativeDistribution;
import edu.cuny.util.MathUtil;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * <p>
 * A class implementing the Roth-Erev learning algorithm. This learning
 * algorithm is designed to mimic human-like behaviour in extensive form games.
 * See:
 * </p>
 * <p>
 * A.E.Roth and I. Erev "Learning in extensive form games: experimental data and
 * simple dynamic models in the intermediate term" Games and Economic Behiour,
 * Volume 8
 * </p>
 * 
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.k</tt><br>
 * <font size=-1>int >= 0</font></td>
 * <td valign=top>(the number of a possible actions)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.recency</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(the recency parameter)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.experimentation</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(the experimentation parameter)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.scaling</tt><br>
 * <font size=-1>int >= 0</font></td>
 * <td valign=top>(the scaling parameter)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>rotherev_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.18 $
 * 
 */

public class RothErevLearner extends AbstractLearner implements Prototypeable,
		StimuliResponseLearner, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of choices available to make at each iteration.
	 */
	protected int k;

	/**
	 * The recency parameter.
	 */
	protected double recency;

	/**
	 * The experimentation parameter.
	 */
	protected double experimentation;

	/**
	 * The scaling parameter.
	 */
	protected double scaling;

	/**
	 * Propensity for each possible action.
	 */
	protected double propensities[];

	/**
	 * Probabilities for each possible action.
	 */
	protected DiscreteProbabilityDistribution probabilityDistribution;

	/**
	 * The current iteration.
	 */
	protected int iteration;

	/**
	 * The last action chosen.
	 */
	protected int lastAction;

	/**
	 * The total amount of update to the probability vector on the last iteration.
	 */
	protected double deltaP;

	static final int DEFAULT_K = 100;

	static final double DEFAULT_RECENCY = 0.1;

	static final double DEFAULT_EXPERIMENTATION = 0.2;

	static final double DEFAULT_SCALING = 1.0;

	static final String P_DEF_BASE = "rotherev_learner";

	static Logger logger = Logger.getLogger(RothErevLearner.class);

	public RothErevLearner() {
		this(RothErevLearner.DEFAULT_K, RothErevLearner.DEFAULT_RECENCY,
				RothErevLearner.DEFAULT_EXPERIMENTATION,
				RothErevLearner.DEFAULT_SCALING);
	}

	/**
	 * Construct a new learner.
	 * 
	 * @param k
	 *          The no. of possible actions.
	 * @param recency
	 *          The recency parameter.
	 * @param experimentation
	 *          The experimentation parameter.
	 */
	public RothErevLearner(final int k, final double recency,
			final double experimentation, final double scaling) {
		this.k = k;
		this.recency = recency;
		this.experimentation = experimentation;
		this.scaling = scaling;
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(RothErevLearner.P_DEF_BASE);

		k = parameters.getIntWithDefault(base.push("k"), defBase.push("k"),
				RothErevLearner.DEFAULT_K);
		recency = parameters.getDoubleWithDefault(base.push("recency"), defBase
				.push("recency"), RothErevLearner.DEFAULT_RECENCY);
		experimentation = parameters.getDoubleWithDefault(base
				.push("experimentation"), defBase.push("experimentation"),
				RothErevLearner.DEFAULT_EXPERIMENTATION);
		scaling = parameters.getDoubleWithDefault(base.push("scaling"), defBase
				.push("scaling"), RothErevLearner.DEFAULT_SCALING);
	}

	@Override
	public void initialize() {
		super.initialize();

		validateParams();
		init1();
	}

	private void validateParams() {
		if (!(k > 0)) {
			throw new IllegalArgumentException("k must be positive");
		}
		if (!((recency >= 0) && (recency <= 1))) {
			throw new IllegalArgumentException("recency must range [0..1]");
		}
		if (!((experimentation >= 0) && (experimentation <= 1))) {
			throw new IllegalArgumentException("experimentation must range [0..1]");
		}
	}

	private void init1() {
		iteration = 0;
		propensities = new double[k];
		probabilityDistribution = new DiscreteProbabilityDistribution(k);
		resetDistributions();
	}

	public void reset() {
		init1();
	}

	public Object protoClone() {
		RothErevLearner clonedLearner;
		try {
			clonedLearner = (RothErevLearner) clone();
			clonedLearner.probabilityDistribution = (DiscreteProbabilityDistribution) probabilityDistribution
					.protoClone();
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
		return clonedLearner;
	}

	/**
	 * Generate the next action for this learner.
	 * 
	 * @return An int in the range [0..k) representing the choice made by the
	 *         learner.
	 */
	public int act() {
		final int action = choose();
		lastAction = action;
		iteration++;
		return action;
	}

	/**
	 * TODO: has yet to implement
	 */
	public int act(final Set<Integer> actions) {
		RothErevLearner.logger.error(getClass()
				+ ".act(Set actions) has yet to be implemented !");
		return -1;
	}

	/**
	 * Reward the last action taken by the learner according to some payoff.
	 * 
	 * @param reward
	 *          The payoff for the last action taken by the learner.
	 */
	public void reward(final double reward) {
		updatePropensities(lastAction, reward);
		updateProbabilities();
	}

	/**
	 * Choose a random number according to the probability distribution defined by
	 * p.
	 * 
	 * @return one of [0..k) according to the probabilities p[0..k-1].
	 */
	public int choose() {
		return probabilityDistribution.generateRandomEvent();
	}

	public void resetDistributions() {
		final double initialPropensity = scaling / k;
		for (int i = 0; i < k; i++) {
			propensities[i] = initialPropensity;
		}
		updateProbabilities();
	}

	/**
	 * Update the propensities for each possible action.
	 * 
	 * @param action
	 *          The last action chosen by the learner
	 */
	protected void updatePropensities(final int action, final double reward) {
		for (int i = 0; i < k; i++) {
			propensities[i] = (1 - recency) * propensities[i]
					+ experience(i, action, reward);
		}
	}

	/**
	 * Update the probabilities.
	 */
	protected void updateProbabilities() {
		double sigmaQ = 0;
		for (int i = 0; i < k; i++) {
			sigmaQ += propensities[i];
		}
		if (sigmaQ <= 10E-10) {
			resetDistributions();
			return;
		}
		deltaP = 0;
		for (int i = 0; i < k; i++) {
			final double p1 = propensities[i] / sigmaQ;
			deltaP += MathUtil.diffSq(probabilityDistribution.getProbability(i), p1);
			probabilityDistribution.setProbability(i, p1);
		}
	}

	/**
	 * The experience function
	 * 
	 * @param i
	 *          The action under consideration
	 * 
	 * @param action
	 *          The last action chosen
	 */
	public double experience(final int i, final int action, final double reward) {
		if (i == action) {
			return reward * (1 - experimentation);
		} else {
			return reward * (experimentation / (k - 1));
		}
	}

	/**
	 * Replace the current propensities with the supplied propensity array.
	 * 
	 * @param propensities
	 *          The new propensity array to use.
	 */
	public void setPropensities(final double propensities[]) {
		this.propensities = propensities;
		updateProbabilities();
	}

	public void setRecency(final double recency) {
		this.recency = recency;
		validateParams();
	}

	public void setExperimentation(final double experimentation) {
		this.experimentation = experimentation;
		validateParams();
	}

	public void setScaling(final double scaling) {
		this.scaling = scaling;
	}

	/**
	 * Count the number of peaks in the probability distribution.
	 * 
	 * @return The number of peaks in the distribution.
	 */
	public int countPeaks() {
		int peaks = 0;
		double lastValue = 0;
		double lastDelta = 0;
		double delta = 0;
		for (int i = 0; i < k; i++) {
			delta = propensities[i] - lastValue;
			if (Math.abs(delta) < 1.0 / (k * 100000)) {
				delta = 0;
			}
			if ((delta < 0)
					&& (RothErevLearner.sign(delta) != RothErevLearner.sign(lastDelta))) {
				peaks++;
			}
			lastDelta = delta;
			lastValue = propensities[i];
		}
		return peaks;
	}

	/**
	 * Compute modes of the probability distribution p.
	 */
	public void computeDistributionStats(final CumulativeDistribution stats) {
		probabilityDistribution.computeStats(stats);
	}

	private static int sign(final double value) {
		return (new Double(value)).compareTo(new Double(0));
	}

	@Override
	public void dumpState(final DataWriter out) {
		for (int i = 0; i < k; i++) {
			out.newData(probabilityDistribution.getProbability(i));
		}
	}

	public int getK() {
		return k;
	}

	public int getNumberOfActions() {
		return getK();
	}

	public void setNumberOfActions(final int numActions) {
		k = numActions;
	}

	@Override
	public double getLearningDelta() {
		return deltaP;
	}

	public double getProbability(final int i) {
		return probabilityDistribution.getProbability(i);
	}

	public double getExperimentation() {
		return experimentation;
	}

	public int getIteration() {
		return iteration;
	}

	public int getLastAction() {
		return lastAction;
	}

	public double getRecency() {
		return recency;
	}

	public double getScaling() {
		return scaling;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent(" k:" + k + " recency:" + recency + " experimentation:"
						+ experimentation + " scaling:" + scaling);
		return s;
	}

}

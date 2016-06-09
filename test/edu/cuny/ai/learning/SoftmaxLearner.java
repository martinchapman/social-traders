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
import java.util.Set;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * This is deprecated. Please use {@link NArmedBanditLearner} with
 * {@link SoftmaxActionChoosingPolicy} and {@link AdaptiveReturnUpdatingPolicy}
 * (with {@link AveragingLearner}) instead. It is moved from the source and left
 * here for test purposes.
 * 
 * <p>
 * A learner that implements the softmax algorithm for n-armed bandit problem
 * described in Section 2.3, Sutton and Barto's RL book.
 * </p>
 * 
 * <p>
 * <code>temperature</code> is constant when <code>alpha</code> is 1, or
 * reducing down to <code>mintemperature</code> when <code>alpha</code> is less
 * than 1.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.k</tt><br>
 * <font size=-1>int >=1</font></td>
 * <td valign=top>(number of actions)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.temperature</tt><br>
 * <font size=-1>double >=0</font></td>
 * <td valign=top>(the temperature controlling the probabilities actions are
 * chosen)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.mintemperature</tt><br>
 * <font size=-1>double >=0</font></td>
 * <td valign=top>(the minimum value for temperature)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.alpha</tt><br>
 * <font size=-1>double (0, 1]</font></td>
 * <td valign=top>(the cooling rate of temperature over time)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>softmax_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class SoftmaxLearner extends AbstractLearner implements
		ExposedStimuliResponseLearner, Serializable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(SoftmaxLearner.class);

	public static final String P_DEF_BASE = "softmax_learner";

	public static final String P_K = "k";

	public static final String P_TEMPERATURE = "temperature";

	public static final String P_MIN_TEMPERATURE = "mintemperature";

	public static final String P_ALPHA = "alpha";

	protected int numActions;

	/**
	 * initial value of {@link #temperature}
	 */
	protected double initialTemperature;

	/**
	 * controls the relative weights of actions
	 */
	protected double temperature;

	/**
	 * the minimal value to which {@link #temperature} may be decreased
	 */
	protected double minTemperature;

	/**
	 * decreasing rate of {@link #temperature}
	 */
	protected double alpha;

	/**
	 * the average rewards of actions
	 */
	protected double[] q;

	protected int maxQIndex;

	protected double[] normalized_q;

	protected double[] p;

	/**
	 * the action taken last time
	 */
	protected int lastAction;

	/**
	 * the numbers of actions selected.
	 */
	protected int[] times;

	protected Uniform distribution;

	public static final int DEFAULT_NUM_ACTIONS = 10;

	public static final double DEFAULT_TEMPERATURE = 0.2;

	public static final double DEFAULT_MIN_TEMPERATURE = 0.01;

	public static final double DEFAULT_ALPHA = 1;

	public SoftmaxLearner() {
		this(SoftmaxLearner.DEFAULT_NUM_ACTIONS, SoftmaxLearner.DEFAULT_TEMPERATURE);
	}

	public SoftmaxLearner(final int numActions, final double temperature) {
		this.numActions = numActions;
		this.temperature = temperature;

		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(SoftmaxLearner.P_DEF_BASE);

		numActions = parameters.getIntWithDefault(base.push(SoftmaxLearner.P_K),
				defBase.push(SoftmaxLearner.P_K), SoftmaxLearner.DEFAULT_NUM_ACTIONS);
		if (numActions < 1) {
			SoftmaxLearner.logger
					.error("The number of actions available must be a positive integer !");
			numActions = SoftmaxLearner.DEFAULT_NUM_ACTIONS;
		}

		initialTemperature = parameters
				.getDoubleWithDefault(base.push(SoftmaxLearner.P_TEMPERATURE), defBase
						.push(SoftmaxLearner.P_TEMPERATURE),
						SoftmaxLearner.DEFAULT_TEMPERATURE);
		if (initialTemperature == 0) {
			SoftmaxLearner.logger.error("temperature cannot be 0 ! Use "
					+ SoftmaxLearner.DEFAULT_TEMPERATURE + " instead.");
			initialTemperature = SoftmaxLearner.DEFAULT_TEMPERATURE;
		}

		minTemperature = parameters.getDoubleWithDefault(base
				.push(SoftmaxLearner.P_MIN_TEMPERATURE), defBase
				.push(SoftmaxLearner.P_MIN_TEMPERATURE),
				SoftmaxLearner.DEFAULT_MIN_TEMPERATURE);
		if (minTemperature < SoftmaxLearner.DEFAULT_MIN_TEMPERATURE) {
			SoftmaxLearner.logger
					.info("Invalid value for mintemperature. Use the default value instead. ");
			minTemperature = SoftmaxLearner.DEFAULT_MIN_TEMPERATURE;
		}

		alpha = parameters.getDoubleWithDefault(base.push(SoftmaxLearner.P_ALPHA),
				defBase.push(SoftmaxLearner.P_ALPHA), SoftmaxLearner.DEFAULT_ALPHA);
		if ((alpha <= 0) || (alpha > 1)) {
			SoftmaxLearner.logger
					.error("alpha must be a double between 0 (exclusive) and 1 !");
			alpha = SoftmaxLearner.DEFAULT_ALPHA;
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		init1();
	}

	private void init1() {
		times = new int[numActions];
		lastAction = -1;

		q = new double[numActions];
		normalized_q = new double[numActions];
		p = new double[numActions];
		updateProbabilities();

		temperature = initialTemperature;
	}

	public void reset() {
		init1();
	}

	public Object protoClone() {
		try {
			return clone();
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public int act() {

		lastAction = p.length - 1;
		double prob = distribution.nextDouble();

		// logger.info("prob: " + prob);

		for (int i = 0; i < p.length; i++) {
			prob -= p[i];
			if (prob <= 0) {
				lastAction = i;
				break;
			}
		}

		updateTemperature();

		return lastAction;
	}

	public int act(final Set<Integer> actions) {
		final Integer indices[] = actions.toArray(new Integer[0]);
		if (indices.length == 0) {
			return -1;
		}

		final double tempP[] = new double[indices.length];
		int index = tempP.length - 1;
		double sum = 0;
		for (int i = 0; i < tempP.length; i++) {
			tempP[i] = p[indices[i].intValue()];
			sum += tempP[i];
		}

		double prob = distribution.nextDoubleFromTo(0, sum);

		for (int i = 0; i < tempP.length; i++) {
			prob -= tempP[i];
			if (prob <= 0) {
				index = i;
				break;
			}
		}

		lastAction = indices[index].intValue();

		updateTemperature();

		return lastAction;
	}

	private void updateTemperature() {
		temperature *= alpha;
		if (temperature < minTemperature) {
			temperature = minTemperature;
		}
	}

	public void reward(final double reward) {
		// update q
		q[lastAction] = q[lastAction] + (reward - q[lastAction])
				/ (1 + times[lastAction]);
		times[lastAction]++;

		updateProbabilities();
	}

	protected void updateProbabilities() {

		// logger.info("q: " + CatpMessage.concatenate(q));

		// get max of q's
		double maxQ = Double.NEGATIVE_INFINITY;
		for (final double element : q) {
			if (Math.abs(element) > maxQ) {
				maxQ = Math.abs(element);
			}
		}

		if (maxQ == 0) {
			maxQ = 1;
		}

		// normalize q and calculate e to the power of q
		double sumOfEQ = 0;
		for (int i = 0; i < q.length; i++) {
			normalized_q[i] = q[i] / maxQ;
			p[i] = Math.exp(normalized_q[i] / temperature); // utilize p temporarily
			sumOfEQ += p[i];
		}

		// logger.info("temperature: " + temperature + " sumOfEQ: " + sumOfEQ);

		// logger.info("normalized_q: " + CatpMessage.concatenate(normalized_q));

		// calculate p
		for (int i = 0; i < p.length; i++) {
			p[i] /= sumOfEQ;
		}

		// logger.info("p: " + CatpMessage.concatenate(p));
		// logger.info("\n");

	}

	@Override
	public double getLearningDelta() {
		// TODO:
		return 0.0;
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}

	public int getNumberOfActions() {
		return numActions;
	}

	public void setNumberOfActions(final int numActions) {
		this.numActions = numActions;
	}

	public double getInitialTemperature() {
		return initialTemperature;
	}

	public void setInitialTemperature(double initialTemperature) {
		this.initialTemperature = initialTemperature;
	}

	public double getMinTemperature() {
		return minTemperature;
	}

	public void setMinTemperature(double minTemperature) {
		this.minTemperature = minTemperature;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * for debugging purpose only.
	 */
	public double[] getReturns() {
		return q;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent("temperature:" + initialTemperature + " minTemperature:"
						+ minTemperature + " alpha:" + alpha + " k:" + numActions);
		return s;
	}
}

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

import java.util.Set;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A policy used by {@link StimuliResponseLearner} to update the expected
 * returns of multiple discrete actions based on the softmax method in Section
 * 2.3, Sutton and Barto's RL book
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
 * <td valign=top><i>base</i><tt>.temperature</tt><br>
 * <font size=-1>double >=0 (<code>0.2</code> by default)</font></td>
 * <td valign=top>(the temperature controlling the probabilities actions are
 * chosen)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.mintemperature</tt><br>
 * <font size=-1>double >=0 (<code>0.01</code> by default)</font></td>
 * <td valign=top>(the minimum value for temperature)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.alpha</tt><br>
 * <font size=-1>double (0,1] (<code>1</code> by default)</font></td>
 * <td valign=top>(the cooling rate of temperature over time)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>softmax_action_choosing</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class SoftmaxActionChoosingPolicy extends ActionChoosingPolicy implements
		Parameterizable {

	static Logger logger = Logger.getLogger(SoftmaxActionChoosingPolicy.class);

	public static final String P_DEF_BASE = "softmax_action_choosing";

	public static final String P_TEMPERATURE = "temperature";

	public static final String P_MIN_TEMPERATURE = "mintemperature";

	public static final String P_ALPHA = "alpha";

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

	protected Uniform distribution;

	public static final double DEFAULT_TEMPERATURE = 0.2;

	public static final double DEFAULT_MIN_TEMPERATURE = 0.01;

	public static final double DEFAULT_ALPHA = 1;

	public SoftmaxActionChoosingPolicy() {
		this(SoftmaxActionChoosingPolicy.DEFAULT_TEMPERATURE);
	}

	public SoftmaxActionChoosingPolicy(final double initialTemperature) {
		this.initialTemperature = initialTemperature;

		distribution = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(
				SoftmaxActionChoosingPolicy.P_DEF_BASE);

		initialTemperature = parameters.getDoubleWithDefault(base
				.push(SoftmaxActionChoosingPolicy.P_TEMPERATURE), defBase
				.push(SoftmaxActionChoosingPolicy.P_TEMPERATURE),
				SoftmaxActionChoosingPolicy.DEFAULT_TEMPERATURE);
		if (initialTemperature == 0) {
			SoftmaxActionChoosingPolicy.logger.error("temperature cannot be 0 ! Use "
					+ SoftmaxActionChoosingPolicy.DEFAULT_TEMPERATURE + " instead.");
			initialTemperature = SoftmaxActionChoosingPolicy.DEFAULT_TEMPERATURE;
		}

		minTemperature = parameters.getDoubleWithDefault(base
				.push(SoftmaxActionChoosingPolicy.P_MIN_TEMPERATURE), defBase
				.push(SoftmaxActionChoosingPolicy.P_MIN_TEMPERATURE),
				SoftmaxActionChoosingPolicy.DEFAULT_MIN_TEMPERATURE);
		if (minTemperature < SoftmaxActionChoosingPolicy.DEFAULT_MIN_TEMPERATURE) {
			SoftmaxActionChoosingPolicy.logger
					.info("Invalid value for mintemperature. Use the default value instead. ");
			minTemperature = SoftmaxActionChoosingPolicy.DEFAULT_MIN_TEMPERATURE;
		}

		alpha = parameters.getDoubleWithDefault(base
				.push(SoftmaxActionChoosingPolicy.P_ALPHA), defBase
				.push(SoftmaxActionChoosingPolicy.P_ALPHA),
				SoftmaxActionChoosingPolicy.DEFAULT_ALPHA);
		if ((alpha <= 0) || (alpha > 1)) {
			SoftmaxActionChoosingPolicy.logger
					.error("alpha must be a double between 0 (exclusive) and 1 !");
			alpha = SoftmaxActionChoosingPolicy.DEFAULT_ALPHA;
		}
	}

	public void initialize() {
		init1();
	}

	private void init1() {
		temperature = initialTemperature;
	}

	public void reset() {
		init1();
	}

	public Object protoClone() {
		try {
			final SoftmaxActionChoosingPolicy copy = (SoftmaxActionChoosingPolicy) clone();
			copy.distribution = (Uniform) distribution.clone();
			return copy;
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public double getTemperature() {
		return temperature;
	}

	/**
	 * 
	 * @param returns
	 * 
	 * @return the probabilities of actions being chosen based on the given
	 *         returns of these actions.
	 */
	protected double[] calculateProbabilities(final double returns[]) {

		// logger.info("returns: " + CatpMessage.concatenate(returns));

		final double[] q = new double[returns.length];

		// get max of returns's
		double maxQ = Double.NEGATIVE_INFINITY;
		for (final double return1 : returns) {
			if (Math.abs(return1) > maxQ) {
				maxQ = Math.abs(return1);
			}
		}

		if (maxQ == 0) {
			maxQ = 1;
		}

		// normalize returns and calculate e to the power of returns
		double sumOfEQ = 0;
		for (int i = 0; i < returns.length; i++) {
			q[i] = returns[i] / maxQ;
			q[i] = Math.exp(q[i] / temperature);
			sumOfEQ += q[i];
		}

		// logger.info("temperature: " + temperature + " sumOfEQ: " + sumOfEQ);

		// calculate probabilities
		for (int i = 0; i < q.length; i++) {
			// use q temporarily
			q[i] /= sumOfEQ;
		}

		// logger.info("p: " + CatpMessage.concatenate(q));
		// logger.info("\n");

		return q;
	}

	@Override
	public int act(final double returns[]) {
		final int lastAction = internalAct(returns);

		updateTemperature();

		return lastAction;
	}

	/**
	 * acts without updating temperature. This is useful when multiple acts are
	 * required in a row at a single step.
	 * 
	 * @param returns
	 * @return the action chosen to take
	 */
	protected int internalAct(final double returns[]) {
		final double[] p = calculateProbabilities(returns);

		int lastAction = returns.length - 1;
		double prob = distribution.nextDouble();

		// logger.info("prob: " + prob);

		for (int i = 0; i < returns.length; i++) {
			prob -= p[i];
			if (prob <= 0) {
				lastAction = i;
				break;
			}
		}

		return lastAction;
	}

	@Override
	public int act(final double returns[], final Set<Integer> actions) {

		if ((actions == null) || (actions.size() == 0)) {
			return -1;
		}

		int lastAction = -1;

		Integer indices[] = new Integer[actions.size()];
		indices = actions.toArray(indices);

		final double q[] = new double[indices.length];
		for (int i = 0; i < q.length; i++) {
			q[i] = returns[indices[i].intValue()];
		}

		lastAction = indices[act(q)];

		return lastAction;
	}

	@Override
	public double[] getProbabilities(final double[] returns) {
		return calculateProbabilities(returns);
	}

	protected void updateTemperature() {
		temperature *= alpha;
		if (temperature < minTemperature) {
			temperature = minTemperature;
		}
	}

	public double getInitialTemperature() {
		return initialTemperature;
	}

	public void setInitialTemperature(final double initialTemperature) {
		this.initialTemperature = initialTemperature;
	}

	public double getMinTemperature() {
		return minTemperature;
	}

	public void setMinTemperature(final double minTemperature) {
		this.minTemperature = minTemperature;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(final double alpha) {
		this.alpha = alpha;
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += "\n"
				+ Utils.indent(SoftmaxActionChoosingPolicy.P_TEMPERATURE + ":"
						+ initialTemperature);
		s += "\n"
				+ Utils.indent(SoftmaxActionChoosingPolicy.P_MIN_TEMPERATURE + ":"
						+ minTemperature);
		s += "\n" + Utils.indent(SoftmaxActionChoosingPolicy.P_ALPHA + ":" + alpha);

		return s;
	}

}
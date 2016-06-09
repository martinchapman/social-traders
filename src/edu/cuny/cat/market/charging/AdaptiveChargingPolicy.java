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

package edu.cuny.cat.market.charging;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Uniform;
import cern.jet.random.engine.RandomEngine;
import edu.cuny.ai.learning.MimicryLearner;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * An adaptive charging policy that uses learners to determine charges..
 * </p>
 * 
 * <p>
 * If a learner for some type of charge is not provided, the charging becomes
 * fixed at the initial rate.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.registration</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(initial charge on registration)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.registration.learner</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(learner that adapts registration charge; can be null)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.registration.scale</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(level of perturbation on registration charge)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.information</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(initial charge on information)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.information.learner</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(learner that adapts information charge; can be null)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.information.scale</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(level of perturbation on information charge)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.shout</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(initial charge on shout)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.shout.learner</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(learner that adapts shout charge; can be null)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.shout.scale</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(level of perturbation on shout charge)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.transaction</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(initial charge on transaction)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.transaction.learner</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(learner that adapts transaction charge; can be null)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.transaction.scale</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(level of perturbation on transaction charge)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.profit</tt><br>
 * <font size=-1>double [0,1]</font></td>
 * <td valign=top>(initial charge on profit)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.profit.learner</tt><br>
 * <font size=-1>name of class, inheriting
 * {@link edu.cuny.ai.learning.MimicryLearner}</font></td>
 * <td valign=top>(learner that adapts profit charge; can be null)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.profit.scale</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(level of perturbation on profit charge)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>adaptive_charging</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 * 
 */
public abstract class AdaptiveChargingPolicy extends ChargingPolicy {

	static Logger logger = Logger.getLogger(AdaptiveChargingPolicy.class);

	public static final String P_DEF_BASE = "adaptive_charging";

	public static final String P_LEARNER = "learner";

	public static final String P_SCALE = "scale";

	/**
	 * learning algorithms to adapt different types of charges
	 */
	protected MimicryLearner learners[];

	/**
	 * small random amounts added to adapted charges
	 */
	protected AbstractContinousDistribution perturbations[];

	/**
	 * the speed to adapt charges
	 */
	protected double scales[];

	/**
	 * the initial charges to impose
	 */
	protected double initialFees[];

	public AdaptiveChargingPolicy() {
		learners = new MimicryLearner[fees.length];
		initialFees = new double[fees.length];
		scales = new double[fees.length];
		perturbations = new Uniform[fees.length];
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(AdaptiveChargingPolicy.P_DEF_BASE);

		final RandomEngine prng = Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine();

		Parameter feeParam = null;
		Parameter defFeeParam = null;
		for (int i = 0; i < fees.length; i++) {
			feeParam = base.push(ChargingPolicy.P_FEES[i]);
			defFeeParam = defBase.push(ChargingPolicy.P_FEES[i]);

			initialFees[i] = parameters.getDoubleWithDefault(feeParam, defFeeParam,
					initialFees[i]);

			try {
				learners[i] = parameters.getInstanceForParameter(feeParam
						.push(AdaptiveChargingPolicy.P_LEARNER), defFeeParam
						.push(AdaptiveChargingPolicy.P_LEARNER), MimicryLearner.class);
				if (learners[i] instanceof Parameterizable) {
					((Parameterizable) learners[i]).setup(parameters, feeParam
							.push(AdaptiveChargingPolicy.P_LEARNER));
				}
				learners[i].initialize();

				scales[i] = parameters.getDoubleWithDefault(feeParam
						.push(AdaptiveChargingPolicy.P_SCALE), defFeeParam
						.push(AdaptiveChargingPolicy.P_SCALE), 0.1);

				perturbations[i] = new Uniform(0, scales[i], prng);
			} catch (final ParamClassLoadException e) {
				learners[i] = null;
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		for (int i = 0; i < fees.length; i++) {
			fees[i] = initialFees[i];
			if (learners[i] != null) {
				learners[i].setOutputLevel(fees[i]);
			}
		}
	}

	@Override
	public void reset() {
		// reset fees
		super.reset();

		// reset learners
		for (int i = 0; i < learners.length; i++) {
			if (learners[i] != null) {
				learners[i].reset();
				learners[i].setOutputLevel(fees[i]);
			}
		}
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		for (int i = 0; i < ChargingPolicy.P_FEES.length; i++) {
			s += "\n" + Utils.indent(ChargingPolicy.P_FEES[i] + ":" + initialFees[i]);
			if (learners[i] != null) {
				s += "\n" + Utils.indent(Utils.indent(learners[i].toString()));
				s += "\n" + Utils.indent(Utils.indent("scale:" + scales[i]));
			}
		}

		return s;
	}
}
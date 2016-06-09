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

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.io.DataWriter;

/**
 * A meta learner combines multiple learners and based on their performance,
 * gives preference to better ones.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.master</tt><br>
 * <font size=-1>name of class, implementing {@link StimuliResponseLearner}
 * </font></td>
 * <td valign=top>(master learner)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.n</tt><br>
 * <font size=-1>int >=1</font></td>
 * <td valign=top>(number of sub-learners)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.</tt><i>n</i><br>
 * <font size=-1>name of class, implementing {@link StimuliResponseLearner}
 * </font></td>
 * <td valign=top>(the <i>n</i>th sub-learner)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>meta_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.11 $
 */

public class MetaLearner extends AbstractLearner implements
		StimuliResponseLearner, Parameterizable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int currentLearner;

	protected StimuliResponseLearner[] subLearners;

	protected StimuliResponseLearner masterLearner;

	static final String P_N = "n";

	static final String P_MASTER = "master";

	public static final String P_DEF_BASE = "meta_learner";

	public MetaLearner() {
	}

	public MetaLearner(final int numLearners) {
		subLearners = new StimuliResponseLearner[numLearners];
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(MetaLearner.P_DEF_BASE);

		masterLearner = parameters.getInstanceForParameter(base
				.push(MetaLearner.P_MASTER), defBase.push(MetaLearner.P_MASTER),
				StimuliResponseLearner.class);
		if (masterLearner instanceof Parameterizable) {
			((Parameterizable) masterLearner).setup(parameters, base
					.push(MetaLearner.P_MASTER));
		}

		final int numLearners = parameters.getInt(base.push(MetaLearner.P_N),
				defBase.push(MetaLearner.P_N), 1);

		subLearners = new StimuliResponseLearner[numLearners];

		for (int i = 0; i < numLearners; i++) {
			final StimuliResponseLearner sub = parameters
					.getInstanceForParameter(base.push(i + ""), defBase.push(i + ""),
							StimuliResponseLearner.class);
			if (sub instanceof Parameterizable) {
				((Parameterizable) sub).setup(parameters, base.push(i + ""));
			}
			sub.initialize();
			subLearners[i] = sub;
		}
	}

	public void reset() {
		masterLearner.reset();
		for (int i = 0; i < subLearners.length; i++) {
			subLearners[i].reset();
		}
	}

	public int act() {
		currentLearner = masterLearner.act();
		return subLearners[currentLearner].act();
	}

	/**
	 * note that <code>actions</code> used to set active choices at the dispose of
	 * sub-learners, instead of specifying which sublearners are available.
	 */
	public int act(final Set<Integer> actions) {
		currentLearner = masterLearner.act();
		return subLearners[currentLearner].act(actions);
	}

	public void reward(final double reward) {
		masterLearner.reward(reward);
		subLearners[currentLearner].reward(reward);
	}

	@Override
	public double getLearningDelta() {
		return masterLearner.getLearningDelta();
	}

	public int getNumberOfActions() {
		return subLearners.length;
	}

	public void setNumberOfActions(final int numActions) {
		// simply overlook since this learner requires more complex setup.
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}
}

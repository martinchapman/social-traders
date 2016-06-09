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

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A policy that chooses multiple actions from a set of actions based on their
 * expected returns, using a {@link EpsilonGreedyActionChoosingPolicy} to choose
 * these actions one by one.
 * </p>
 * 
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.1 $
 */

public class EpsilonGreedyMultiActionChoosingPolicy extends
		MultiActionChoosingPolicy implements Parameterizable {

	static Logger logger = Logger
			.getLogger(EpsilonGreedyMultiActionChoosingPolicy.class);

	protected EpsilonGreedyActionChoosingPolicy choosing;

	public EpsilonGreedyMultiActionChoosingPolicy() {
		choosing = new EpsilonGreedyActionChoosingPolicy();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		choosing.setup(parameters, base);
		choosing.initialize();
	}

	public void initialize() {
		// do nothing
	}

	public void reset() {
		choosing.reset();
	}

	public Object protoClone() {
		try {
			final EpsilonGreedyMultiActionChoosingPolicy copy = (EpsilonGreedyMultiActionChoosingPolicy) clone();
			copy.choosing = (EpsilonGreedyActionChoosingPolicy) choosing.protoClone();
			return copy;
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int[] act(final double returns[], final int num) {
		int[] chosen = null;
		if (num > returns.length) {
			chosen = new int[returns.length];
		} else {
			chosen = new int[num];
		}

		final ArrayList<Double> values = new ArrayList<Double>();
		final ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < returns.length; i++) {
			values.add(returns[i]);
			indices.add(i);
		}

		int index = -1;
		for (int i = 0; i < chosen.length; i++) {
			index = choosing.internalAct(Utils.toArray(values));
			chosen[i] = indices.get(index).intValue();

			// remove the chosen action and its return
			values.remove(index);
			indices.remove(index);
		}

		choosing.updateEpsilon();

		return chosen;
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += "\n" + Utils.indent(choosing.toString());

		return s;
	}
}
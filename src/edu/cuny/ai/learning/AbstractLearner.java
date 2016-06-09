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

import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.io.DataWriter;

/**
 * @author Steve Phelps
 * @version $Revision: 1.9 $
 */

public abstract class AbstractLearner implements Learner, Parameterizable {

	protected LearnerMonitor monitor = null;

	public static final String P_MONITOR = "monitor";

	public AbstractLearner() {
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		try {
			final Parameter monitorParameter = base.push(AbstractLearner.P_MONITOR);
			monitor = parameters.getInstanceForParameter(monitorParameter, null,
					LearnerMonitor.class);
			monitor.setup(parameters, monitorParameter);
		} catch (final ParamClassLoadException e) {
			monitor = null;
		}
	}

	public void initialize() {
		// do nothing
	}

	public void monitor() {
		if (monitor != null) {
			monitor.startRecording();
			dumpState(monitor);
			monitor.finishRecording();
		}
	}

	public abstract double getLearningDelta();

	public abstract void dumpState(DataWriter out);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
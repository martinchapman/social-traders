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

import edu.cuny.util.Resetable;
import edu.cuny.util.io.DataWriter;

/**
 * Classes implementing this interface indicate that they implement a learning
 * algorithm.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.8 $
 */

public interface Learner extends Resetable {

	/**
	 * initializes after parameters are set up.
	 */
	public void initialize();

	/**
	 * Return a value indicative of the amount of learning that occured during the
	 * last iteration. Values close to 0.0 indicate that the learner has converged
	 * to an equilibrium status.
	 * 
	 * @return A double representing the amount of learning that occured.
	 */
	public double getLearningDelta();

	/**
	 * Write out our status data to the specified data writer.
	 */
	public void dumpState(DataWriter out);

	/**
	 * A hook to provide monitoring functionality. Implementations of learning
	 * algorithms should either log, or provide a visualisation of, their status
	 * in response to this method.
	 */
	public void monitor();

}
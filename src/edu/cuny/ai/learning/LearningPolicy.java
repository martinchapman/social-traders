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

import edu.cuny.util.Prototypeable;
import edu.cuny.util.Resetable;

/**
 * An interface that should be implemented by all policies for learners.
 * Policies are used by composite learners to regulate some aspect in learning,
 * e.g., action choosing and return updating in q-learning.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public interface LearningPolicy extends Prototypeable, Resetable {

	/**
	 * initializes after parameters are set up.
	 */
	public void initialize();
}
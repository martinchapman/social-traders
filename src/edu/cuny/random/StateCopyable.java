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
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.random;

/**
 * <p>
 * An interface defining an {@link cern.jet.random.AbstractDistribution} whose
 * distribution parameters can be setup after an example object of the same
 * type.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public interface StateCopyable {

	/**
	 * setups parameters based on what is used in the example object.
	 * 
	 * @param example
	 *          the example distribution.
	 */
	public void copyStateFrom(Object example);
}
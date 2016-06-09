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

package edu.cuny.util;

/**
 * <p>
 * Classes implementing this interface indicate that they can be initialized
 * from a parameter database using the "Clique" pattern of ECJ. It is planned to
 * move over to a bean-based XML approach at some time in the future.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.9 $
 */

public interface Parameterizable {

	/**
	 * Initialise this object from a parameter database.
	 */
	public abstract void setup(ParameterDatabase parameters, Parameter base);

}
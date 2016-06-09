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
 Copyright 2006 by Sean Luke
 Licensed under the Academic Free License version 3.0
 See the file "LICENSE" for more information
 */

package edu.cuny.util;

/**
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author spaus
 * @version $Revision: 1.7 $
 */
public class ParameterDatabaseAdapter implements ParameterDatabaseListener {

	/**
	 * 
	 */
	public ParameterDatabaseAdapter() {
		super();
	}

	/**
	 * @see edu.cuny.util.ParameterDatabaseListener#parameterSet(ParameterDatabaseEvent)
	 */
	public void parameterSet(final ParameterDatabaseEvent evt) {
	}

	/**
	 * @see edu.cuny.util.ParameterDatabaseListener#parameterAccessed(ParameterDatabaseEvent)
	 */
	public void parameterAccessed(final ParameterDatabaseEvent evt) {
	}

}

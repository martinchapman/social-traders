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

/*
 * Created on Apr 9, 2005 8:28:11 AM
 * 
 * By: spaus
 */
package edu.cuny.util;

import java.util.EventListener;

/**
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author spaus
 * @version $Revision: 1.7 $
 */
public interface ParameterDatabaseListener extends EventListener {
	/**
	 * @param evt
	 */
	public void parameterSet(ParameterDatabaseEvent evt);

	/**
	 * @param evt
	 */
	public void parameterAccessed(ParameterDatabaseEvent evt);
}

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
 * This exception is thrown by the {@link Parameter} Database when it fails to
 * locate and load a class specified by a given parameter as requested. Most
 * commonly this results in the program exiting with an error, so it is defined
 * as a RuntimeException so you don't have to catch it or declare that you throw
 * it.
 * 
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author Sean Luke
 * @version $Revision: 1.7 $
 */

public class ParamClassLoadException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParamClassLoadException(final String s) {
		super("\n" + s);
	}
}

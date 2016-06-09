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
 * Created on Apr 9, 2005 8:36:42 AM
 * 
 * By: spaus
 */
package edu.cuny.util;

import java.util.EventObject;

/**
 * <p>
 * This is a modified version of the class from the original ECJ package by Sean
 * Luke, et al..
 * </p>
 * 
 * @author spaus
 * @version $Revision: 1.7 $
 */
public class ParameterDatabaseEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int SET = 0;

	public static final int ACCESSED = 1;

	private final Parameter parameter;

	private final String value;

	private final int type;

	/**
	 * For ParameterDatabase events.
	 * 
	 * @param source
	 *          the ParameterDatabase
	 * @param parameter
	 *          the Parameter associated with the event
	 * @param value
	 *          the value of the Parameter associated with the event
	 * @param type
	 *          the type of the event
	 */
	public ParameterDatabaseEvent(final Object source, final Parameter parameter,
			final String value, final int type) {
		super(source);
		this.parameter = parameter;
		this.value = value;
		this.type = type;
	}

	/**
	 * @return the Parameter associated with the event
	 */
	public Parameter getParameter() {
		return parameter;
	}

	/**
	 * @return the value of the Parameter associated with the event.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the type of the event.
	 */
	public int getType() {
		return type;
	}
}

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

package edu.cuny.cat.stat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.event.EventListener;
import edu.cuny.util.Galaxy;

/**
 * <p>
 * A class recording updates of various {@link ReportVariable}s and accepting
 * queries. It may also be notified through {@link edu.cuny.event.Event}s on the
 * update of report variable values.
 * </p>
 * 
 * <p>
 * TODO: to refactor later to register this in object registry.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.18 $
 */

public class ReportVariableBoard implements EventListener {

	static Logger logger = Logger.getLogger(ReportVariableBoard.class);

	private static ReportVariableBoard instance;

	protected final Map<String, Object> board;

	private ReportVariableBoard() {
		board = Collections.synchronizedMap(new HashMap<String, Object>());

		/* TODO: need to check out sometime later ? */
		Galaxy.getInstance().getDefaultTyped(EventEngine.class).checkIn(
				ReportVariable.class, this);
	}

	public synchronized static ReportVariableBoard getInstance() {
		if (ReportVariableBoard.instance == null) {
			ReportVariableBoard.instance = new ReportVariableBoard();
		}
		return ReportVariableBoard.instance;
	}

	public void reset() {
		if (board != null) {
			board.clear();
		}
	}

	public Collection<String> getVarNames() {
		return board.keySet();
	}

	public Double getValueAsDouble(final String varName) {
		return (Double) board.get(varName);
	}

	public Double getValueAsDouble(final ReportVariable var) {
		return getValueAsDouble(var.getName());
	}

	public Object getValue(final String varName) {
		return board.get(varName);
	}

	public Object getValue(final ReportVariable var) {
		return getValue(var.getName());
	}

	public void reportValue(final String varName, final Object value) {
		board.put(varName, value);
	}

	public void reportValue(final ReportVariable var, final Object value) {
		reportValue(var.getName(), value);
	}

	public void reportValue(final String varName, final double value) {
		reportValue(varName, new Double(value));
	}

	public void reportValues(final Map<ReportVariable, Object> vars) {

		for (final ReportVariable var : vars.keySet()) {
			final Object value = vars.get(var);
			if (value instanceof Number) {
				// report it no matter what it is.
				// if (!Double.isNaN(v)) {
				// reportValue(var.getName(), v);
				// }
				reportValue(var.getName(), value);
			} else if ((value instanceof Boolean) || (value instanceof String)) {
				reportValue(var.getName(), value);
			} else {
				ReportVariableBoard.logger.info("Value of report variable "
						+ var.getName() + " is not supported yet !");
			}
		}
	}

	/**
	 * allows posting report variables without directly refering to this board
	 */
	public void eventOccurred(final Event te) {
		for (final String key : te.getKeys()) {
			final Number value = (Number) te.getValue(key);
			reportValue(key, value.doubleValue());
		}
	}
}

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

package edu.cuny.cat.stat;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * A {@link ScoreDaysCondition} that is based on the rules in a set of sub-
 * {@link ScoreDaysCondition}s.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.isor</tt><br>
 * <font size=-1>boolean (false by default)</font></td>
 * <td valign=top>(controls how to logically combine the results of sub-
 * {@link ScoreDaysCondition}s)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.n</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(the number of different {@link ScoreDaysCondition}s)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.</tt><i>n</i><br>
 * <font size=-1>name of class, implementing {@link ScoreDaysCondition}</font></td>
 * <td valign=top>(the <i>n</i>th {@link ScoreDaysCondition})</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class CombiScoreDaysCondition extends AbstractScoreDaysCondition {

	public static final String P_NUM = "n";

	public static final String P_ISOR = "isor";

	protected List<ScoreDaysCondition> conditions = null;

	boolean isOR = false;

	public CombiScoreDaysCondition() {
		conditions = new LinkedList<ScoreDaysCondition>();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		isOR = parameters.getBoolean(base.push(CombiScoreDaysCondition.P_ISOR),
				null, isOR);

		final int num = parameters.getInt(base.push(CombiScoreDaysCondition.P_NUM),
				null, 0);

		for (int i = 0; i < num; i++) {
			final ScoreDaysCondition condition = parameters.getInstanceForParameter(
					base.push(i + ""), null, ScoreDaysCondition.class);
			if (condition instanceof Parameterizable) {
				((Parameterizable) condition).setup(parameters, base.push(i + ""));
			}
			addCondition(condition);
		}
	}

	/**
	 * Add a new ScoreDaysCondition
	 */
	public void addCondition(final ScoreDaysCondition condition) {
		conditions.add(condition);
	}

	@Override
	public void reset() {
		super.reset();

		for (final ScoreDaysCondition condition : conditions) {
			if (condition instanceof Resetable) {
				((Resetable) condition).reset();
			}
		}
	}

	public Iterator<ScoreDaysCondition> conditionIterator() {
		return conditions.iterator();
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		for (final ScoreDaysCondition condition : conditions) {
			if (condition instanceof AuctionEventListener) {
				try {
					((AuctionEventListener) condition).eventOccurred(event);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String toString() {
		String s = super.toString();
		int n = 0;

		for (final ScoreDaysCondition condition : conditions) {
			s += Utils.indent("\n" + n + ":" + Utils.indent(condition.toString()));
			n++;
		}

		return s;
	}

	@Override
	protected boolean updateTaken(final int day) {
		boolean taken;

		for (final ScoreDaysCondition condition : conditions) {
			taken = condition.count(day);
			if (taken && isOR) {
				return true;
			} else if (!taken && !isOR) {
				return false;
			}
		}

		// if OR, all returned false; if AND, all returned true
		return !isOR;
	}
}

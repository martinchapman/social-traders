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

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.AuctionEventListener;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * <p>
 * An abstract {@link ScoreDaysCondition} which makes decision once for each
 * queries day.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.debug</tt><br>
 * <font size=-1>boolean (false by default)</font></td>
 * <td valign=top>(whether to output info on if a day is a scoring day or not)</td>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public abstract class AbstractScoreDaysCondition implements ScoreDaysCondition,
		Parameterizable, AuctionEventListener, Resetable {

	static final Logger logger = Logger
			.getLogger(AbstractScoreDaysCondition.class);

	public static final String P_DEBUG = "debug";

	protected boolean debug = false;

	protected boolean taken;

	protected int day;

	public AbstractScoreDaysCondition() {
		init0();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		debug = parameters.getBoolean(
				base.push(AbstractScoreDaysCondition.P_DEBUG), null, debug);
	}

	private void init0() {
		day = Integer.MIN_VALUE;
		taken = false;
	}

	public void reset() {
		init0();
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof GameStartingEvent) {
			reset();
		} else if (event instanceof DayOpeningEvent) {
			if (count(event.getDay()) && debug) {
				AbstractScoreDaysCondition.logger.info(Utils.indent("score day - "
						+ event.getDay())
						+ "\n");
			}
		}
	}

	/**
	 * determines whether the day is a scoring day or not.
	 * 
	 * @param day
	 */
	protected abstract boolean updateTaken(int day);

	public boolean count(final int day) {
		if (this.day != day) {
			taken = updateTaken(day);
		}

		return taken;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

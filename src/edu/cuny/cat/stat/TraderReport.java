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

import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Trader;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.server.GameController;

/**
 * <p>
 * A report recording information about traders.
 * </p>
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>&lt;trader&gt;.specialist</code></td>
 * <td>the id of the specialist a trader registers with on the current day.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;trader&gt;.value</code></td>
 * <td>the private value of a trader.</td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class TraderReport implements GameReport {

	static Logger logger = Logger.getLogger(TraderReport.class);

	public TraderReport() {
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	public void produceUserOutput() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof DayClosedEvent) {
			final ReportVariableBoard board = ReportVariableBoard.getInstance();
			final Trader traders[] = GameController.getInstance().getRegistry()
					.getTraders();
			for (final Trader trader2 : traders) {
				board.reportValue(trader2.getId() + ReportVariable.SEPARATOR
						+ GameReport.VALUE, trader2.getPrivateValue());
				board.reportValue(trader2.getId() + ReportVariable.SEPARATOR
						+ GameReport.SPECIALIST, trader2.getSpecialistId());
			}
		}
	}
}

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

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.event.FeesAnnouncedEvent;
import edu.cuny.cat.market.charging.ChargingPolicy;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Utils;

/**
 * A report tracking and recording charges announced by specialists.
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>&lt;specialist&gt;.fee.registration</code></td>
 * <td>the registration fee of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.fee.information</code></td>
 * <td>the information fee of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.fee.shout</code></td>
 * <td>the shout fee of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.fee.transaction</code></td>
 * <td>the transaction fee of a specialist.</td>
 * </tr>
 * <tr>
 * <td> <code>&lt;specialist&gt;.fee.profit</code></td>
 * <td>the profit fee of a specialist.</td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.15 $
 */

public class FeeReport implements GameReport {

	static Logger logger = Logger.getLogger(FeeReport.class);

	public FeeReport() {
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	public void produceUserOutput() {
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof FeesAnnouncedEvent) {
			final Specialist specialist = ((FeesAnnouncedEvent) event)
					.getSpecialist();
			final ReportVariableBoard board = ReportVariableBoard.getInstance();
			board.reportValue(specialist.getId() + ReportVariable.SEPARATOR
					+ GameReport.FEE + ReportVariable.SEPARATOR
					+ ChargingPolicy.P_FEES[ChargingPolicy.REGISTRATION_INDEX],
					specialist.getRegistrationFee());
			board.reportValue(specialist.getId() + ReportVariable.SEPARATOR
					+ GameReport.FEE + ReportVariable.SEPARATOR
					+ ChargingPolicy.P_FEES[ChargingPolicy.INFORMATION_INDEX], specialist
					.getInformationFee());
			board.reportValue(specialist.getId() + ReportVariable.SEPARATOR
					+ GameReport.FEE + ReportVariable.SEPARATOR
					+ ChargingPolicy.P_FEES[ChargingPolicy.SHOUT_INDEX], specialist
					.getShoutFee());
			board.reportValue(specialist.getId() + ReportVariable.SEPARATOR
					+ GameReport.FEE + ReportVariable.SEPARATOR
					+ ChargingPolicy.P_FEES[ChargingPolicy.TRANSACTION_INDEX], specialist
					.getTransactionFee());
			board.reportValue(specialist.getId() + ReportVariable.SEPARATOR
					+ GameReport.FEE + ReportVariable.SEPARATOR
					+ ChargingPolicy.P_FEES[ChargingPolicy.PROFIT_INDEX], specialist
					.getProfitFee());
		} else if (event instanceof DayStatPassEvent) {
			switch (((DayStatPassEvent) event).getPass()) {
			case DayStatPassEvent.SECOND_PASS:
				printFees();
			}
		}
	}

	protected void printFees() {

		FeeReport.logger.info(getClass().getSimpleName() + "\n");

		final Specialist specialists[] = GameController.getInstance().getRegistry()
				.getSpecialists();

		for (final Specialist specialist2 : specialists) {
			FeeReport.logger.info(Utils.indent(Utils.formatter.format(specialist2
					.getRegistrationFee())
					+ " "
					+ Utils.formatter.format(specialist2.getInformationFee())
					+ " "
					+ Utils.formatter.format(specialist2.getShoutFee())
					+ " "
					+ Utils.formatter.format(specialist2.getTransactionFee())
					+ " "
					+ Utils.formatter.format(specialist2.getProfitFee())
					+ "  \t"
					+ specialist2.getId()));
		}

		FeeReport.logger.info("");
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

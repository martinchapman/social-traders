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

import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.io.CSVWriter;

/**
 * <p>
 * This class writes values of specified report variables to the specified
 * <code>DataWriter</code> objects, and thus can be used to log data to eg, CSV
 * files, a database backend, etc.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.26 $
 */

public class ReportVariableWriterReport extends AbstractReportVariableReport {

	static Logger logger = Logger.getLogger(ReportVariableWriterReport.class);

	protected CSVWriter log = null;

	protected int gameNum;

	public ReportVariableWriterReport() {
	}

	public ReportVariableWriterReport(final CSVWriter log) {
		this.log = log;
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		log = new CSVWriter();
		log.setAutowrap(false);
		log.setAppend(false);
		log.setup(parameters, base);
		log.open();
	}

	protected void generateHeader() {

		final String headers[] = { "game", "day" };

		for (final String header : headers) {
			log.newData(header);
		}

		for (final String varName : varNames) {
			log.newData(varName);
		}

		log.endRecord();
		log.flush();
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		if (event instanceof SimulationStartedEvent) {
			gameNum = -1;
			generateHeader();
		} else if (event instanceof GameStartingEvent) {
			gameNum++;
		} else if (event instanceof DayStatPassEvent) {
			if (((DayStatPassEvent) event).getPass() == DayStatPassEvent.THIRD_PASS) {
				updateLog(event);
			}
		} else if (event instanceof SimulationOverEvent) {
			log.close();
		}
	}

	protected void updateLog(final AuctionEvent event) {

		log.newData(gameNum);
		log.newData(event.getDay());

		final ReportVariableBoard board = ReportVariableBoard.getInstance();
		Object value;
		for (final String varName : varNames) {
			value = board.getValue(varName);
			if (value instanceof Double) {
				final Double doubleValue = (Double) value;
				if (doubleValue.isNaN() || doubleValue.isInfinite()) {
					// use NaN for all these cases
					log.newData(Double.NaN);
				} else {
					log.newData(GameReport.Formatter.format(doubleValue.doubleValue()));
				}
			} else if (value != null) {
				log.newData(value);
			} else {
				log.newData(Double.NaN);
			}
		}

		log.endRecord();
		log.flush();
	}

	@Override
	public void produceUserOutput() {
	}

	@Override
	public Map<ReportVariable, ?> getVariables() {
		return null;
	}
}
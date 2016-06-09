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

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayStatPassEvent;
import edu.cuny.util.Utils;

/**
 * <p>
 * A report collecting resource consumption information, including memory use,
 * etc.
 * </p>
 * 
 * <p>
 * <b>Report variables</b>
 * </p>
 * <table cellpadding="5">
 * <tr>
 * <td> <code>memory.max</code></td>
 * <td>the max amount of memory that can be allocated to the VM.</td>
 * </tr>
 * <tr>
 * <td> <code>memory.total</code></td>
 * <td>the total amount of memory allocated to the VM.</td>
 * </tr>
 * <tr>
 * <td> <code>memory.free</code></td>
 * <td>the amount of free memory available to the VM.</td>
 * </tr>
 * <tr>
 * <td> <code>memory.used</code></td>
 * <td>the amount of memory currently used by the VM.</td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class ResourceReport implements GameReport {

	static Logger logger = Logger.getLogger(ResourceReport.class);

	protected static final int KILO = 1024;

	public static final String MEMORY = "memory";

	public static final String MAX = "max";

	public static final String TOTAL = "total";

	public static final String USED = "used";

	public static final String FREE = "free";

	protected int maxMemory;

	protected int totalMemory;

	protected int freeMemory;

	protected int usedMemory;

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof DayStatPassEvent) {
			switch (((DayStatPassEvent) event).getPass()) {
			case DayStatPassEvent.FIRST_PASS:
				recordMemoryUse();
				break;

			case DayStatPassEvent.SECOND_PASS:
				reportMemoryUse();
				break;
			}
		}
	}

	public void recordMemoryUse() {
		final ReportVariableBoard board = ReportVariableBoard.getInstance();

		final Runtime runtime = Runtime.getRuntime();
		freeMemory = (int) (runtime.freeMemory() / ResourceReport.KILO);
		totalMemory = (int) (runtime.totalMemory() / ResourceReport.KILO);
		usedMemory = totalMemory - freeMemory;
		maxMemory = (int) (runtime.maxMemory() / ResourceReport.KILO);

		board.reportValue(ResourceReport.MEMORY + ReportVariable.SEPARATOR
				+ ResourceReport.FREE, freeMemory);
		board.reportValue(ResourceReport.MEMORY + ReportVariable.SEPARATOR
				+ ResourceReport.TOTAL, totalMemory);
		board.reportValue(ResourceReport.MEMORY + ReportVariable.SEPARATOR
				+ ResourceReport.USED, usedMemory);
		board.reportValue(ResourceReport.MEMORY + ReportVariable.SEPARATOR
				+ ResourceReport.MAX, maxMemory);
	}

	public void reportMemoryUse() {

		ResourceReport.logger.info(getClass().getSimpleName() + "\n");

		ResourceReport.logger.info(Utils.indent(" Used mem: " + usedMemory + "K"));
		ResourceReport.logger.info(Utils.indent(" Free mem: " + freeMemory + "K"));
		ResourceReport.logger.info(Utils.indent("Total mem: " + totalMemory + "K"));
		ResourceReport.logger.info(Utils.indent("  Max mem: " + maxMemory + "K"));

		ResourceReport.logger.info("\n");
	}

	public void produceUserOutput() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

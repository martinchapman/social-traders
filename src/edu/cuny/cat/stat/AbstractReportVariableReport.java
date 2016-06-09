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
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.SimulationStartedEvent;
import edu.cuny.cat.market.charging.ChargingPolicy;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * The super class of any report class that relies upon the declaration of a
 * list of report variables.
 * </p>
 * 
 * <p>
 * Name templates can be used when a variable name is needed, e.g.
 * <code>&lt;specialist&gt;.profit,
 * or <code>&lt;specialist&gt;.&lt;fee&gt;</code>, which will be expanded
 * accordingly.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public abstract class AbstractReportVariableReport implements GameReport,
		Parameterizable {

	static Logger logger = Logger.getLogger(AbstractReportVariableReport.class);

	public static String[] TEMPLATES = { GameReport.TRADER,
			GameReport.SPECIALIST, GameReport.FEE, GameReport.SHOUT };

	static String[] SHOUTS = { "ask", "bid" };

	static String[] FEES = null;

	static {
		AbstractReportVariableReport.FEES = new String[ChargingPolicy.P_FEES.length];
		for (int i = 0; i < AbstractReportVariableReport.FEES.length; i++) {
			AbstractReportVariableReport.FEES[i] = GameReport.FEE
					+ ReportVariable.SEPARATOR + ChargingPolicy.P_FEES[i];
		}
	}

	protected static String P_VAR = "var";

	protected static String P_NUM = "n";

	protected String varNames[];

	public AbstractReportVariableReport() {
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		final int n = parameters.getIntWithDefault(base.push(
				AbstractReportVariableReport.P_VAR).push(
				AbstractReportVariableReport.P_NUM), null, 0);

		varNames = new String[n];
		for (int i = 0; i < n; i++) {
			varNames[i] = parameters.getString(base.push(
					AbstractReportVariableReport.P_VAR).push(String.valueOf(i)), null);
		}
	}

	/**
	 * A variable name may be in the format of, for example,
	 * "<specialist>.profit", which represents several report variables actually,
	 * each for a specialist. This method recognizes the wildcard formats in
	 * variable names and replace with the atomic names.
	 * 
	 */
	protected void decodeVarNames() {

		final Pattern patterns[] = new Pattern[AbstractReportVariableReport.TEMPLATES.length];
		final String replacements[][] = new String[AbstractReportVariableReport.TEMPLATES.length][];
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = getPattern(AbstractReportVariableReport.TEMPLATES[i]);
			replacements[i] = getReplacements(AbstractReportVariableReport.TEMPLATES[i]);
		}

		final Stack<String> oldNames = new Stack<String>();
		for (final String varName : varNames) {
			oldNames.push(varName);
		}

		final Stack<String> newNames = new Stack<String>();
		String name = null;
		Matcher m = null;
		while (!oldNames.isEmpty()) {
			name = oldNames.pop();
			for (int i = 0; i < patterns.length; i++) {
				m = patterns[i].matcher(name);
				if (m.find()) {
					for (int j = 0; j < replacements[i].length; j++) {
						oldNames.push(m.replaceAll(replacements[i][j]));
					}
					name = null;
					break;
				}
			}

			if (name != null) {
				newNames.push(name);
			}
		}

		varNames = new String[newNames.size()];
		for (int i = 0; i < varNames.length; i++) {
			varNames[i] = newNames.pop();
		}

		if (!oldNames.isEmpty() || !newNames.isEmpty()) {
			/* should always be empty now */

			oldNames.clear();
			newNames.clear();

			AbstractReportVariableReport.logger.error("Empty stack expected !");
		}
	}

	protected Pattern getPattern(final String template) {
		final String prefix = "<";
		final String postfix = ">";

		return Pattern.compile(prefix + template + postfix,
				Pattern.CASE_INSENSITIVE);
	}

	protected String[] getReplacements(final String template) {
		final Registry registry = GameController.getInstance().getRegistry();

		if (template.equalsIgnoreCase(GameReport.TRADER)) {
			return registry.getTraderIds();
		} else if (template.equalsIgnoreCase(GameReport.SPECIALIST)) {
			return registry.getSpecialistIds();
		} else if (template.equalsIgnoreCase(GameReport.SHOUT)) {
			return AbstractReportVariableReport.SHOUTS;
		} else if (template.equalsIgnoreCase(GameReport.FEE)) {
			return AbstractReportVariableReport.FEES;
		} else {
			AbstractReportVariableReport.logger
					.fatal("Unsupported template for report variable: " + template);
			return null;
		}
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof SimulationStartedEvent) {
			decodeVarNames();
		}
	}

	public abstract void produceUserOutput();

	public abstract Map<ReportVariable, ?> getVariables();

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
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

package edu.cuny.cat.trader.marketselection;

import java.util.Collection;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * A market selection strategy that always chooses a particular specialist to
 * register with.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.specialist</tt><br>
 * <font size=-1>string</font></td>
 * <td valign=top>(the name of the specialist to choose always)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 * 
 */

public class StaticMarketSelectionStrategy extends
		AbstractMarketSelectionStrategy {

	static Logger logger = Logger.getLogger(StaticMarketSelectionStrategy.class);

	public static final String P_SPECIALIST = "specialist";

	protected String specialistId;

	protected Integer specialistIndex;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		specialistId = parameters.getString(base
				.push(StaticMarketSelectionStrategy.P_SPECIALIST), null);

		if ((specialistId == null) || (specialistId.length() == 0)) {
			StaticMarketSelectionStrategy.logger
					.error("Invalid specialist id in the configuration for " + this
							+ " !");
		}
	}

	@Override
	protected void setupMarkets(final Collection<Specialist> marketColl) {
		markets = marketColl.toArray(new Specialist[0]);

		for (int i = 0; i < markets.length; i++) {
			// will constantly select this specialist
			if (markets[i].getId().equals(specialistId)) {
				specialistIndex = new Integer(i);
			}

			marketIndices.put(markets[i], new Integer(i));
		}

		if (specialistIndex == null) {
			StaticMarketSelectionStrategy.logger.error("Specialist specified in "
					+ this + " does not exist !");
		}
	}

	public void setSpecialistId(final String specialistId) {
		this.specialistId = specialistId;
	}

	public String getSpecialistId() {
		return specialistId;
	}

	/**
	 * always select the pre-specified specialist if it is active or none
	 * otherwise.
	 */
	@Override
	public void selectMarket() {
		if (activeMarkets.contains(specialistIndex)) {
			currentMarketIndex = specialistIndex.intValue();
		} else {
			currentMarketIndex = -1;
		}
	}
}
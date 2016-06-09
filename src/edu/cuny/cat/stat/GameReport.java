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

import java.text.DecimalFormat;
import java.util.Map;

import edu.cuny.cat.event.AuctionEventListener;

/**
 * An interface defined for game logging.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.16 $
 */

public interface GameReport extends AuctionEventListener {

	public static String GLOBAL = "global";

	public final static String EFFICIENCY = "efficiency";

	public final static String CONVERGENCE_COEFFICIENT = "convergence_coefficient";

	public final static String PROFIT_DISPERSION = "profit_dispersion";

	public final static String EQUILIBRIUM = "equilibrium";

	public final static String QUANTITY = "quantity";

	public final static String VALUE = "value";

	public final static String PRICE = "price";

	public final static String FEE = "fee";

	public final static String PLACED = "placed";

	public final static String ACCEPTED = "accepted";

	public final static String SHOUT = "shout";

	public final static String ASK = "ask";

	public final static String BID = "bid";

	public final static String TRANSACTION = "transaction";

	public final static String TRADER = "trader";

	public final static String SPECIALIST = "specialist";

	/**
	 * values from a distribution
	 */
	public final static String MEAN = "mean";

	public final static String MIN = "min";

	public final static String MAX = "max";

	public final static String STDEV = "stdev";

	/**
	 * values for assessment
	 */

	public static String SCORE = "score";

	public static String TOTAL = "total";

	public final static String PROFIT = "profit";

	public static String MARKETSHARE = "marketshare";

	public static String TRANSACTIONRATE = "transactionrate";

	public static DecimalFormat Formatter = new DecimalFormat(
			"+#########0.000;-#########.000");

	/**
	 * Produce the final report for the user. Implementors can do whatever they
	 * see fit, for example by writing a report on stdout, or they may choose to
	 * do nothing.
	 */
	public void produceUserOutput();

	/**
	 * Returns a Map of all of the variables that are produced in the report. The
	 * Map maps variables, represented by objects of type ReportVariable, onto
	 * values, which may be of any class. If no variables are produced by this
	 * report then an empty Map is returned.
	 */
	public Map<ReportVariable, ?> getVariables();

}

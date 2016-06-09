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

package edu.cuny.cat.market.core;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Trader;

/**
 * A class that extends {@link edu.cuny.cat.core.Trader} and includes additional
 * information for a specialist to record detailed information about a trader.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.88 $
 */

public class TraderInfo extends Trader {

	static Logger logger = Logger.getLogger(TraderInfo.class);

	public static final int INTRA_MARGINAL = 0;

	public static final int EXTRA_MARGINAL = 1;

	public static final int UNKNOWN_STATUS = 255;

	/**
	 * tells whether this trader is intra-marginal or extra-marginal in the global
	 * market
	 */
	protected int marginalStatus;

	/**
	 * goods that are traded today
	 */
	protected int goodsTraded;

	protected double lastShoutPrice;

	/**
	 * true if the trader placed a posted shout
	 */
	protected boolean isTraced;

	protected int lastDayTraced;

	protected int numOfDaysTraced;

	protected String prevSpecialistId;

	public TraderInfo(final String id, final String desc, boolean isSeller) {
		super(id, desc, isSeller ? Double.MAX_VALUE : 0.0, isSeller);

		goodsTraded = 0;
		entitlement = 1;

		lastShoutPrice = Double.NaN;

		marginalStatus = TraderInfo.UNKNOWN_STATUS;

		isTraced = false;
		lastDayTraced = -1;
		numOfDaysTraced = 0;

		prevSpecialistId = "";
		specialistId = "";
	}

	public void dayOpening() {
		prevSpecialistId = specialistId;
		specialistId = "";
		goodsTraded = 0;
	}

	public void dayClosed() {
		updateEntitlement();
	}

	public void shoutPlaced(double price) {
		if (isSeller) {
			if (price < privateValue) {
				privateValue = price;
			}
		} else if (price > privateValue) {
			privateValue = price;
		}

		lastShoutPrice = price;
	}

	public double getLastShoutPrice() {
		return lastShoutPrice;
	}

	public void updateTrace(int day) {
		lastDayTraced = day;
		numOfDaysTraced++;
		isTraced = true;
	}

	public boolean isTraced() {
		return isTraced;
	}

	public int getNumOfDaysTraced() {
		return numOfDaysTraced;
	}

	public int getLastDayTraced() {
		return lastDayTraced;
	}

	public void increaseGoodsTraded(int goodsTraded) {
		this.goodsTraded += goodsTraded;
		updateEntitlement();
	}

	public void updateEntitlement() {
		if (goodsTraded > entitlement) {
			entitlement = goodsTraded;
		}
	}

	public int getGoodsTraded() {
		return goodsTraded;
	}

	/**
	 * @return true if the trader registers with the same specialist consecutively
	 *         for two days, and false otherwise.
	 */
	public boolean isStationary() {
		return specialistId.equalsIgnoreCase(getPrevSpecialistId());
	}

	/**
	 * @return the id of the specialist this trader registered with on the
	 *         previous day
	 */
	public String getPrevSpecialistId() {
		return prevSpecialistId;
	}

	/**
	 * 
	 * @param marginalStatus
	 *          one of the following values: {@link #INTRA_MARGINAL},
	 *          {@link #EXTRA_MARGINAL}, and {@link #UNKNOWN_STATUS}
	 * 
	 */
	public void setMarginalStatus(int marginalStatus) {
		this.marginalStatus = marginalStatus;
	}

	/**
	 * 
	 * @return {@link #INTRA_MARGINAL} if this trader is intra-marginal in the
	 *         global market, {@link #EXTRA_MARGINAL} if extra-marginal, and
	 *         {@link #UNKNOWN_STATUS} if the marginal status is not determined.
	 */
	public int getMarginalStatus() {
		return marginalStatus;
	}
}

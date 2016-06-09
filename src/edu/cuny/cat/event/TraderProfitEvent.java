/*
 * JCAT - TAC Market Design Competition Platform
 * Copyright (C) 2006-2009 Jinzhong Niu, Kai Cai
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

package edu.cuny.cat.event;

/**
 * <p>
 * Informs of the profit a trader has made in the previous day.
 * </p>
 * 
 * 
 * @author Martin Chapman
 * @version $Revision: 1.7 $
 * 
 */

public class TraderProfitEvent extends AuctionEvent {

	protected String traderId;
	
	protected double profit;

	protected boolean earnedWithAdvice = false;

	public TraderProfitEvent(final String traderId, final double profit, final boolean earnedWithAdvice) {
		this.traderId = traderId;
		this.profit = profit;
		this.earnedWithAdvice = earnedWithAdvice;
	}
	
	public String getTraderId() {
		return traderId;
	}
	
	public double getTraderProfit() {
		return profit;
	}

	public boolean getEarnedWithAdvice()
	{
		return earnedWithAdvice;
	}
}

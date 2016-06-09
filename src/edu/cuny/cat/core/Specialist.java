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

package edu.cuny.cat.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.cuny.cat.market.charging.ChargingPolicy;

/**
 * Each instance of this class represents a specialist/market.
 * 
 * @author Kai Cai
 * @version $Revision: 1.17 $
 */
public class Specialist extends AccountHolder implements Cloneable {

	/**
	 * The registration cost for a trader.
	 */
	protected double registrationFee;

	/**
	 * The price of information.
	 */
	protected double informationFee;

	/**
	 * The charge for placing a shout.
	 */
	protected double shoutFee;

	/**
	 * The charge for each transaction.
	 */
	protected double transactionFee;

	/**
	 * The charge for profit made by traders in a transaction. Different from the
	 * other types of charge, this is a fraction rather than an absolute value.
	 */
	protected double profitFee;

	/**
	 * Traders registered with this specialist
	 */
	protected Map<String, Trader> traderMap;

	public Specialist(final String id) {
		this(id, null);
	}

	public Specialist(final String id, final String desc) {
		this(id, desc, 0d, 0d, 0d, 0d, 0d);
	}

	public Specialist(final String id, final String desc,
			final double registrationFee, final double informationFee,
			final double shoutFee, final double transactionFee, final double profitFee) {
		super(id, desc);
		this.registrationFee = registrationFee;
		this.informationFee = informationFee;
		this.shoutFee = shoutFee;
		this.transactionFee = transactionFee;
		this.profitFee = profitFee;

		traderMap = Collections.synchronizedMap(new HashMap<String, Trader>());
	}

	@Override
	public void reset() {
		super.reset();
		traderMap.clear();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Specialist specialist = null;
		specialist = (Specialist) super.clone();

		if (traderMap != null) {
			final Map<String, Trader> newTraderMap = new HashMap<String, Trader>();
			for (final String traderId : traderMap.keySet()) {
				newTraderMap.put(traderId, traderMap.get(traderId));
			}
			specialist.traderMap = newTraderMap;
		}

		return specialist;
	}

	public double getRegistrationFee() {
		return registrationFee;
	}

	public double getInformationFee() {
		return informationFee;
	}

	public double getShoutFee() {
		return shoutFee;
	}

	public double getTransactionFee() {
		return transactionFee;
	}

	public double getProfitFee() {
		return profitFee;
	}

	public void setRegistrationFee(final double registrationFee) {
		this.registrationFee = registrationFee;
	}

	public void setInformationFee(final double informationFee) {
		this.informationFee = informationFee;
	}

	public void setShoutFee(final double shoutFee) {
		this.shoutFee = shoutFee;
	}

	public void setTransactionFee(final double transactionFee) {
		this.transactionFee = transactionFee;
	}

	public void setProfitFee(final double profitFee) {
		this.profitFee = profitFee;
	}

	public void setFees(final double fees[]) {
		setRegistrationFee(fees[ChargingPolicy.REGISTRATION_INDEX]);
		setInformationFee(fees[ChargingPolicy.INFORMATION_INDEX]);
		setShoutFee(fees[ChargingPolicy.SHOUT_INDEX]);
		setTransactionFee(fees[ChargingPolicy.TRANSACTION_INDEX]);
		setProfitFee(fees[ChargingPolicy.PROFIT_INDEX]);
	}

	public double[] getFees() {
		return new double[] { registrationFee, informationFee, shoutFee,
				transactionFee, profitFee };
	}

	public void registerTrader(final Trader trader) {
		traderMap.put(trader.getId(), trader);
	}

	public Map<String, Trader> getTraderMap() {
		return traderMap;
	}

	public Trader getTrader(final String id) {
		return traderMap.get(id);
	}

	public void clearTraders() {
		traderMap.clear();
	}
}

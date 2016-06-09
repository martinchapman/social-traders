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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.stat.Score;

/**
 * A class that extends {@link edu.cuny.cat.core.Specialist} and includes
 * additional information for a specialist to record detailed information about
 * a specialist.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.88 $
 * 
 */

public class SpecialistInfo extends Specialist {

	static Logger logger = Logger.getLogger(SpecialistInfo.class);

	protected Score dailyScore;

	protected double dailyProfit;

	protected int supply;

	protected int demand;

	/**
	 * number of traders registered with the market daily.
	 */
	protected int numOfTraders;

	/**
	 * stores all the transactions made in the market daily.
	 */
	protected Map<String, Transaction> transactions;

	/**
	 * stores all the shouts placed in the market daily, excluding those modified
	 * by later shouts
	 */
	protected Map<String, Shout> shouts;

	public SpecialistInfo(String id) {
		this(id, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}

	public SpecialistInfo(String id, int scoreMemory) {
		this(id, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	}

	public SpecialistInfo(String id, double registrationFee,
			double informationFee, double shoutFee, double transactionFee,
			double profitFee) {
		super(id, null, registrationFee, informationFee, shoutFee, transactionFee,
				profitFee);

		transactions = Collections
				.synchronizedMap(new HashMap<String, Transaction>());
		shouts = Collections.synchronizedMap(new HashMap<String, Shout>());

		dailyScore = new Score();
	}

	public void dayOpening() {
		supply = demand = 0;
		dailyProfit = 0.0;

		shouts.clear();
		transactions.clear();

		numOfTraders = 0;
		traderMap.clear();

		dailyScore.reset();
	}

	public void setDailyProfit(double dailyProfit) {
		this.dailyProfit = dailyProfit;
	}

	public double getDailyProfit() {
		return dailyProfit;
	}

	public Score getDailyScore() {
		return dailyScore;
	}

	public void calculateDailyScore(int totalNumOfTraders,
			double dailySpecialistsProfit) {
		dailyScore.marketShare = (double) numOfTraders / totalNumOfTraders;
		dailyScore.profitShare = (dailySpecialistsProfit == 0.0D) ? 0.0D
				: dailyProfit / dailySpecialistsProfit;

		// TODO: assume that all shouts involve the same quantity of goods
		dailyScore.transactionRate = transactions.isEmpty() ? 0.0D
				: (double) (2 * transactions.size()) / (shouts.size());
		dailyScore.updateTotal();
	}

	public void addSupply(int supply) {
		this.supply += supply;
	}

	public void addDemand(int demand) {
		this.demand += demand;
	}

	public int getSupply() {
		return supply;
	}

	public int getDemand() {
		return demand;
	}

	public int getNumOfTraders() {
		return numOfTraders;
	}

	public void addNumOfTraders(int numOfTraders) {
		this.numOfTraders += numOfTraders;
	}

	public void shoutPosted(Shout shout) {
		shouts.put(shout.getId(), shout);
	}

	public Map<String, Shout> getShouts() {
		return shouts;
	}

	public void transactionPosted(Transaction transaction) {
		transactions.put(transaction.getId(), transaction);
	}

	public Map<String, Transaction> getTransactions() {
		return transactions;
	}
}

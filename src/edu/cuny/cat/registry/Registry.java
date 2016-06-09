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

package edu.cuny.cat.registry;

import edu.cuny.cat.core.AccountHolder;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.core.Trader;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEventListener;

/**
 * This interface defines the functionality of a logging module that stores cat
 * interactions. It may be implemented by writing the data into a file, a
 * database, or simply internal java data structure.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.23 $
 */
public interface Registry extends AuctionEventListener {

	// all

	public String[] getTraderIds();

	public String[] getSpecialistIds();

	public boolean containsTrader(String traderId);

	public boolean containsSpecialist(String specialistId);

	public Specialist[] getSpecialists();

	public Trader[] getTraders();

	public Specialist getSpecialist(String specialistId);

	public Trader getTrader(String traderId);

	public int getNumOfClients();

	public int getNumOfSpecialists();

	public int getNumOfTraders();

	/**
	 * @return a String including numbers of clients dead and alive.
	 */
	public String getClientStatInfo();

	// active specialists

	/**
	 * @return a list of specialists that are available for traders to do business
	 *         with
	 * 
	 */
	public Specialist[] getActiveSpecialists();

	public Specialist getActiveSpecialist(String specialistId);

	public int getNumOfActiveSpecialists();

	// working clients

	public Trader[] getWorkingTraders();

	public Specialist[] getWorkingSpecialists();

	public Trader getWorkingTrader(String traderId);

	public Specialist getWorkingSpecialist(String specialistId);

	public String[] getWorkingTraderIds();

	public String[] getWorkingSpecialistIds();

	public int getNumOfWorkingClients();

	public int getNumOfWorkingTraders();

	public int getNumOfWorkingSpecialists();

	// failed

	public AccountHolder getFailedClient(String clientId);

	// expected specialist

	public void addExpectedSpecialist(Specialist specialist);

	public Specialist getExpectedSpecialist(String clientId);

	// subscription and registration

	public String[] getSubscriberIds(String specialistId);

	public String getBrokerId(String traderId);

	// shout and transaction

	public Shout getShout(String shoutId);

	public Shout[] getShouts();

	public Transaction getTransaction(String transactionId);

	public Transaction[] getTransactions();

	// misc

	public void start();

	public void stop();
}
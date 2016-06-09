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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.collections15.bag.TreeBag;
import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.GameStartingEvent;
import edu.cuny.cat.event.IdAssignedEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Resetable;
import edu.cuny.util.SortedTreeList;
import edu.cuny.util.Utils;

/**
 * <p>
 * A report that keeps a historical record of the shouts in the market that lead
 * to the last N transactions. This logger is used to keep historical data that
 * is used by various different trading strategies, especially GD.
 * </p>
 * 
 * <p>
 * Since {@link edu.cuny.cat.trader.strategy.GDStrategy} uses this report to
 * compute the number of shouts above or below a certain price, which leads to
 * slow simulation, SortedView and IncreasingQueryAccelerator are introduced to
 * speed up GDStrategy's queries based on the pattern of prices of concern.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.memorysize</tt><br>
 * <font size=-1>int > 0 (5 by default) </font></td>
 * <td valign=top>(the length of most recent history to be recorded)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>historical_report</tt></td>
 * </tr>
 * </table>
 * 
 * @see edu.cuny.cat.trader.strategy.GDStrategy
 * 
 * @author Steve Phelps
 * @version $Revision: 1.32 $
 */

public class HistoricalReport implements GameReport, Serializable, Resetable {

	protected static Logger logger = Logger.getLogger(HistoricalReport.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_DEF_BASE = "historical_report";

	public static final String P_MEMORYSIZE = "memorysize";

	public static final String P_ROUNDRESET = "roundreset";

	public static final String P_DEBUG = "debug";

	/**
	 * the default size of the memory to contain shouts in terms of the number of
	 * transactions covered.
	 */
	public final static int DEFAULT_MEMORYSIZE = 5;

	/**
	 * asks in the order they arrive.
	 */
	protected LinkedList<Shout> asks;

	/**
	 * bids in the order they arrive.
	 */
	protected LinkedList<Shout> bids;

	/**
	 * shouts sorted based on price and id if prices are equal.
	 */
	protected TreeBag<Shout> sortedShouts;

	/**
	 * shouts in the memory that have been matched.
	 */
	protected Set<Shout> matchedShouts;

	/**
	 * a mapping from shout IDs to those shouts in the memory.
	 */
	protected Map<String, Shout> shoutMap;

	/**
	 * the size of the memory to contain shouts in terms of the number of
	 * transactions covered.
	 */
	protected int memorySize = HistoricalReport.DEFAULT_MEMORYSIZE;

	/**
	 * 
	 */
	protected int currentMemoryCell = 0;

	/**
	 * records the numbers of bids placed between transactions in the memory, each
	 * entry for the interval between two subsequent transactions.
	 */
	protected int[] memoryBids;

	/**
	 * records the numbers of asks placed between transactions in the memory, each
	 * entry for the interval between two subsequent transactions.
	 */
	protected int[] memoryAsks;

	/**
	 * 
	 */
	protected double lowestAskPrice;

	/**
	 * 
	 */
	protected double highestBidPrice;

	/**
	 * 
	 */
	protected Shout highestUnmatchedBid;

	/**
	 * 
	 */
	protected Shout lowestUnmatchedAsk;

	/**
	 * the object that helps to save time in querying about shouts in the history.
	 */
	protected IncreasingQueryAccelerator accelerator;

	/**
	 * used to make this report observable so that {@link #accelerator} can reset.
	 */
	protected Observable observableProxy;

	/**
	 * flag used to control whether to clear the record of matched shouts each
	 * round
	 */
	protected boolean roundReset = false;

	/**
	 * flag used to control whether to do additional debugging.
	 */
	protected boolean isDebugging = false;

	/**
	 * the ID of the GD trader that uses this report; for debugging purpose only
	 */
	protected String traderId;

	public HistoricalReport() {
		asks = new LinkedList<Shout>();
		bids = new LinkedList<Shout>();
		sortedShouts = new TreeBag<Shout>(new ShoutComparator());
		matchedShouts = Collections.synchronizedSet(new HashSet<Shout>());
		shoutMap = Collections.synchronizedMap(new HashMap<String, Shout>());

		observableProxy = new Observable() {
			@Override
			public void notifyObservers() {
				setChanged();
				super.notifyObservers();
			}
		};
	}

	public void addObserver(final Observer o) {
		observableProxy.addObserver(o);
	}

	public void deleteObserver(final Observer o) {
		observableProxy.deleteObserver(o);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(HistoricalReport.P_DEF_BASE);
		memorySize = parameters.getIntWithDefault(base
				.push(HistoricalReport.P_MEMORYSIZE), defBase
				.push(HistoricalReport.P_MEMORYSIZE), memorySize);
		roundReset = parameters.getBoolean(
				base.push(HistoricalReport.P_ROUNDRESET), defBase
						.push(HistoricalReport.P_ROUNDRESET), roundReset);
		isDebugging = parameters.getBoolean(base.push(HistoricalReport.P_DEBUG),
				defBase.push(HistoricalReport.P_DEBUG), isDebugging);
	}

	public void initialize() {

		memoryBids = new int[memorySize];
		memoryAsks = new int[memorySize];

		init1();
	}

	private void init1() {
		initializePriceRanges();
		observableProxy.notifyObservers();
	}

	private void initializePriceRanges() {
		highestBidPrice = Double.NEGATIVE_INFINITY;
		lowestAskPrice = Double.POSITIVE_INFINITY;

		highestUnmatchedBid = null;
		lowestUnmatchedAsk = null;
	}

	public void reset() {
		bids.clear();
		asks.clear();

		matchedShouts.clear();
		sortedShouts.clear();

		shoutMap.clear();

		for (int i = 0; i < memorySize; i++) {
			memoryBids[i] = 0;
			memoryAsks[i] = 0;
		}

		init1();

		disableIncreasingQueryAccelerator();
	}

	public void setMemorySize(final int memorySize) {
		this.memorySize = memorySize;
		initialize();
	}

	public int getMemorySize() {
		return memorySize;
	}

	public boolean isDebugging() {
		return isDebugging;
	}

	public void setDebugging(final boolean isDebugging) {
		this.isDebugging = isDebugging;
	}

	public void checkConsistency() {
		if (isDebugging()) {
			if (asks.size() + bids.size() != sortedShouts.size()) {
				HistoricalReport.logger.info("inconsistency found !");
				HistoricalReport.logger.info(asks.size() + " " + getAsks() + "\n");
				HistoricalReport.logger.info(bids.size() + " " + getBids() + "\n");
				HistoricalReport.logger.info(sortedShouts.size() + " "
						+ getSortedShouts() + "\n");
			}
		}
	}

	/**
	 * makes a copy of any shout
	 * 
	 * @param orig
	 * @return an exact copy of the original shout
	 */
	private Shout makeShoutCopy(final Shout orig) {
		final Shout copy = new Shout();
		copy.copyFrom(orig);
		return copy;
	}

	protected void removeNShouts(final int n, final LinkedList<Shout> shouts) {
		for (int i = 0; i < n; i++) {
			final Shout shout = shouts.removeFirst();

			if (isDebugging() && (sortedShouts.getCount(shout) > 1)) {
				HistoricalReport.logger.info(sortedShouts.getCount(shout) + " "
						+ shout.toString());
			}

			final int count = sortedShouts.getCount(shout);

			// TODO: which one is removed ? or doesn't matter?
			sortedShouts.remove(shout, 1);

			if (count - sortedShouts.getCount(shout) != 1) {
				HistoricalReport.logger
						.error("failed in removing exactly the single shout: " + shout);
				HistoricalReport.logger.error(count + " -> "
						+ sortedShouts.getCount(shout) + " " + shout);
			}

			matchedShouts.remove(shout);

			// if the shout is the last one with the id, remove its record in shoutMap
			if (shout == getMappedShout(shout.getId())) {
				shoutMap.remove(shout.getId());
				// } else {
				// HistoricalReport.logger.info("Earlier shout " + prettyString(shout)
				// + " removed from shout list, which has a later modifying shout "
				// + prettyString(getMappedShout(shout.getId())) + " in shout map.");
			}
		}
	}

	protected void markMatchedShout(final Shout matched) {
		Shout lastPlaced = getMappedShout(matched.getId());

		if (lastPlaced != null) {
			// found corresponding shout copy in record
			if (lastPlaced.getPrice() == matched.getPrice()) {
				// matching record
				lastPlaced.setState(Shout.MATCHED);
				matchedShouts.add(lastPlaced);
			} else {
				// price doesn't match, the latest update is missing somehow, so make it
				// up

				// TODO: to check when this could happen
				HistoricalReport.logger.warn("latest update of the shout on file is "
						+ lastPlaced);
				HistoricalReport.logger.warn("matched shout: " + matched);

				/* add the event on placing the missing shout */
				lastPlaced = makeShoutCopy(matched);
				lastPlaced.setState(Shout.PLACED);
				final ShoutPostedEvent event = new ShoutPostedEvent(lastPlaced);
				updateShoutLog(event);

				markMatchedShout(matched);
			}
		} else {
			// This is normal. It's likely that the matched shout is too old to exist
			// in shoutMap.

			// This happens often in CH in particular since CH clears the market at
			// the end of a round and consecutive transactions may lead to empty the
			// asks and bids in the memory of GD.

			if (isDebugging()) {
				HistoricalReport.logger.warn("missing shout: " + matched);
				HistoricalReport.logger.warn("asks: " + prettyString(asks, 0, -1));
				HistoricalReport.logger.warn("bids: " + prettyString(bids, 0, -1));
				HistoricalReport.logger.warn("\n");
				printState();

				HistoricalReport.logger.warn("Specialist: "
						+ matched.getSpecialist().getId());
			}
		}
	}

	/**
	 * 
	 * @param shouts
	 * @param index
	 * @param length
	 * @return a string that prints out the specified shouts in the list in a
	 *         pretty way.
	 */
	protected String prettyString(final LinkedList<Shout> shouts,
			final int index, final int length) {
		int last = shouts.size();
		if (length >= 0) {
			last = index + length;
		}

		String s = "";

		for (int i = index; i < last; i++) {
			if (s.length() == 0) {
				s += "[";
			} else {
				s += ", ";
			}
			s += prettyString(shouts.get(i));
		}

		if (s.length() == 0) {
			s += "[";
		}

		s += "]";

		return s;
	}

	/**
	 * 
	 * @param shouts
	 * @param counts
	 * @return a string that prints out the shouts in the list and grouped
	 *         according to the counts array in a pretty way.
	 */
	protected String prettyString(final LinkedList<Shout> shouts,
			final int counts[]) {
		String s = "";
		int size = shouts.size();
		for (int i = 0; i < memorySize; i++) {
			final int index = (currentMemoryCell + memorySize - i) % memorySize;
			s = prettyString(shouts, size - counts[index], counts[index]) + "\n" + s;
			size -= counts[index];
		}

		return s;
	}

	/**
	 * 
	 * @param shout
	 * @return a string that prints out the shout info in a pretty way.
	 */
	protected String prettyString(final Shout shout) {
		String s = "";
		if (shout.isMatched()) {
			s += "*";
		}

		final String traderId = shout.getTrader().getId();

		return s
				+ shout.getId()
				+ "/"
				+ Utils.format(shout.getPrice())
				+ " @ "
				+ traderId
				+ "/"
				+ GameController.getInstance().getRegistry().getTrader(traderId)
						.getPrivateValue();
	}

	/**
	 * prints out the current state of this report.
	 */
	protected void printState() {
		HistoricalReport.logger.info("asks\n----------\n"
				+ prettyString(asks, memoryAsks));
		HistoricalReport.logger.info("bids\n----------\n"
				+ prettyString(bids, memoryBids));
		HistoricalReport.logger.info("currentMemoryCell: " + currentMemoryCell);
		HistoricalReport.logger.info("lowest unmatched ask: " + lowestUnmatchedAsk);
		HistoricalReport.logger.info("highest unmatched bid: "
				+ highestUnmatchedBid);
	}

	/**
	 * prints out the current state of this report in a concise way.
	 */
	protected void printShortState() {
		HistoricalReport.logger.info("memoryBids: " + Arrays.toString(memoryBids));
		HistoricalReport.logger.info("memoryAsks: " + Arrays.toString(memoryAsks));
		HistoricalReport.logger.info("currentMemoryCell: " + currentMemoryCell);
		HistoricalReport.logger.info("lowest unmatched ask: " + lowestUnmatchedAsk);
		HistoricalReport.logger.info("highest unmatched bid: "
				+ highestUnmatchedBid);
	}

	/**
	 * updates the log of transactions and shouts when a transaction is made in
	 * the market.
	 * 
	 * @param event
	 */
	protected void updateTransPriceLog(final TransactionPostedEvent event) {
		final Transaction transaction = event.getTransaction();

		if (!transaction.getAsk().isMatched() || !transaction.getBid().isMatched()) {
			HistoricalReport.logger
					.fatal("Invalid state in matched shouts received at " + traderId
							+ " !");
			HistoricalReport.logger.fatal("ask: " + transaction.getAsk());
			HistoricalReport.logger.fatal("bid: " + transaction.getBid());
			new Exception().printStackTrace();
			printShortState();
		}

		if (isDebugging()) {
			HistoricalReport.logger.info("\nTRANSACTION: "
					+ prettyString(transaction.getAsk()) + " - "
					+ prettyString(transaction.getBid()) + "\n");
		}

		markMatchedShout(transaction.getAsk());
		markMatchedShout(transaction.getBid());

		currentMemoryCell = (currentMemoryCell + 1) % memorySize;
		if ((memoryAsks[currentMemoryCell] > 0)
				|| (memoryBids[currentMemoryCell] > 0)) {

			if (isDebugging()) {
				HistoricalReport.logger.info(memoryAsks[currentMemoryCell]
						+ " asks to be removed");
				HistoricalReport.logger.info(memoryBids[currentMemoryCell]
						+ " bids to be removed");

				if (memoryAsks[currentMemoryCell] > asks.size()) {
					HistoricalReport.logger
							.info("Asks to be removed do not exist in asks list !");
				}

				if (memoryBids[currentMemoryCell] > bids.size()) {
					HistoricalReport.logger
							.info("Bids to be removed do not exist in bids list !");
				}

				if (memoryBids[currentMemoryCell] + memoryAsks[currentMemoryCell] > sortedShouts
						.size()) {
					HistoricalReport.logger
							.info("Shouts to be removed do not exist in sorted shout list !");
				}
			}

			removeNShouts(memoryAsks[currentMemoryCell], asks);
			removeNShouts(memoryBids[currentMemoryCell], bids);
			memoryAsks[currentMemoryCell] = 0;
			memoryBids[currentMemoryCell] = 0;

			checkConsistency();
		}

		if (transaction.getAsk() == lowestUnmatchedAsk) {
			lowestUnmatchedAsk = null;
		}

		if (transaction.getBid() == highestUnmatchedBid) {
			highestUnmatchedBid = null;
		}

		observableProxy.notifyObservers();

		// if (isDebugging()) {
		// printState();
		// }
	}

	/**
	 * updates the log of shouts when a shout is posted in the market.
	 * 
	 * @param event
	 */
	protected void updateShoutLog(final ShoutPostedEvent event) {

		if (isDebugging()) {
			HistoricalReport.logger.info("\nPOSTED: "
					+ prettyString(event.getShout()) + ".\n");
		}

		/* make a fresh copy of the shout */
		Shout shout = shoutMap.get(event.getShout().getId());

		// allow duplicate shouts since a trader may be able to place shouts at the
		// same price.
		if ((shout != null) && (shout.getPrice() == event.getShout().getPrice())
				&& (shout.getQuantity() == event.getShout().getQuantity())) {
			// Duplicate shouts are received.

			// do nothing except

			// if (isDebugging()) {
			// HistoricalReport.logger.info("Duplicate shout received: " + shout);
			// }
		}

		shout = makeShoutCopy(event.getShout());

		/*
		 * Shouts may be modified. This would keep the latest modified shouts in the
		 * map.
		 */
		shoutMap.put(shout.getId(), shout);

		addToSortedShouts(shout);

		if (shout.isAsk()) {
			asks.add(shout);
			memoryAsks[currentMemoryCell]++;
			if (shout.getPrice() < lowestAskPrice) {
				lowestAskPrice = shout.getPrice();
			}

			if ((lowestUnmatchedAsk == null)
					|| (lowestUnmatchedAsk.getPrice() > shout.getPrice())) {
				lowestUnmatchedAsk = shout;
			}
		} else {
			bids.add(shout);
			memoryBids[currentMemoryCell]++;
			if (shout.getPrice() > highestBidPrice) {
				highestBidPrice = shout.getPrice();
			}

			if ((highestUnmatchedBid == null)
					|| (highestUnmatchedBid.getPrice() < shout.getPrice())) {
				highestUnmatchedBid = shout;
			}
		}

		checkConsistency();

		observableProxy.notifyObservers();

		// if (getDebug()) {
		// printShortState();
		// }
	}

	protected void roundClosed(final RoundClosedEvent event) {

		if (roundReset) {
			// NOTE: in JASA, auctioneer clears acceptedShouts records every round,
			// which however still leads to high efficiency. It's unclear why it is
			// done
			// this way in JASA and if there is any metrics that JASA fails to
			// replicate according to the literature. Doing this here lowers the
			// efficiency, which is expected.
			//
			//
			matchedShouts.clear();
		}

		initializePriceRanges();
		observableProxy.notifyObservers();
	}

	protected Shout getMappedShout(final String shoutId) {
		return shoutMap.get(shoutId);
	}

	protected boolean isMatched(final Shout shout) {
		return matchedShouts.contains(shout);
	}

	protected void addToSortedShouts(final Shout shout) {
		sortedShouts.add(shout);

		// shouts with same id and same price are possible, if a market allows a
		// modifying shout to offer a price same as the earlier offer.

		// if (sortedShouts.getCount(shout) > 1) {
		// logger.debug("Duplicate shout: " + sortedShouts.getCount(shout) + "\n"
		// + shout);
		// logger.debug(getMappedShout(shout.getId()) + "\n");
		// }
	}

	public void eventOccurred(final AuctionEvent event) {
		if (event instanceof IdAssignedEvent) {
			traderId = ((IdAssignedEvent) event).getId();
		} else if (event instanceof GameStartingEvent) {
			reset();
		} else if (event instanceof RoundClosedEvent) {
			roundClosed((RoundClosedEvent) event);
		} else if (event instanceof ShoutPostedEvent) {
			updateShoutLog((ShoutPostedEvent) event);
		} else if (event instanceof TransactionPostedEvent) {
			updateTransPriceLog((TransactionPostedEvent) event);
		} else if (event instanceof DayClosedEvent) {
			if (isDebugging()) {
				// a problem that causes used memory to constantly grow is the lists and
				// maps here hold unneeded shouts over time. this helps to check if this
				// happens.
				HistoricalReport.logger.info("shoutMap.size: " + shoutMap.size());
				HistoricalReport.logger.info("asks: " + asks.size());
				HistoricalReport.logger.info("bids: " + bids.size());
				HistoricalReport.logger.info("sortedShouts: " + sortedShouts.size());

				HistoricalReport.logger.info("\n");
				HistoricalReport.logger.info("shoutMap.keySet: "
						+ shoutMap.keySet().toString() + "\n");
			}
		}
	}

	public double getHighestBidPrice() {
		return highestBidPrice;
	}

	public double getLowestAskPrice() {
		return lowestAskPrice;
	}

	public double getHighestUnacceptedBidPrice() {
		if (highestUnmatchedBid != null) {
			return highestUnmatchedBid.getPrice();
		}

		final Iterator<Shout> i = bids.iterator();
		double highestUnacceptedBidPrice = Double.NEGATIVE_INFINITY;
		while (i.hasNext()) {
			final Shout s = i.next();
			if (!isMatched(s)) {
				if (s.getPrice() > highestUnacceptedBidPrice) {
					highestUnacceptedBidPrice = s.getPrice();
					highestUnmatchedBid = s;
				}
			}
		}
		return highestUnacceptedBidPrice;
	}

	public double getLowestAcceptedBidPrice() {
		final Iterator<Shout> i = bids.iterator();
		double lowestAcceptedBidPrice = Double.POSITIVE_INFINITY;
		while (i.hasNext()) {
			final Shout s = i.next();
			if (isMatched(s)) {
				if (s.getPrice() < lowestAcceptedBidPrice) {
					lowestAcceptedBidPrice = s.getPrice();
				}
			}
		}
		return lowestAcceptedBidPrice;
	}

	public double getLowestUnacceptedAskPrice() {
		if (lowestUnmatchedAsk != null) {
			return lowestUnmatchedAsk.getPrice();
		}

		final Iterator<Shout> i = asks.iterator();
		double lowestUnacceptedBidPrice = Double.POSITIVE_INFINITY;
		while (i.hasNext()) {
			final Shout s = i.next();
			if (!isMatched(s)) {
				if (s.getPrice() < lowestUnacceptedBidPrice) {
					lowestUnacceptedBidPrice = s.getPrice();
					lowestUnmatchedAsk = s;
				}
			}
		}
		return lowestUnacceptedBidPrice;
	}

	public double getHighestAcceptedAskPrice() {
		final Iterator<Shout> i = asks.iterator();
		double highestAcceptedAskPrice = Double.NEGATIVE_INFINITY;
		while (i.hasNext()) {
			final Shout s = i.next();
			if (isMatched(s)) {
				if (s.getPrice() > highestAcceptedAskPrice) {
					highestAcceptedAskPrice = s.getPrice();
				}
			}
		}
		return highestAcceptedAskPrice;
	}

	public List<Shout> getBids() {
		return bids;
	}

	public List<Shout> getAsks() {
		return asks;
	}

	public Set<Shout> getMatchedShouts() {
		return matchedShouts;
	}

	public TreeBag<Shout> getSortedShouts() {
		return sortedShouts;
	}

	public int getNumberOfAsks(final double price, final boolean matched) {
		return getNumberOfShouts(asks, price, matched);
	}

	public int getNumberOfBids(final double price, final boolean matched) {
		return getNumberOfShouts(bids, price, matched);
	}

	public Iterator<Shout> sortedShoutIterator() {
		return sortedShouts.iterator();
	}

	/**
	 * 
	 * @param shouts
	 *          the list of shouts to be considered for the counting
	 * @param price
	 *          the sign of price controls whether higher shouts or lower shouts
	 *          are needed; if it is positive, higher shouts are counted;
	 *          otherwise lower shouts.
	 * @param matched
	 *          whether only matched shouts are counted or not
	 * @return the number of shouts that meet the specified condition
	 */
	public int getNumberOfShouts(final List<Shout> shouts, final double price,
			final boolean matched) {

		int numShouts = 0;
		final Iterator<Shout> i = shouts.iterator();
		while (i.hasNext()) {
			final Shout shout = i.next();
			if (((price >= 0) && (shout.getPrice() >= price))
					|| ((price < 0) && (shout.getPrice() <= -price))) {
				if (matched) {
					if (isMatched(shout)) {
						numShouts++;
					}
				} else {
					numShouts++;
				}
			}
		}

		return numShouts;
	}

	public void produceUserOutput() {
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();
		s += " " + "memorySize:" + memorySize + " isDebugging:" + isDebugging
				+ (roundReset ? " roundReset:true" : "");
		return s;
	}

	public IncreasingQueryAccelerator getIncreasingQueryAccelerator() {
		if (accelerator == null) {
			accelerator = new IncreasingQueryAccelerator();
		}

		return accelerator;
	}

	public void disableIncreasingQueryAccelerator() {
		if (accelerator != null) {
			accelerator.destroy();
			accelerator = null;
		}
	}

	/*
	 * to sort shouts based on both its price and its id.
	 */
	public class ShoutComparator implements Comparator<Shout> {

		public int compare(final Shout s0, final Shout s1) {
			int result = s0.compareTo(s1);
			if (result == 0) {
				result = s0.getId().compareTo(s1.getId());
			}

			return result;
		}
	}

	/**
	 * a class providing sorted lists of shouts.
	 * 
	 */
	public class SortedView extends Observable implements Observer {

		private SortedTreeList<Shout> sortedAsks;

		private SortedTreeList<Shout> sortedBids;

		private SortedTreeList<Shout> sortedAcceptedAsks;

		private SortedTreeList<Shout> sortedAcceptedBids;

		private SortedTreeList<Shout> sortedRejectedAsks;

		private SortedTreeList<Shout> sortedRejectedBids;

		private boolean toBeReset;

		public SortedView() {
			HistoricalReport.this.addObserver(this);
			toBeReset = true;
		}

		public void destroy() {
			HistoricalReport.this.deleteObserver(this);
		}

		public void reset() {

			if (sortedAsks != null) {
				sortedAsks.clear();
			} else {
				sortedAsks = new SortedTreeList<Shout>("sortedAsks");
			}

			if (sortedBids != null) {
				sortedBids.clear();
			} else {
				sortedBids = new SortedTreeList<Shout>("sortedBids");
			}

			if (sortedAcceptedAsks != null) {
				sortedAcceptedAsks.clear();
			} else {
				sortedAcceptedAsks = new SortedTreeList<Shout>("sortedAcceptedAsks");
			}

			if (sortedAcceptedBids != null) {
				sortedAcceptedBids.clear();
			} else {
				sortedAcceptedBids = new SortedTreeList<Shout>("sortedAcceptedBids");
			}

			if (sortedRejectedAsks != null) {
				sortedRejectedAsks.clear();
			} else {
				sortedRejectedAsks = new SortedTreeList<Shout>("sortedRejectedAsks");
			}

			if (sortedRejectedBids != null) {
				sortedRejectedBids.clear();
			} else {
				sortedRejectedBids = new SortedTreeList<Shout>("sortedRejectedBids");
			}

			Shout s;

			Iterator<Shout> i = asks.iterator();
			while (i.hasNext()) {
				s = i.next();
				sortedAsks.add(s);
				if (isMatched(s)) {
					sortedAcceptedAsks.add(s);
				} else {
					sortedRejectedAsks.add(s);
				}
			}

			i = bids.iterator();
			while (i.hasNext()) {
				s = i.next();
				sortedBids.add(s);
				if (isMatched(s)) {
					sortedAcceptedBids.add(s);
				} else {
					sortedRejectedBids.add(s);
				}
			}

			setChanged();
			notifyObservers();
		}

		public void update(final Observable o, final Object arg) {
			toBeReset = true;
			setChanged();
			notifyObservers();
		}

		public void resetIfNeeded() {
			if (toBeReset) {
				reset();
				toBeReset = false;
			}
		}

		public SortedTreeList<Shout> getSortedAsks() {
			resetIfNeeded();
			return sortedAsks;
		}

		public SortedTreeList<Shout> getSortedBids() {
			resetIfNeeded();
			return sortedBids;
		}

		public SortedTreeList<Shout> getSortedAcceptedAsks() {
			resetIfNeeded();
			return sortedAcceptedAsks;
		}

		public SortedTreeList<Shout> getSortedAcceptedBids() {
			resetIfNeeded();
			return sortedAcceptedBids;
		}

		public SortedTreeList<Shout> getSortedRejectedAsks() {
			resetIfNeeded();
			return sortedRejectedAsks;
		}

		public SortedTreeList<Shout> getSortedRejectedBids() {
			resetIfNeeded();
			return sortedRejectedBids;
		}

	}

	/**
	 * a class to speed up queries from GDStrategy regarding the number of shouts
	 * above or below a certain price. It is designed based on the pattern of
	 * increasing prices queried about.
	 * 
	 */
	public class IncreasingQueryAccelerator implements Observer {

		protected ListIterator<Shout> asksI;

		protected ListIterator<Shout> bidsI;

		protected ListIterator<Shout> acceptedAsksI;

		protected ListIterator<Shout> acceptedBidsI;

		protected ListIterator<Shout> rejectedAsksI;

		protected ListIterator<Shout> rejectedBidsI;

		protected int numOfAsksBelow;

		protected int numOfBidsAbove;

		protected int numOfAcceptedAsksAbove;

		protected int numOfAcceptedBidsBelow;

		protected int numOfRejectedAsksBelow;

		protected int numOfRejectedBidsAbove;

		protected double priceForAsksBelow;

		protected double priceForBidsAbove;

		protected double priceForAcceptedAsksAbove;

		protected double priceForAcceptedBidsBelow;

		protected double priceForRejectedAsksBelow;

		protected double priceForRejectedBidsAbove;

		protected SortedView view;

		private boolean toBeReset;

		public IncreasingQueryAccelerator() {
			view = new SortedView();
			view.addObserver(this);
			toBeReset = true;
		}

		public SortedView getSortedView() {
			return view;
		}

		public void destroy() {
			if (view != null) {
				view.deleteObserver(this);
				view.destroy();
				view = null;
			}
		}

		/*
		 * for debug purpose to display the internal state of this view.
		 */
		public String toPrettyString() {
			String s = getClass().getSimpleName();

			s += "\n"
					+ Utils.indent(view.getSortedAsks().toString() + " ("
							+ numOfAsksBelow + " below " + priceForAsksBelow + ")");
			s += "\n"
					+ Utils.indent(view.getSortedBids().toString() + " ("
							+ numOfBidsAbove + " above " + priceForBidsAbove + ")");
			s += "\n"
					+ Utils.indent(view.getSortedAcceptedAsks().toString() + " ("
							+ numOfAcceptedAsksAbove + " above " + priceForAcceptedAsksAbove
							+ ")");
			s += "\n"
					+ Utils.indent(view.getSortedAcceptedBids().toString() + " ("
							+ numOfAcceptedBidsBelow + " below " + priceForAcceptedBidsBelow
							+ ")");
			s += "\n"
					+ Utils.indent(view.getSortedRejectedAsks().toString() + " ("
							+ numOfRejectedAsksBelow + " below " + priceForRejectedAsksBelow
							+ ")");
			s += "\n"
					+ Utils.indent(view.getSortedRejectedBids().toString() + " ("
							+ numOfRejectedBidsAbove + " above " + priceForRejectedBidsAbove
							+ ")");

			return s;
		}

		/*
		 * resets all the iterations and counting variables when the underlying
		 * sorted view changes.
		 */
		public void update(final Observable o, final Object arg) {
			toBeReset = true;
		}

		protected void resetIfNeeded() {
			if (toBeReset) {
				reset();
				toBeReset = false;
			}
		}

		public void reset() {
			resetForAsksBelow();
			resetForBidsAbove();
			resetForAcceptedAsksAbove();
			resetForAcceptedBidsBelow();
			resetForRejectedAsksBelow();
			resetForRejectedBidsAbove();
		}

		protected void resetForAsksBelow() {
			asksI = view.getSortedAsks().listIterator();
			numOfAsksBelow = 0;
			priceForAsksBelow = -1;
		}

		protected void resetForBidsAbove() {
			bidsI = view.getSortedBids().listIterator();
			numOfBidsAbove = view.getSortedBids().size();
			priceForBidsAbove = -1;
		}

		protected void resetForAcceptedAsksAbove() {
			acceptedAsksI = view.getSortedAcceptedAsks().listIterator();
			numOfAcceptedAsksAbove = view.getSortedAcceptedAsks().size();
			priceForAcceptedAsksAbove = -1;
		}

		protected void resetForAcceptedBidsBelow() {
			acceptedBidsI = view.getSortedAcceptedBids().listIterator();
			numOfAcceptedBidsBelow = 0;
			priceForAcceptedBidsBelow = -1;
		}

		protected void resetForRejectedAsksBelow() {
			rejectedAsksI = view.getSortedRejectedAsks().listIterator();
			numOfRejectedAsksBelow = 0;
			priceForRejectedAsksBelow = -1;
		}

		protected void resetForRejectedBidsAbove() {
			rejectedBidsI = view.getSortedRejectedBids().listIterator();
			numOfRejectedBidsAbove = view.getSortedRejectedBids().size();
			priceForRejectedBidsAbove = -1;
		}

		public int getNumOfAsksBelow(final double price) {
			resetIfNeeded();

			if (priceForAsksBelow > price) {
				resetForAsksBelow();
			}
			priceForAsksBelow = price;

			while (asksI.hasNext()) {
				if ((asksI.next()).getPrice() <= price) {
					numOfAsksBelow++;
				} else {
					try {
						asksI.previous();
					} catch (final Exception e) {
						HistoricalReport.logger.info(e);
						asksI.previous();
					}
					break;
				}
			}

			return numOfAsksBelow;
		}

		public int getNumOfBidsAbove(final double price) {
			resetIfNeeded();

			if (priceForBidsAbove > price) {
				resetForBidsAbove();
			}
			priceForBidsAbove = price;

			while (bidsI.hasNext()) {
				if ((bidsI.next()).getPrice() < price) {
					numOfBidsAbove--;
				} else {
					try {
						bidsI.previous();
					} catch (final Exception e) {
						HistoricalReport.logger.info(e);
						bidsI.previous();
					}
					break;
				}
			}

			return numOfBidsAbove;
		}

		public int getNumOfAcceptedAsksAbove(final double price) {
			resetIfNeeded();

			if (priceForAcceptedAsksAbove > price) {
				resetForAcceptedAsksAbove();
			}
			priceForAcceptedAsksAbove = price;

			while (acceptedAsksI.hasNext()) {
				if ((acceptedAsksI.next()).getPrice() < price) {
					numOfAcceptedAsksAbove--;
				} else {
					try {
						acceptedAsksI.previous();
					} catch (final Exception e) {
						HistoricalReport.logger.info(e);
						acceptedAsksI.previous();
					}
					break;
				}
			}

			return numOfAcceptedAsksAbove;
		}

		public int getNumOfAcceptedBidsBelow(final double price) {
			resetIfNeeded();

			if (priceForAcceptedBidsBelow > price) {
				resetForAcceptedBidsBelow();
			}
			priceForAcceptedBidsBelow = price;

			while (acceptedBidsI.hasNext()) {
				if ((acceptedBidsI.next()).getPrice() <= price) {
					numOfAcceptedBidsBelow++;
				} else {
					// NOTE: due to a possible bug in TreeList,
					// NullPointerException may be
					// thrown. Simply doing it again seems working fine.
					try {
						acceptedBidsI.previous();
					} catch (final Exception e) {
						HistoricalReport.logger.info(e);
						acceptedBidsI.previous();
					}
					break;
				}
			}

			return numOfAcceptedBidsBelow;
		}

		public int getNumOfRejectedAsksBelow(final double price) {
			resetIfNeeded();

			if (priceForRejectedAsksBelow > price) {
				resetForRejectedAsksBelow();
			}
			priceForRejectedAsksBelow = price;

			while (rejectedAsksI.hasNext()) {
				if ((rejectedAsksI.next()).getPrice() <= price) {
					numOfRejectedAsksBelow++;
				} else {
					try {
						rejectedAsksI.previous();
					} catch (final Exception e) {
						HistoricalReport.logger.info(e);
						rejectedAsksI.previous();
					}
					break;
				}
			}

			return numOfRejectedAsksBelow;
		}

		public int getNumOfRejectedBidsAbove(final double price) {
			resetIfNeeded();

			if (priceForRejectedBidsAbove > price) {
				resetForRejectedBidsAbove();
			}
			priceForRejectedBidsAbove = price;

			while (rejectedBidsI.hasNext()) {
				if ((rejectedBidsI.next()).getPrice() < price) {
					numOfRejectedBidsAbove--;
				} else {
					try {
						rejectedBidsI.previous();
					} catch (final Exception e) {
						HistoricalReport.logger.info(e);
						rejectedBidsI.previous();
					}
					break;
				}
			}

			return numOfRejectedBidsAbove;
		}
	}
}

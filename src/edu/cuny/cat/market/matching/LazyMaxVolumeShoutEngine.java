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

package edu.cuny.cat.market.matching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections15.iterators.CollatingIterator;
import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.DuplicateShoutException;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.Uniform;
import edu.cuny.util.Galaxy;
import edu.cuny.util.SortedTreeList;

/**
 * <p>
 * This class provides a matching policy that differs from
 * {@link FourHeapShoutEngine} in the sense that it maximizes the matching
 * quantity by pairing high intra-marginal shouts with extra-marginal shouts.
 * </p>
 * 
 * <p>
 * Due to performance consideration, this engine does not maintain a matched set
 * of shouts for the moment.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 */

public class LazyMaxVolumeShoutEngine extends ShoutEngine {

	static Logger logger = Logger.getLogger(LazyMaxVolumeShoutEngine.class);

	/**
	 * asks in ascending order
	 */
	protected SortedTreeList<Shout> asks = new SortedTreeList<Shout>("asks",
			ShoutEngine.AscendingOrder);

	/**
	 * bids in ascending order
	 */
	protected SortedTreeList<Shout> bids = new SortedTreeList<Shout>("bids",
			ShoutEngine.AscendingOrder);

	/**
	 * Matched bids in ascending order
	 */
	protected SortedTreeList<Shout> bIn = new SortedTreeList<Shout>(
			"matched bids", ShoutEngine.AscendingOrder);

	/**
	 * Unmatched bids in ascending order
	 */
	protected SortedTreeList<Shout> bOut = new SortedTreeList<Shout>(
			"unmatched bids", ShoutEngine.AscendingOrder);

	/**
	 * Matched asks in ascending order
	 */
	protected SortedTreeList<Shout> sIn = new SortedTreeList<Shout>(
			"matched asks", ShoutEngine.AscendingOrder);

	/**
	 * Unmatched asks in ascending order
	 */
	protected SortedTreeList<Shout> sOut = new SortedTreeList<Shout>(
			"unmatched asks", ShoutEngine.AscendingOrder);

	/**
	 * used to randomize the order of matching pairs of shouts
	 */
	protected Uniform uniform;

	public LazyMaxVolumeShoutEngine() {
		uniform = new Uniform(0, 1, Galaxy.getInstance().getDefaultTyped(
				GlobalPRNG.class).getEngine());
	}

	@Override
	public synchronized void reset() {
		super.reset();

		asks.clear();
		bids.clear();
		sIn.clear();
		bIn.clear();
		sOut.clear();
		bOut.clear();
	}

	@Override
	public synchronized void removeShout(final Shout shout) {
		if (shout.isAsk()) {
			asks.remove(shout);
		} else {
			bids.remove(shout);
		}

		// logger.info("\n++ removed shout (" + shout.getId() + "/" +
		// shout.getPrice()
		// + ") ++\n");
		updateMatchedShouts();
	}

	@Override
	public void newShout(final Shout shout) throws DuplicateShoutException {
		if (shout.isBid()) {
			bids.add(shout);
		} else {
			asks.add(shout);
		}

		// logger.info("\n** new shout (" + shout.getId() + "/" + shout.getPrice()
		// + ") **\n");
		updateMatchedShouts();
	}

	protected void updateMatchedShouts() {
		sIn.clear();
		sOut.clear();
		bIn.clear();
		bOut.clear();

		final int matchingQuantity = getMatchingQuantity();

		// prettyPrint("bids", bids);
		// prettyPrint("asks", asks);
		//
		// logger.info("\nDistributing bids of " + matchingQuantity
		// + "\n---------------");
		distributeShoutsFromTail(matchingQuantity, bids, bIn, bOut);
		// logger.info("\nDistributing asks of " + matchingQuantity
		// + "\n---------------");
		distributeShoutsFromHead(matchingQuantity, asks, sIn, sOut);

		// logger.info("=====================================\n");
	}

	protected void distributeShoutsFromTail(final int quantity,
			final SortedTreeList<Shout> list, final SortedTreeList<Shout> in,
			final SortedTreeList<Shout> out) {
		distributeShouts(quantity, list, in, out, true);
	}

	protected void distributeShoutsFromHead(final int quantity,
			final SortedTreeList<Shout> list, final SortedTreeList<Shout> in,
			final SortedTreeList<Shout> out) {
		distributeShouts(quantity, list, in, out, false);
	}

	protected void distributeShouts(int quantity,
			final SortedTreeList<Shout> list, final SortedTreeList<Shout> in,
			final SortedTreeList<Shout> out, final boolean fromTail) {
		int i;
		if (fromTail) {
			i = list.size();
		} else {
			i = -1;
		}

		// logger.info(Utils.indent(" fill in"));

		Shout shout = null;
		while (quantity > 0) {
			if (fromTail) {
				i--;
			} else {
				i++;
			}

			shout = list.get(i);
			// logger.info(Utils.indent(i + ": " + shout));
			// logger.info(Utils.indent("quantity to fill: " + quantity));

			if (shout.getQuantity() <= quantity) {
				in.add(shout);
				quantity -= shout.getQuantity();
			} else {
				final Shout remainder = shout.split(shout.getQuantity() - quantity);
				if (fromTail) {
					list.add(i, remainder);
					i++;
				} else {
					list.add(i + 1, remainder);
				}
				in.add(shout);
				quantity = 0; // will break out
			}
		}

		// logger.info(Utils.indent(" fill out"));

		while (true) {
			if (fromTail) {
				if (i == 0) {
					break;
				} else {
					i--;
				}
			} else {
				if (i >= list.size() - 1) {
					break;
				} else {
					i++;
				}
			}

			shout = list.get(i);
			// logger.info(Utils.indent(i + ": " + shout));

			out.add(shout);
		}
	}

	protected int getMatchingQuantity() {
		int q = 0;
		int qmin = q;

		final Iterator<Shout> askItor = asks.iterator();
		final Iterator<Shout> bidItor = bids.iterator();

		Shout ask = null, bid = null;

		// 1. find lowest ask
		if (askItor.hasNext()) {
			ask = askItor.next();

			// 2. find lowest bid above the lowest ask
			// can be optimized to log(n)
			while (true) {
				if (bidItor.hasNext()) {
					bid = bidItor.next();
					if (bid.getPrice() >= ask.getPrice()) {
						break;
					}
				} else {
					bid = null;
					break;
				}
			}

			// 3. find the amount of demand and supply that can be matched
			int demand = 0;
			while (bid != null) {
				if ((ask != null) && (ask.getPrice() <= bid.getPrice())) {
					q += ask.getQuantity();
					ask = askItor.hasNext() ? askItor.next() : null;
				} else {
					q -= bid.getQuantity();
					if (q < qmin) {
						qmin = q;
					}
					demand += bid.getQuantity();
					bid = bidItor.hasNext() ? bidItor.next() : null;
				}
			}

			// get the quantity of goods that can be matched;
			// demand now is the demand above the price of the lowest ask; this should
			// be the initial value of q, now adjust it
			qmin += demand;
		}

		return qmin;
	}

	/**
	 * <p>
	 * Return a list of matched bids and asks. The list is of the form
	 * </p>
	 * <br>
	 * ( b0, a0, b1, a1 .. bn, an )<br>
	 * 
	 * <p>
	 * where bi is the ith bid and a0 is the ith ask. A typical auctioneer would
	 * clear by matching bi with ai for all i at some price.
	 * </p>
	 */
	@Override
	public List<Shout> getMatchedShouts() {
		final ArrayList<Shout> result = new ArrayList<Shout>(sIn.size()
				+ bIn.size());

		// prettyPrint("sIn: ", sIn);
		// prettyPrint("bIn: ", bIn);
		//
		// prettyPrint("asks", asks);
		// prettyPrint("bid", bids);

		Shout sInTop = null;
		Shout bInTop = null;

		final ListIterator<Shout> sItor = sIn.listIterator();
		final ListIterator<Shout> bItor = bIn.listIterator();

		while (true) {

			if (sInTop == null) {
				if (sItor.hasNext()) {
					sInTop = sItor.next();
					if (!asks.remove(sInTop)) {
						LazyMaxVolumeShoutEngine.logger
								.fatal("Failed to remove the matched ask !");
						LazyMaxVolumeShoutEngine.logger.fatal(sInTop);
					}
					// logger.info(sInTop);
				} else {
					break;
				}
			}

			if (bInTop == null) {
				if (bItor.hasNext()) {
					bInTop = bItor.next();
					if (!bids.remove(bInTop)) {
						LazyMaxVolumeShoutEngine.logger
								.fatal("Failed to remove the matched bid !");
						LazyMaxVolumeShoutEngine.logger.fatal(bInTop);
					}
					// logger.info(bInTop);
				} else {
					LazyMaxVolumeShoutEngine.logger.fatal("Unempty bInTop expected !");
				}
			}

			result.add(bInTop);
			result.add(sInTop);

			if (bInTop.getPrice() < sInTop.getPrice()) {
				LazyMaxVolumeShoutEngine.logger
						.fatal("Wrong match between ask and bid !");
				LazyMaxVolumeShoutEngine.logger.fatal(bInTop);
				LazyMaxVolumeShoutEngine.logger.fatal(sInTop);
			}

			final int nS = sInTop.getQuantity();
			final int nB = bInTop.getQuantity();
			if (nS < nB) {
				// split the bid
				bInTop = bInTop.split(nB - nS);
				sInTop = null;
			} else if (nB < nS) {
				// split the ask
				sInTop = sInTop.split(nS - nB);
				bInTop = null;
			} else {
				bInTop = null;
				sInTop = null;
			}
		}

		if (bItor.hasNext()) {
			LazyMaxVolumeShoutEngine.logger.fatal("Inconsistent state of bIn in "
					+ this + ". Empty bItor expected !");
		}

		sIn.clear();
		bIn.clear();

		// randomize the matching pairs
		Shout bid, ask;
		int index;
		for (int i = result.size() / 2 - 1; i > 0; i--) {
			// pick a pair to be at the i(th)
			// TODO: check whether this can lead to mutual access or not.
			index = uniform.nextIntFromTo(0, i);

			bid = result.get(i * 2);
			ask = result.get(i * 2 + 1);

			result.set(i * 2, result.get(index * 2));
			result.set(i * 2 + 1, result.get(index * 2 + 1));

			result.set(index * 2, bid);
			result.set(index * 2 + 1, ask);
		}

		return result;
	}

	@Override
	public Iterator<Shout> askIterator() {
		return new CollatingIterator<Shout>(ShoutEngine.AscendingOrder, sIn
				.iterator(), sOut.iterator());
	}

	@Override
	public Iterator<Shout> bidIterator() {
		return new CollatingIterator<Shout>(ShoutEngine.DescendingOrder, bIn
				.iterator(), bOut.iterator());
	}

	@Override
	public Shout getLowestUnmatchedAsk() {
		if (sOut.isEmpty()) {
			return null;
		} else {
			return sOut.get(0);
		}
	}

	@Override
	public Shout getHighestMatchedAsk() {
		if (sIn.isEmpty()) {
			return null;
		} else {
			return sIn.get(sIn.size() - 1);
		}
	}

	@Override
	public Shout getHighestUnmatchedBid() {
		if (bOut.isEmpty()) {
			return null;
		} else {
			return bOut.get(bOut.size() - 1);
		}
	}

	@Override
	public Shout getLowestMatchedBid() {
		if (bIn.isEmpty()) {
			return null;
		} else {
			return bIn.get(0);
		}
	}

	/**
	 * Log the current status of the auction.
	 */
	@Override
	public void printState() {
		LazyMaxVolumeShoutEngine.logger.info("Auction status:\n");
		prettyPrint("Matched bids", bIn);
		prettyPrint("Matched asks", sIn);
		prettyPrint("Runner-up bids", bOut);
		prettyPrint("Runner-up asks", sOut);
	}

	public void prettyPrint(final String title, final List<Shout> shouts) {
		LazyMaxVolumeShoutEngine.logger.info(title);
		LazyMaxVolumeShoutEngine.logger.info("--------------");
		final Iterator<Shout> i = shouts.iterator();
		while (i.hasNext()) {
			final Shout shout = i.next();
			LazyMaxVolumeShoutEngine.logger.info(shout.toPrettyString());
		}
		LazyMaxVolumeShoutEngine.logger.info("");
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
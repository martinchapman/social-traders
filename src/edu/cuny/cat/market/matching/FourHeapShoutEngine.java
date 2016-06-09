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

package edu.cuny.cat.market.matching;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections15.buffer.PriorityBuffer;
import org.apache.commons.collections15.iterators.CollatingIterator;
import org.apache.log4j.Logger;

import edu.cuny.cat.core.AuctionError;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.DuplicateShoutException;

/**
 * <p>
 * This class provides auction shout management services using the 4-Heap
 * algorithm. See:
 * </p>
 * 
 * <p>
 * "Flexible Double Auctions for Electronic Commerce: Theory and Implementation"
 * by Wurman, Walsh and Wellman 1998.
 * </p>
 * 
 * <p>
 * All status is maintained in memory resident data structures and no crash
 * recovery is provided.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.16 $
 */

public class FourHeapShoutEngine extends ShoutEngine implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Matched bids in ascending order
	 */
	protected PriorityBuffer<Shout> bIn = null;

	/**
	 * Unmatched bids in descending order
	 */
	protected PriorityBuffer<Shout> bOut = null;

	/**
	 * Matched asks in descending order
	 */
	protected PriorityBuffer<Shout> sIn = null;

	/**
	 * Unmatched asks in ascending order
	 */
	protected PriorityBuffer<Shout> sOut = null;

	static Logger logger = Logger.getLogger(FourHeapShoutEngine.class);

	public FourHeapShoutEngine() {
		bIn = new PriorityBuffer<Shout>(ShoutEngine.AscendingOrder);
		bOut = new PriorityBuffer<Shout>(ShoutEngine.DescendingOrder);
		sIn = new PriorityBuffer<Shout>(ShoutEngine.DescendingOrder);
		sOut = new PriorityBuffer<Shout>(ShoutEngine.AscendingOrder);
	}

	@Override
	public synchronized void reset() {
		super.reset();

		bIn.clear();
		bOut.clear();
		sIn.clear();
		sOut.clear();
	}

	@Override
	public synchronized void removeShout(final Shout shout) {
		preRemovalProcessing();
		if (shout.isAsk()) {
			removeAsk(shout);
		} else {
			removeBid(shout);
		}
		postRemovalProcessing();
	}

	protected void removeAsk(final Shout shout) {
		if (sIn.remove(shout)) {
			reinsert(bIn, shout.getQuantity());
		} else {
			sOut.remove(shout);
		}
	}

	protected void removeBid(final Shout shout) {
		if (bIn.remove(shout)) {
			reinsert(sIn, shout.getQuantity());
		} else {
			bOut.remove(shout);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * Log the current status of the auction.
	 */
	@Override
	public void printState() {
		FourHeapShoutEngine.logger.info("Auction status:\n");
		prettyPrint("Matched bids", bIn);
		prettyPrint("Matched asks", sIn);
		prettyPrint("Runner-up bids", bOut);
		prettyPrint("Runner-up asks", sOut);
	}

	public void prettyPrint(final String title, final PriorityBuffer<Shout> shouts) {
		FourHeapShoutEngine.logger.info(title);
		FourHeapShoutEngine.logger.info("--------------");
		final Iterator<Shout> i = shouts.iterator();
		while (i.hasNext()) {
			final Shout shout = i.next();
			FourHeapShoutEngine.logger.info(shout.toPrettyString());
		}
		FourHeapShoutEngine.logger.info("");
	}

	/**
	 * Insert a shout into a binary heap.
	 * 
	 * @param heap
	 *          The heap to insert into
	 * @param shout
	 *          The shout to insert
	 * 
	 */
	private static void insertShout(final PriorityBuffer<Shout> heap,
			final Shout shout) throws DuplicateShoutException {
		try {
			heap.add(shout);
		} catch (final IllegalArgumentException e) {
			FourHeapShoutEngine.logger.error(e.toString());
			e.printStackTrace();
			throw new DuplicateShoutException("Duplicate shout: " + shout.toString());
		}
	}

	/**
	 * Insert an unmatched ask into the approriate heap.
	 */
	public void insertUnmatchedAsk(final Shout ask)
			throws DuplicateShoutException {
		FourHeapShoutEngine.insertShout(sOut, ask);
	}

	/**
	 * Insert an unmatched bid into the approriate heap.
	 */
	public void insertUnmatchedBid(final Shout bid)
			throws DuplicateShoutException {
		FourHeapShoutEngine.insertShout(bOut, bid);
	}

	/**
	 * Get the highest unmatched bid.
	 */
	@Override
	public Shout getHighestUnmatchedBid() {
		if (bOut.isEmpty()) {
			return null;
		}
		return bOut.get();
	}

	/**
	 * Get the lowest matched bid
	 */
	@Override
	public Shout getLowestMatchedBid() {
		if (bIn.isEmpty()) {
			return null;
		}
		return bIn.get();
	}

	/**
	 * Get the lowest unmatched ask.
	 */
	@Override
	public Shout getLowestUnmatchedAsk() {
		if (sOut.isEmpty()) {
			return null;
		}
		return sOut.get();
	}

	/**
	 * Get the highest matched ask.
	 */
	@Override
	public Shout getHighestMatchedAsk() {
		if (sIn.isEmpty()) {
			return null;
		}
		return sIn.get();
	}

	/**
	 * Unify the shout at the top of the heap with the supplied shout, so that
	 * quantity(shout) = quantity(top(heap)). This is achieved by splitting the
	 * supplied shout or the shout at the top of the heap.
	 * 
	 * @param shout
	 *          The shout.
	 * @param heap
	 *          The heap.
	 * 
	 * @return A reference to the, possibly modified, shout.
	 * 
	 */
	protected static Shout unifyShout(Shout shout,
			final PriorityBuffer<Shout> heap) {

		final Shout top = heap.get();

		if (shout.getQuantity() > top.getQuantity()) {
			shout = shout.splat(shout.getQuantity() - top.getQuantity());
		} else {
			if (top.getQuantity() > shout.getQuantity()) {
				final Shout remainder = top.split(top.getQuantity()
						- shout.getQuantity());
				heap.add(remainder);
			}
		}

		return shout;
	}

	protected int displaceShout(Shout shout, final PriorityBuffer<Shout> from,
			final PriorityBuffer<Shout> to) throws DuplicateShoutException {
		shout = FourHeapShoutEngine.unifyShout(shout, from);
		to.add(from.remove());
		FourHeapShoutEngine.insertShout(from, shout);
		return shout.getQuantity();
	}

	public int promoteShout(Shout shout, final PriorityBuffer<Shout> from,
			final PriorityBuffer<Shout> to, final PriorityBuffer<Shout> matched)
			throws DuplicateShoutException {

		shout = FourHeapShoutEngine.unifyShout(shout, from);
		FourHeapShoutEngine.insertShout(matched, shout);
		to.add(from.remove());
		return shout.getQuantity();
	}

	public int displaceHighestMatchedAsk(final Shout ask)
			throws DuplicateShoutException {
		return displaceShout(ask, sIn, sOut);
	}

	public int displaceLowestMatchedBid(final Shout bid)
			throws DuplicateShoutException {
		return displaceShout(bid, bIn, bOut);
	}

	public int promoteHighestUnmatchedBid(final Shout ask)
			throws DuplicateShoutException {
		return promoteShout(ask, bOut, bIn, sIn);
	}

	public int promoteLowestUnmatchedAsk(final Shout bid)
			throws DuplicateShoutException {
		return promoteShout(bid, sOut, sIn, bIn);
	}

	@Override
	public void newShout(final Shout shout) throws DuplicateShoutException {
		if (shout.isBid()) {
			newBid(shout);
		} else {
			newAsk(shout);
		}
	}

	protected void newBid(final Shout bid) throws DuplicateShoutException {

		final double bidVal = bid.getPrice();

		int uninsertedUnits = bid.getQuantity();

		while (uninsertedUnits > 0) {

			final Shout sOutTop = getLowestUnmatchedAsk();
			final Shout bInTop = getLowestMatchedBid();

			if ((sOutTop != null) && (bidVal >= sOutTop.getPrice())
					&& ((bInTop == null) || (bInTop.getPrice() >= sOutTop.getPrice()))) {

				// found match
				uninsertedUnits -= promoteLowestUnmatchedAsk(bid);

			} else if ((bInTop != null) && (bidVal > bInTop.getPrice())) {

				uninsertedUnits -= displaceLowestMatchedBid(bid);

			} else {
				insertUnmatchedBid(bid);
				uninsertedUnits -= bid.getQuantity();
			}

		}
	}

	protected void newAsk(final Shout ask) throws DuplicateShoutException {

		final double askVal = ask.getPrice();

		int uninsertedUnits = ask.getQuantity();

		while (uninsertedUnits > 0) {

			final Shout sInTop = getHighestMatchedAsk();
			final Shout bOutTop = getHighestUnmatchedBid();

			if ((bOutTop != null) && (askVal <= bOutTop.getPrice())
					&& ((sInTop == null) || (sInTop.getPrice() <= bOutTop.getPrice()))) {

				uninsertedUnits -= promoteHighestUnmatchedBid(ask);

			} else if ((sInTop != null) && (askVal <= sInTop.getPrice())) {

				uninsertedUnits -= displaceHighestMatchedAsk(ask);

			} else {

				insertUnmatchedAsk(ask);
				uninsertedUnits -= ask.getQuantity();

			}
		}
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
		while (!sIn.isEmpty()) {
			final Shout sInTop = sIn.remove();
			final Shout bInTop = bIn.remove();
			final int nS = sInTop.getQuantity();
			final int nB = bInTop.getQuantity();
			if (nS < nB) {
				// split the bid
				final Shout remainder = bInTop.split(nB - nS);
				bIn.add(remainder);
			} else if (nB < nS) {
				// split the ask
				final Shout remainder = sInTop.split(nS - nB);
				sIn.add(remainder);
			}
			result.add(bInTop);
			result.add(sInTop);
		}
		return result;
	}

	/**
	 * Sub-classes should override this method if they wish to check auction
	 * status integrity before shout removal. This is useful for
	 * testing/debugging.
	 */
	protected void preRemovalProcessing() {
		// Do nothing
	}

	/**
	 * Sub-classes should override this method if they wish to check auction
	 * status integrity after shout removal. This is useful for testing/debugging.
	 */
	protected void postRemovalProcessing() {
		// Do nothing
	}

	/**
	 * Remove, possibly several, shouts from heap such that quantity(heap) is
	 * reduced by the supplied quantity and reinsert the shouts using the standard
	 * insertion logic. quantity(heap) is defined as the total quantity of every
	 * shout in the heap.
	 * 
	 * @param heap
	 *          The heap to remove shouts from.
	 * @param quantity
	 *          The total quantity to remove.
	 */
	protected void reinsert(final PriorityBuffer<Shout> heap, int quantity) {

		while (quantity > 0) {

			final Shout top = heap.remove();

			if (top.getQuantity() > quantity) {
				heap.add(top.split(top.getQuantity() - quantity));
			}

			quantity -= top.getQuantity();

			try {
				if (top.isBid()) {
					newBid(top);
				} else {
					newAsk(top);
				}
			} catch (final DuplicateShoutException e) {
				throw new AuctionError("Invalid auction status");
			}
		}
	}
}

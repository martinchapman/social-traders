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

package edu.cuny.cat.market;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections15.buffer.PriorityBuffer;

import edu.cuny.cat.MyTestCase;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.matching.FourHeapShoutEngine;

/**
 * @author Steve Phelps
 * @version $Revision: 1.6 $
 */

public class FourHeapTest extends MyTestCase {

	TestShoutEngine shoutEngine;

	Random randGenerator;

	public FourHeapTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		shoutEngine = new TestShoutEngine();
		randGenerator = new Random();
	}

	public Shout randomShout() {
		final int quantity = randGenerator.nextInt(50);
		final double price = randGenerator.nextDouble() * 100;
		final boolean isBid = randGenerator.nextBoolean();
		return new Shout(quantity, price, isBid);
	}

	public void testRandom() {
		System.out.println("\n>>>>>>>>>\t " + "testRandom() \n");

		int matches = 0;

		try {

			Shout testRemoveShout = null, testRemoveShout2 = null;

			for (int round = 0; round < 700; round++) {

				if (testRemoveShout != null) {
					shoutEngine.removeShout(testRemoveShout);
					shoutEngine.removeShout(testRemoveShout2);
				}

				for (int shout = 0; shout < 200; shout++) {
					shoutEngine.newShout(randomShout());
				}

				shoutEngine.newShout(testRemoveShout = randomShout());
				testRemoveShout2 = (Shout) testRemoveShout.clone();
				testRemoveShout2 = new Shout(testRemoveShout.getQuantity(),
						testRemoveShout.getPrice(), !testRemoveShout.isBid());
				shoutEngine.newShout(testRemoveShout2);

				if ((round & 0x01) > 0) {
					continue;
				}

				final List<Shout> matched = shoutEngine.getMatchedShouts();
				final Iterator<Shout> i = matched.iterator();
				while (i.hasNext()) {
					matches++;
					final Shout bid = i.next();
					final Shout ask = i.next();
					Assert.assertTrue(bid.isBid());
					Assert.assertTrue(ask.isAsk());
					Assert.assertTrue(bid.getPrice() >= ask.getPrice());
					// System.out.print(bid + "/" + ask + " ");
				}
				// System.out.println("");
			}

		} catch (final Exception e) {
			shoutEngine.printState();
			e.printStackTrace();
			Assert.fail();
		}

		System.out.println("Matches = " + matches);

	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(FourHeapTest.suite());
	}

	public static Test suite() {
		return new TestSuite(FourHeapTest.class);
	}

}

class TestShoutEngine extends FourHeapShoutEngine {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void preRemovalProcessing() {
		checkBalanced();
	}

	@Override
	protected void postRemovalProcessing() {
		checkBalanced();
	}

	protected void checkBalanced() {

		final int nS = TestShoutEngine.countQty(sIn);
		final int nB = TestShoutEngine.countQty(bIn);
		if (nS != nB) {
			printState();
			throw new Error("shout heaps not balanced nS=" + nS + " nB=" + nB);
		}

		final Shout bInTop = getLowestMatchedBid();
		final Shout sInTop = getHighestMatchedAsk();
		final Shout bOutTop = getHighestUnmatchedBid();
		final Shout sOutTop = getLowestUnmatchedAsk();

		checkBalanced(bInTop, bOutTop, "bIn >= bOut");
		checkBalanced(sOutTop, sInTop, "sOut >= sIn");
		checkBalanced(sOutTop, bOutTop, "sOut >= bOut");
		checkBalanced(bInTop, sInTop, "bIn >= sIn");
	}

	protected void checkBalanced(final Shout s1, final Shout s2,
			final String condition) {
		if (!(((s1 == null) || (s2 == null)) || (s1.getPrice() >= s2.getPrice()))) {
			printState();
			System.out.println("shout1 = " + s1);
			System.out.println("shout2 = " + s2);
			throw new Error("Heaps not balanced! - " + condition);
		}
	}

	public static int countQty(final PriorityBuffer<Shout> heap) {
		final Iterator<Shout> i = heap.iterator();
		int qty = 0;
		while (i.hasNext()) {
			final Shout s = i.next();
			qty += s.getQuantity();
		}
		return qty;
	}

	@Override
	public void newShout(final Shout shout) throws DuplicateShoutException {
		if (shout.isAsk()) {
			newAsk(shout);
		} else {
			newBid(shout);
		}
	}
}
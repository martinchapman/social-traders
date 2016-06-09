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

package edu.cuny.cat.market;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cuny.cat.MyTestCase;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.market.matching.LazyMaxVolumeShoutEngine;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class LazyMaxVolumeShoutEngineTest extends MyTestCase {

	LazyMaxVolumeShoutEngine shoutEngine;

	Random randGenerator;

	public LazyMaxVolumeShoutEngineTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		shoutEngine = new LazyMaxVolumeShoutEngine();
		randGenerator = new Random();
	}

	public Shout randomShout() {
		final int quantity = randGenerator.nextInt(50);
		final double price = randGenerator.nextDouble() * 100;
		final boolean isBid = randGenerator.nextBoolean();
		return new Shout(quantity, price, isBid);
	}

	/**
	 * TODO: occasionally, there is something wrong found in this test.
	 */
	public void testRandom() {
		System.out.println("\n>>>>>>>>>\t " + "testRandom() \n");

		int matches = 0;

		try {

			Shout testRemoveShout = null, testRemoveShout2 = null;

			for (int round = 0; round < 2; round++) {

				if (testRemoveShout != null) {
					shoutEngine.removeShout(testRemoveShout);
					shoutEngine.removeShout(testRemoveShout2);
				}

				for (int shout = 0; shout < 2; shout++) {
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
		junit.textui.TestRunner.run(LazyMaxVolumeShoutEngineTest.suite());
	}

	public static Test suite() {
		return new TestSuite(LazyMaxVolumeShoutEngineTest.class);
	}
}
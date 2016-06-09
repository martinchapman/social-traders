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

package edu.cuny.cat.stat;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;
import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.11 $
 */

public class HistoricalReportTest extends MyTestCase {

	static Logger logger = Logger.getLogger(HistoricalReportTest.class);

	HistoricalReport report;

	double askPrices[] = { 30, 40, 50, 60, 70 };

	double bidPrices[] = { 5, 15, 25, 35 };

	public HistoricalReportTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		report = new HistoricalReport();
		report.initialize();
	}

	@Override
	public void tearDown() {
		report.reset();
		report = null;
	}

	protected void placeShouts() {
		Shout shout = null;
		for (int i = 0; i < Math.max(askPrices.length, bidPrices.length); i++) {
			if (i < askPrices.length) {
				shout = new Shout(1, askPrices[i], false);
				shout.setId("ask" + i);
				shout.setState(Shout.PLACED);
				report.eventOccurred(new ShoutPostedEvent(shout));
			}

			if (i < bidPrices.length) {
				shout = new Shout(1, bidPrices[i], true);
				shout.setId("bid" + i);
				shout.setState(Shout.PLACED);
				report.eventOccurred(new ShoutPostedEvent(shout));
			}
		}
	}

	protected void matchShouts() {
		final Shout ask = report.lowestUnmatchedAsk;
		final Shout bid = report.highestUnmatchedBid;

		Assert.assertEquals("lowest unmatched ask", 30, (int) ask.getPrice());
		Assert.assertEquals("highest unmatched bid", 35, (int) bid.getPrice());

		ask.setState(Shout.MATCHED);
		bid.setState(Shout.MATCHED);
		final Transaction transaction = new Transaction(ask, bid, ask.getPrice());
		report.eventOccurred(new TransactionPostedEvent(transaction));
	}

	public void testPlacingShouts() {
		System.out.println("\n>>>>>>>>>\t " + "testPlacingShouts() \n");

		placeShouts();

		Assert.assertEquals("Wrong number of asks in report !", askPrices.length,
				report.getAsks().size());

		HistoricalReportTest.logger.info("through accelerator: \n"
				+ report.getIncreasingQueryAccelerator());

		HistoricalReportTest.logger.info("sorted asks: \n"
				+ report.getIncreasingQueryAccelerator().getSortedView()
						.getSortedAsks());
		HistoricalReportTest.logger.info("sorted bids: \n"
				+ report.getIncreasingQueryAccelerator().getSortedView()
						.getSortedBids());

		final boolean matchedOnly = true;
		Assert.assertEquals("", 2, report.getNumberOfAsks(-45, !matchedOnly));
		Assert.assertEquals("", 2, report.getIncreasingQueryAccelerator()
				.getNumOfAsksBelow(45));
		Assert.assertEquals("", 5, report.getNumberOfAsks(-75, !matchedOnly));
		Assert.assertEquals("", 5, report.getIncreasingQueryAccelerator()
				.getNumOfAsksBelow(75));
		Assert.assertEquals("", 0, report.getNumberOfAsks(75, !matchedOnly));
		Assert.assertEquals("", 3, report.getNumberOfAsks(45, !matchedOnly));

		Assert.assertEquals("", 0, report.getNumberOfAsks(-45, matchedOnly));
		Assert.assertEquals("", 0, report.getNumberOfAsks(-75, matchedOnly));
		Assert.assertEquals("", 0, report.getNumberOfAsks(75, matchedOnly));
		Assert.assertEquals("", 0, report.getIncreasingQueryAccelerator()
				.getNumOfAcceptedAsksAbove(75));
		Assert.assertEquals("", 0, report.getNumberOfAsks(45, matchedOnly));
		Assert.assertEquals("", 0, report.getIncreasingQueryAccelerator()
				.getNumOfAcceptedAsksAbove(45));

		Assert.assertEquals("Wrong number of bids in report !", bidPrices.length,
				report.getBids().size());

		Assert.assertEquals("", 1, report.getNumberOfBids(-10, !matchedOnly));
		Assert.assertEquals("", 2, report.getNumberOfBids(-20, !matchedOnly));
		Assert.assertEquals("", 2, report.getNumberOfBids(20, !matchedOnly));
		Assert.assertEquals("", 2, report.getIncreasingQueryAccelerator()
				.getNumOfBidsAbove(20));
		Assert.assertEquals("", 3, report.getNumberOfBids(10, !matchedOnly));
		Assert.assertEquals("", 3, report.getIncreasingQueryAccelerator()
				.getNumOfBidsAbove(10));

		Assert.assertEquals("", 0, report.getNumberOfBids(-10, matchedOnly));
		Assert.assertEquals("", 0, report.getIncreasingQueryAccelerator()
				.getNumOfAcceptedBidsBelow(10));
		Assert.assertEquals("", 0, report.getNumberOfBids(-20, matchedOnly));
		Assert.assertEquals("", 0, report.getIncreasingQueryAccelerator()
				.getNumOfAcceptedBidsBelow(20));
		Assert.assertEquals("", 0, report.getNumberOfBids(20, matchedOnly));
		Assert.assertEquals("", 0, report.getNumberOfBids(10, matchedOnly));
	}

	public void testAcceptingShouts() {
		System.out.println("\n>>>>>>>>>\t " + "testAcceptingShouts() \n");

		placeShouts();
		matchShouts();

		HistoricalReportTest.logger.info("asks:");
		Iterator<Shout> itor = report.getAsks().iterator();
		while (itor.hasNext()) {
			System.out.println(itor.next());
		}

		HistoricalReportTest.logger.info("bids:");
		itor = report.getBids().iterator();
		while (itor.hasNext()) {
			System.out.println(itor.next());
		}

		HistoricalReportTest.logger.info("matched shouts:");
		itor = report.matchedShouts.iterator();
		while (itor.hasNext()) {
			System.out.println(itor.next());
		}

		final boolean matchedOnly = true;

		// asks
		Assert.assertEquals("", 2, report.getNumberOfAsks(-45, !matchedOnly));
		Assert.assertEquals("", 5, report.getNumberOfAsks(-75, !matchedOnly));
		Assert.assertEquals("", 0, report.getNumberOfAsks(75, !matchedOnly));
		Assert.assertEquals("", 3, report.getNumberOfAsks(45, !matchedOnly));

		Assert.assertEquals("", 1, report.getNumberOfAsks(-45, matchedOnly));
		Assert.assertEquals("", 1, report.getNumberOfAsks(-75, matchedOnly));
		Assert.assertEquals("", 0, report.getNumberOfAsks(75, matchedOnly));
		Assert.assertEquals("", 0, report.getNumberOfAsks(45, matchedOnly));

		// bids
		Assert.assertEquals("", 1, report.getNumberOfBids(-10, !matchedOnly));
		Assert.assertEquals("", 2, report.getNumberOfBids(-20, !matchedOnly));
		Assert.assertEquals("", 2, report.getNumberOfBids(20, !matchedOnly));
		Assert.assertEquals("", 3, report.getNumberOfBids(10, !matchedOnly));

		Assert.assertEquals("", 0, report.getNumberOfBids(-10, matchedOnly));
		Assert.assertEquals("", 0, report.getNumberOfBids(-20, matchedOnly));
		Assert.assertEquals("", 1, report.getNumberOfBids(20, matchedOnly));
		Assert.assertEquals("", 1, report.getNumberOfBids(10, matchedOnly));

	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(HistoricalReportTest.suite());
	}

	public static Test suite() {
		return new TestSuite(HistoricalReportTest.class);
	}
}
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

package edu.cuny.cat.trader.marketselection;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;
import edu.cuny.cat.core.Specialist;

/**
 * TODO:
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class StimuliResponseMarketSelectionTest extends MyTestCase {

	static Logger logger = Logger
			.getLogger(StimuliResponseMarketSelectionTest.class);

	protected static String specialistIds[] = { "jan", "feb", "mar", "apr",
			"may", "jue", "jul", "aug", "sep", "oct", "nov", "dec" };

	protected Map<String, Specialist> specialists;

	protected Specialist specialistArray[];

	public StimuliResponseMarketSelectionTest(final String name) {
		super(name);

		specialists = new TreeMap<String, Specialist>();
		specialistArray = new Specialist[StimuliResponseMarketSelectionTest.specialistIds.length];

		Specialist specialist = null;
		for (int i = 0; i < StimuliResponseMarketSelectionTest.specialistIds.length; i++) {
			specialist = new Specialist(
					StimuliResponseMarketSelectionTest.specialistIds[i]);
			specialists.put(StimuliResponseMarketSelectionTest.specialistIds[i],
					specialist);
			specialistArray[i] = specialist;
		}
	}

	protected void setupMarkets(final AbstractMarketSelectionStrategy strategy) {
		strategy.setupMarkets(specialists.values());
	}

	protected void setupActiveMarkets(
			final AbstractMarketSelectionStrategy strategy) {
		strategy.activeMarkets.clear();

		for (final Specialist element : specialistArray) {
			strategy.addActiveSpecialist(element);
		}
	}

	public void testStaticMarketSelection() {
		System.out.println("\n>>>>>>>>>\t " + "testStaticMarketSelection() \n");

		for (final String specialistId : StimuliResponseMarketSelectionTest.specialistIds) {

			final StaticMarketSelectionStrategy strategy = new StaticMarketSelectionStrategy();
			strategy.setSpecialistId(specialistId);
			strategy.initialize();

			setupMarkets(strategy);

			setupActiveMarkets(strategy);

			strategy.selectMarket();

			Assert.assertTrue(strategy.getSpecialistId().equals(specialistId));
			Assert.assertTrue(strategy.getCurrenMarket().getId().equals(
					strategy.getSpecialistId()));
		}
	}

	public void testRandomMarketSelection() {
		System.out.println("\n>>>>>>>>>\t " + "testRandomMarketSelection() \n");

		final RandomMarketSelectionStrategy strategy = new RandomMarketSelectionStrategy();
		strategy.initialize();

		setupMarkets(strategy);

		final int frequencies[] = new int[StimuliResponseMarketSelectionTest.specialistIds.length];
		final int times = 500000;

		for (int i = 0; i < times; i++) {
			setupActiveMarkets(strategy);
			strategy.selectMarket();

			Assert
					.assertTrue(strategy.currentMarketIndex < StimuliResponseMarketSelectionTest.specialistIds.length);
			Assert.assertTrue(strategy.currentMarketIndex >= 0);

			frequencies[strategy.currentMarketIndex]++;
		}

		// check if every action is chosen with an approximately same frequency.
		final double avg = (double) times / frequencies.length;
		for (final int frequencie : frequencies) {
			Assert.assertEquals(
					"Each market should have been chosen approximately equally often !",
					1, frequencie / avg, 0.01);
		}
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(StimuliResponseMarketSelectionTest.suite());
	}

	public static Test suite() {
		return new TestSuite(StimuliResponseMarketSelectionTest.class);
	}
}
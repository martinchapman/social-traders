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

package edu.cuny.cat;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.cuny.ai.learning.EpsilonGreedyLearnerTest;
import edu.cuny.ai.learning.NArmedBanditLearnerTest;
import edu.cuny.ai.learning.QLearnerTest;
import edu.cuny.ai.learning.ReverseDiscountSlidingLearnerTest;
import edu.cuny.ai.learning.RothErevLearnerTest;
import edu.cuny.ai.learning.SoftmaxLearnerTest;
import edu.cuny.ai.learning.WidrowHoffLearnerTest;
import edu.cuny.cat.comm.CatpInfrastructureTest;
import edu.cuny.cat.comm.CatpMessageTest;
import edu.cuny.cat.market.FourHeapTest;
import edu.cuny.cat.market.LazyMaxVolumeShoutEngineTest;
import edu.cuny.cat.stat.HistoricalReportTest;
import edu.cuny.cat.sys.BenchmarkTest;
import edu.cuny.cat.trader.marketselection.MarketSelectionTest;
import edu.cuny.cat.valuation.DailyRandomValuerTest;
import edu.cuny.cat.valuation.FixedValuerTest;
import edu.cuny.cat.valuation.IntervalValuerTest;
import edu.cuny.cat.valuation.RandomValuerTest;
import edu.cuny.prng.GlobalPRNG;
import edu.cuny.util.Galaxy;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.PriorityQueueTest;
import edu.cuny.util.SortedTreeListTest;
import edu.cuny.util.UtilsTest;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.21 $
 */

public class AllTests {

	public static void main(final String[] args) {

		// org.apache.log4j.BasicConfigurator.configure();

		final URL url = ParameterDatabase.getURL(Game.getDefaultParameterFile());
		org.apache.log4j.PropertyConfigurator.configure(url);

		// when textui.TestRunner is used, log4j works fine without GUI;
		// when swingui.TestRunner is used, log4j fails to properly initialize.

		Game.setupObjectRegistry();
		Galaxy.getInstance().getTyped(Game.P_CAT, GlobalPRNG.class)
				.setUseMultiEngine(false);

		junit.textui.TestRunner.run(AllTests.suite());
		// junit.swingui.TestRunner.run(AllTests.class);

		Game.cleanupObjectRegistry();
	}

	public static Test suite() {

		final TestSuite suite = new TestSuite("jcat test suite");

		// ai.learning
		suite.addTest(EpsilonGreedyLearnerTest.suite());
		suite.addTest(SoftmaxLearnerTest.suite());
		suite.addTest(NArmedBanditLearnerTest.suite());
		suite.addTest(QLearnerTest.suite());
		suite.addTest(ReverseDiscountSlidingLearnerTest.suite());
		suite.addTest(RothErevLearnerTest.suite());
		suite.addTest(WidrowHoffLearnerTest.suite());

		// market
		suite.addTest(FourHeapTest.suite());
		suite.addTest(LazyMaxVolumeShoutEngineTest.suite());

		// trader
		suite.addTest(MarketSelectionTest.suite());

		// stat
		suite.addTest(HistoricalReportTest.suite());

		// comm
		suite.addTest(CatpMessageTest.suite());
		suite.addTest(CatpInfrastructureTest.suite());

		// valuation
		suite.addTest(FixedValuerTest.suite());
		suite.addTest(RandomValuerTest.suite());
		suite.addTest(DailyRandomValuerTest.suite());
		suite.addTest(IntervalValuerTest.suite());

		// util
		suite.addTest(SortedTreeListTest.suite());
		suite.addTest(PriorityQueueTest.suite());
		suite.addTest(UtilsTest.suite());

		// system
		suite.addTest(BenchmarkTest.suite());

		return suite;
	}

}

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

package edu.cuny.cat.sys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.cuny.cat.Game;
import edu.cuny.cat.GameServer;
import edu.cuny.cat.MarketClient;
import edu.cuny.cat.MyTestCase;
import edu.cuny.cat.TraderClient;
import edu.cuny.cat.stat.GameReport;
import edu.cuny.cat.stat.StatisticalReport;
import edu.cuny.util.CumulativeDistribution;
import edu.cuny.util.MathUtil;
import edu.cuny.util.ParameterDatabase;

/**
 * This aims to check the performance of some classic markets.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class BenchmarkTest extends MyTestCase {

	protected static Logger logger = Logger.getLogger(BenchmarkTest.class);

	String path = null;

	public BenchmarkTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		path = "params/modules";
	}

	protected StatisticalReport runGame(String configFiles[],
			String extraConfigFiles[]) {

		final ParameterDatabase root = new ParameterDatabase();
		ParameterDatabase pdb = null;
		try {
			for (int i = 0; i < configFiles.length; i++) {
				pdb = new ParameterDatabase(new File(path, configFiles[i]));
				root.addParent(pdb);
			}
			for (int i = 0; i < extraConfigFiles.length; i++) {
				pdb = new ParameterDatabase(new File(path, extraConfigFiles[i]));
				root.addParent(pdb);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}

		try {

			final Level level = LogManager.getRootLogger().getLevel();
			LogManager.getRootLogger().setLevel(Level.WARN);

			StatisticalReport statReport = null;

			Game.setupPreferences(root);

			final Collection<? extends TraderClient> traders = Game.createTraders();

			final Collection<? extends MarketClient> markets = Game.createMarkets();

			GameServer server = Game.createServer();

			statReport = server.getController().getReport(StatisticalReport.class);

			Game.startTraders(traders);

			Game.startMarkets(markets);

			Game.cleanUpAfterInitialization();

			Game.startServer(server);

			traders.clear();
			markets.clear();
			server = null;

			System.gc();

			LogManager.getRootLogger().setLevel(level);

			return statReport;

		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void checkEfficiency(String marketId, String configFiles[],
			String extraConfigFiles[], double meanLowerBound, double meanUpperBound,
			double stdevLowerBound, double stdevUpperBound) {
		checkEfficiency(marketId, configFiles, extraConfigFiles, meanLowerBound,
				meanUpperBound, MathUtil.DEFAULT_ERROR, stdevLowerBound,
				stdevUpperBound, MathUtil.DEFAULT_ERROR);
	}

	protected void checkEfficiency(String marketId, String configFiles[],
			String extraConfigFiles[], double meanLowerBound, double meanUpperBound,
			double errorOfMean, double stdevLowerBound, double stdevUpperBound,
			double errorOfStdev) {
		final StatisticalReport statReport = runGame(configFiles, extraConfigFiles);

		if (statReport == null) {
			Assert.assertTrue(false);
		} else {
			CumulativeDistribution dist = null;
			dist = statReport.getDistribution(marketId + "." + GameReport.EFFICIENCY);
			if (dist == null) {
				Assert.assertTrue(false);
			} else {
				BenchmarkTest.logger.info(dist);
				Assert.assertTrue(MathUtil.approxBigger(dist.getMean(), meanLowerBound,
						errorOfMean));
				Assert.assertTrue(MathUtil.approxSmaller(dist.getMean(),
						meanUpperBound, errorOfMean));
				Assert.assertTrue(MathUtil.approxBigger(dist.getStdDev(),
						stdevLowerBound, errorOfStdev));
				Assert.assertTrue(MathUtil.approxSmaller(dist.getStdDev(),
						stdevUpperBound, errorOfStdev));
			}
		}
	}

	public void testEfficiencyOfCH() {
		System.out.println("\n>>>>>>>>>\t " + "testEfficiencyOfCH() \n");

		final String configFiles[] = { "general.params",
				"infrastructure-call.params", "clock-noquitting.params",
				"noconsole.params", "reports-nocsv.params", "specialists-ch.params" };
		String extraConfigFiles[] = null;
		final String marketId = "CH";

		BenchmarkTest.logger.info("\nchecking TT in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-tt.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 100.0, 100.0, 0.0,
				0.0);

		BenchmarkTest.logger.info("\nchecking GD in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-gd.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 99.5, 100.0, 0.0,
				1.0);

		BenchmarkTest.logger.info("\nchecking PS in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-ps.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 98.5, 100, 0.0,
				0.0);

		BenchmarkTest.logger.info("\nchecking ZIP in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-zip.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 98.5, 100.0, 0.0,
				2.0);

		BenchmarkTest.logger.info("\nchecking ZIC in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-zic.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 97.5, 100.0, 0.0,
				2.0);

		BenchmarkTest.logger.info("\nchecking RE in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-re.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 95.0, 100, 0.0,
				8.0);

	}

	public void testEfficiencyOfCDA() {
		System.out.println("\n>>>>>>>>>\t " + "testEfficiencyOfCDA() \n");

		final String configFiles[] = { "general.params",
				"infrastructure-call.params", "clock-noquitting.params",
				"noconsole.params", "reports-nocsv.params", "specialists-cda.params" };
		String extraConfigFiles[] = null;
		final String marketId = "CDA";

		BenchmarkTest.logger.info("\nchecking TT in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-tt.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 70.0, 100.0, 0.0,
				10.0);

		BenchmarkTest.logger.info("\nchecking GD in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-gd.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 99.8, 100.0, 0.0,
				0.2);

		BenchmarkTest.logger.info("\nchecking PS in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-ps.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 80.0, 100, 0.0,
				5.0);

		BenchmarkTest.logger.info("\nchecking ZIP in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-zip.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 85.0, 100.0, 0.0,
				15.0);

		// TODO: doesn't produce expect results, check later
		// logger.info("\nchecking ZIQ in " + marketId + " ...");
		// extraConfigFiles = new String[] { "traders-ziq.params" };
		// checkEfficiency(marketId, configFiles, extraConfigFiles, 95.0, 100.0,
		// 0.0,
		// 5.0);

		BenchmarkTest.logger.info("\nchecking ZIC in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-zic.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 95.0, 100.0, 0.0,
				5.0);

		BenchmarkTest.logger.info("\nchecking RE in " + marketId + " ...");
		extraConfigFiles = new String[] { "traders-re.params" };
		checkEfficiency(marketId, configFiles, extraConfigFiles, 95.0, 100, 0.0,
				5.0);

	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(BenchmarkTest.suite());
	}

	public static Test suite() {
		return new TestSuite(BenchmarkTest.class);
	}

}
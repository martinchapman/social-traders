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

package edu.cuny.cat.valuation;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.random.Uniform;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class DailyRandomValuerTest extends RandomValuerTest {

	static Logger logger = Logger.getLogger(DailyRandomValuerTest.class);

	public DailyRandomValuerTest(final String name) {
		super(name);
	}

	@Override
	public void testUniformValuations() {
		System.out.println("\n>>>>>>>>>\t " + "testUniformValuations() \n");

		final double min = 50;
		final double max = 150;

		final DailyRandomValuerGenerator generator = new DailyRandomValuerGenerator(
				min, max);
		final ValuationPolicy valuer = generator.createValuer();

		Assert.assertTrue(valuer instanceof DailyRandomValuer);

		if (valuer instanceof DailyRandomValuer) {
			final DailyRandomValuer rvaluer = (DailyRandomValuer) valuer;
			Assert.assertTrue("Distribution expected to be uniform instead of "
					+ rvaluer.getDistribution().getClass(),
					rvaluer.getDistribution() instanceof Uniform);
			if (rvaluer.getDistribution() instanceof Uniform) {
				final Uniform uniform = (Uniform) rvaluer.getDistribution();
				testUniformDistribution(uniform, min, max);
			}
		}
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(DailyRandomValuerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(DailyRandomValuerTest.class);
	}
}
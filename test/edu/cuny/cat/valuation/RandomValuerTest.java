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

import edu.cuny.cat.MyTestCase;
import edu.cuny.random.Uniform;
import edu.cuny.util.CumulativeDistribution;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class RandomValuerTest extends MyTestCase {

	static Logger logger = Logger.getLogger(RandomValuerTest.class);

	public RandomValuerTest(final String name) {
		super(name);
	}

	public void testUniformValuations() {
		System.out.println("\n>>>>>>>>>\t " + "testUniformValuations() \n");

		final double min = 50;
		final double max = 150;
		final RandomValuerGenerator generator = new RandomValuerGenerator(min, max);
		for (int i = 0; i < 20; i++) {
			final ValuationPolicy valuer = generator.createValuer();
			Assert.assertTrue(valuer instanceof RandomValuer);
			if (valuer instanceof RandomValuer) {
				final RandomValuer rvaluer = (RandomValuer) valuer;
				Assert.assertTrue("Distribution expected to be uniform instead of "
						+ rvaluer.getDistribution().getClass(),
						rvaluer.getDistribution() instanceof Uniform);
				if (rvaluer.getDistribution() instanceof Uniform) {
					final Uniform uniform = (Uniform) rvaluer.getDistribution();
					testUniformDistribution(uniform, min, max);
				}
			}
		}
	}

	protected void testUniformDistribution(final Uniform uniform,
			final double min, final double max) {
		final double delta = 2.0;
		final CumulativeDistribution dist = new CumulativeDistribution();
		for (int i = 0; i < 10000; i++) {
			final double d = uniform.nextDouble();
			Assert.assertTrue(d <= max);
			Assert.assertTrue(d >= min);
			dist.newData(d);
		}

		Assert.assertEquals(dist.getMean(), (min + max) / 2, delta);
	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(RandomValuerTest.suite());
	}

	public static Test suite() {
		return new TestSuite(RandomValuerTest.class);
	}
}
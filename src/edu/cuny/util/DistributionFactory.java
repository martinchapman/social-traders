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

package edu.cuny.util;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractDistribution;

/**
 * This defines a factory class that creates {@link AbstractDistribution}s.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.4 $
 */

public abstract class DistributionFactory {

	protected static DistributionFactory currentFactory = new Cumulative();

	public static final String P_DISTFACTORY = "distribution";

	public static final String P_DEF_BASE = "P_DISTFACTORY";

	static Logger logger = Logger.getLogger(DistributionFactory.class);

	public static void setup(final ParameterDatabase parameters,
			final Parameter base) {
		try {
			DistributionFactory.currentFactory = parameters.getInstanceForParameter(
					base.push(DistributionFactory.P_DISTFACTORY), new Parameter(
							DistributionFactory.P_DEF_BASE), DistributionFactory.class);
		} catch (final ParamClassLoadException e) {
			DistributionFactory.logger.warn(e.getMessage());
		}
	}

	public static DistributionFactory getFactory() {
		return DistributionFactory.currentFactory;
	}

	public abstract Distribution create();

	public abstract Distribution create(String name);

	/**
	 * This defines a factory class that creates {@link HeavyweightDistribution}s.
	 * 
	 * @author Steve Phelps
	 */
	public static class Heavyweight extends DistributionFactory {

		@Override
		public Distribution create() {
			return new HeavyweightDistribution();
		}

		@Override
		public Distribution create(final String name) {
			return new HeavyweightDistribution(name);
		}
	}

	/**
	 * This defines a factory class that creates {@link CumulativeDistribution}s.
	 * 
	 * @author Steve Phelps
	 */
	public static class Cumulative extends DistributionFactory {

		@Override
		public Distribution create() {
			return new CumulativeDistribution();
		}

		@Override
		public Distribution create(final String name) {
			return new CumulativeDistribution(name);
		}
	}
}

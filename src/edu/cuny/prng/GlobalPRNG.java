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

package edu.cuny.prng;

import org.apache.log4j.Logger;

import cern.jet.random.engine.RandomEngine;
import cern.jet.random.engine.RandomSeedGenerator;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * A facility providing random number generators.
 * </p>
 * 
 * <p>
 * It maintains a default generator, whose seed may be specified in a parameter
 * file for the application or dynamically determined by the current system
 * time. Depending upon the mode of the application, the single default
 * generator may be used for all random number generation, or multiple
 * generators are created, each for different modules of the application. The
 * seeds for these generators are otherwise from an internal seed generator.
 * Whether using the single generator or multiple ones can be controlled by the
 * <code>usemultiengine</code> parameter.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.prng</tt><br>
 * <font size=-1>class, implements {@link PRNGFactory} interface</font></td>
 * <td valign=top>random number generator factory implementation class</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.seed</tt><br>
 * <font size=-1>double > 0</font></td>
 * <td valign=top>(the seed for the default random number generator)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base </i> <tt>.usemultiengine</tt><br>
 * <font size=-1>boolean (<code>true</code> by default)</font></td>
 * <td valign=top>(use multiple random number generators or not)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>prng</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.14 $
 */

public class GlobalPRNG {

	static Logger logger = Logger.getLogger(GlobalPRNG.class);

	public static final String P_USEMULTIENGINE = "usemultiengine";

	public static final String P_SEED = "seed";

	public static final String P_PRNG = "prng";

	public static final String P_DEF_BASE = GlobalPRNG.P_PRNG;

	/**
	 * the factory to create random number engines.
	 */
	protected PRNGFactory factory = new MT();

	/**
	 * whether use multiple engines. In multi-threading scenarios, using a single
	 * engine may lead to conflict.
	 */
	protected boolean useMultiEngine = true;

	/**
	 * the default random number engine.
	 */
	protected RandomEngine prng;

	/**
	 * the seed for the default engine.
	 */
	protected long seed;

	/**
	 * the seed generator used to obtain the default seed for the default engine
	 * or seeds for additional engine when multiple engines are used.
	 * 
	 * @see #seed
	 * @see #prng
	 * @see #useMultiEngine
	 */
	protected RandomSeedGenerator seedGenerator = new RandomSeedGenerator(
			(int) System.currentTimeMillis(), 1);

	public GlobalPRNG() {
		seed = seedGenerator.nextSeed();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		// TODO: to change the parameter names to base.prng.factory,
		// base.prng.usemultiengine,
		// base.prng.seed.

		final Parameter defBase = new Parameter(GlobalPRNG.P_DEF_BASE);

		factory = parameters.getInstanceForParameter(base.push(GlobalPRNG.P_PRNG),
				defBase, PRNGFactory.class);

		useMultiEngine = parameters.getBoolean(base
				.push(GlobalPRNG.P_USEMULTIENGINE), defBase
				.push(GlobalPRNG.P_USEMULTIENGINE), useMultiEngine);

		seed = parameters.getLongWithDefault(base.push(GlobalPRNG.P_SEED), defBase
				.push(GlobalPRNG.P_SEED), seed);

		prng = factory.create(seed);

		GlobalPRNG.logger.info("prng = " + factory.getDescription());
		GlobalPRNG.logger.info("seed = " + seed + "\n");
	}

	private synchronized RandomEngine createEngineWithSeed(final long seed) {
		return factory.create(seed);
	}

	/**
	 * creates a random number generator.
	 * 
	 * @return the created new generator
	 */
	protected synchronized RandomEngine createEngine() {
		final long seed = seedGenerator.nextSeed();
		return createEngineWithSeed(seed);
	}

	/**
	 * obtains a random number generator.
	 * 
	 * @return a new generator, if {@link #useMultiEngine} is <code>true</code>,
	 *         or the default generator that is shared by everyone otherwise.
	 */
	public synchronized RandomEngine getEngine() {
		if (useMultiEngine) {
			return createEngine();
		} else {
			return getDefaultEngine();
		}
	}

	/**
	 * gets the default random number generator, which can be shared by different
	 * components in a single-threading application; otherwise {
	 * {@link #getEngine()} should be used to create a generator for each thread
	 * for safety.
	 * 
	 * @return the default random number generator
	 */
	public synchronized RandomEngine getDefaultEngine() {
		if (prng == null) {
			GlobalPRNG.logger.warn("No default PRNG configured");
			initializeWithSeed(seed);
		}
		return prng;
	}

	public PRNGFactory getFactory() {
		return factory;
	}

	public void setFactory(final PRNGFactory factory) {
		this.factory = factory;
	}

	public long getSeed() {
		return seed;
	}

	public synchronized void initializeWithSeed(final long seed) {
		this.seed = seed;
		prng = createEngineWithSeed(seed);
	}

	/**
	 * sets the flag that controls whether a separate engine is created for each
	 * request or a default one is used all the time.
	 * 
	 * @param useMultiEngine
	 *          true if a new engine should be created; false otherwise.
	 */
	public synchronized void setUseMultiEngine(final boolean useMultiEngine) {
		this.useMultiEngine = useMultiEngine;
	}
}
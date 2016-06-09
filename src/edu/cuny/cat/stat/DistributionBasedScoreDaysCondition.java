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

package edu.cuny.cat.stat;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractDistribution;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A {@link ScoreDaysCondition} defines that game days are chosen for scoring
 * with probabilities according to the cumulative distribution function of a
 * certain distribution. Since some distributions are defined only on a certain
 * range, a linear transformation is used to map its output to a desired range.
 * </p>
 * 
 * <p>
 * Suppose the length of a game is <code>x</code> days and as the time moves on,
 * the probability of counting a day for scoring is hoped to increase, then the
 * following lists parameter suggestions when different distributions are used:
 * <ul>
 * <li>{@link edu.cuny.random.Beta}: alpha = beta = 0.5; a = x</li>
 * <li>{@link edu.cuny.random.Binomial}: n = 10; p = 0.5; a = x/10; b = x/5</li>
 * <li>{@link edu.cuny.random.ChiSquare}: freedom = 6; a = x/10</li>
 * <li>{@link edu.cuny.random.Exponential}: lambda = 1/x</li>
 * <li>{@link edu.cuny.random.Poisson}: mean = 10; a = x/10; b = -2x/5</li>
 * <li>{@link edu.cuny.random.StudentT}: freedom = 1; a = x/10; b = x/2</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following gives a list of examples to configure various distributions:
 * <code>
 * <pre>
 *  cat.server.report.5 = edu.cuny.cat.stat.ScoreReport
 *  cat.server.report.5.condition = edu.cuny.cat.stat.DistributionBasedScoreDaysCondition 
 *  
 *  # lambda = 1/gamelen
 *  cat.server.report.5.condition.distribution = edu.cuny.random.Exponential
 *  cat.server.report.5.condition.distribution.lambda = 0.0025
 *  cat.server.report.5.condition.a = 1
 *  cat.server.report.5.condition.b = 0
 * 
 *  # a = gamelen/10
 *  cat.server.report.5.condition.distribution = edu.cuny.random.ChiSquare
 *  cat.server.report.5.condition.distribution.freedom = 6
 *  cat.server.report.5.condition.a = 40
 *  cat.server.report.5.condition.b = 0
 * 
 *  # a = gamelen/10; b = gamelen/5
 *  cat.server.report.5.condition.distribution = edu.cuny.random.Binomial
 *  cat.server.report.5.condition.distribution.n = 10
 *  cat.server.report.5.condition.distribution.p = 0.5
 *  cat.server.report.5.condition.a = 40
 *  cat.server.report.5.condition.b = 80
 *  
 *  # a = gamelen/10; b = gamelen/2
 *  cat.server.report.5.condition.distribution = edu.cuny.random.StudentT
 *  cat.server.report.5.condition.distribution.mean = 1
 *  cat.server.report.5.condition.a = 40
 *  cat.server.report.5.condition.b = 200
 *  
 *  # a = gamelen/10; b = - 2*gamelen/5
 *  cat.server.report.5.condition.distribution = edu.cuny.random.Poisson
 *  cat.server.report.5.condition.distribution.mean = 10
 *  cat.server.report.5.condition.a = 40
 *  cat.server.report.5.condition.b = -160
 *  
 *  # a = gamelen
 *  cat.server.report.5.condition.distribution = edu.cuny.random.Beta
 *  cat.server.report.5.condition.distribution.alpha = 0.5
 *  cat.server.report.5.condition.distribution.beta = 0.5
 *  cat.server.report.5.condition.a = &amp;cat.server.gamelen;
 *  cat.server.report.5.condition.b = 0
 * </pre>
 * </code>
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.distribution</tt><br>
 * <font size=-1>full name of class inheriting
 * <code>cern.jet.random.AbstractDistribution</code></font></td>
 * <td valign=top>(the distribution used for choosing scoring days, e.g.
 * {@link edu.cuny.random.ChiSquare})</td>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.a</tt><br>
 * <font size=-1>double (1 by default)</font></td>
 * <td valign=top>(slope of the linear transformation)</td>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.b</tt><br>
 * <font size=-1>double (0 by default)</font></td>
 * <td valign=top>(y-intercept of the linear transformation)</td>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class DistributionBasedScoreDaysCondition extends
		AbstractScoreDaysCondition {

	static final Logger logger = Logger
			.getLogger(DistributionBasedScoreDaysCondition.class);

	public static final String P_DISTRIBUTION = "distribution";

	public static final String P_A = "a";

	public static final String P_B = "b";

	public static final String P_DEBUG = "debug";

	public static final double DEFAULT_A = 1;

	public static final double DEFAULT_B = 0;

	protected AbstractDistribution distribution;

	protected double a;

	protected double b;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		distribution = parameters.getInstanceForParameterEq(base
				.push(DistributionBasedScoreDaysCondition.P_DISTRIBUTION), null,
				AbstractDistribution.class);
		if (distribution instanceof Parameterizable) {
			((Parameterizable) distribution).setup(parameters, base
					.push(DistributionBasedScoreDaysCondition.P_DISTRIBUTION));
		}

		a = parameters.getDoubleWithDefault(base
				.push(DistributionBasedScoreDaysCondition.P_A), null,
				DistributionBasedScoreDaysCondition.DEFAULT_A);
		b = parameters.getDoubleWithDefault(base
				.push(DistributionBasedScoreDaysCondition.P_B), null,
				DistributionBasedScoreDaysCondition.DEFAULT_B);
	}

	@Override
	protected boolean updateTaken(final int day) {

		final double d = a * distribution.nextDouble() + b;

		// if (debug) {
		// logger.info(Utils.indent("d: " + d) + "\n");
		// }

		if (day >= d) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		String s = super.toString();

		s += "\n" + Utils.indent(distribution.toString());
		s += "\n" + Utils.indent("a: " + a);
		s += "\n" + Utils.indent("b: " + b);

		return s;
	}
}

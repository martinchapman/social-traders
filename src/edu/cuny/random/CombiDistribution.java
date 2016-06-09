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
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.random;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cern.jet.random.AbstractDistribution;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Resetable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A distribution that combines several different distributions. How to combine
 * these distributions though has yet to be determined in its subclasses.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.n</tt><br>
 * <font size=-1>int >= 1</font></td>
 * <td valign=top>(the number of different distributions to configure)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.</tt><i>n</i><br>
 * <font size=-1>name of class, inheriting
 * {@link cern.jet.random.AbstractDistribution}</font></td>
 * <td valign=top>(the <i>n</i>th distribution)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>combi_distribution</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public abstract class CombiDistribution extends AbstractDistribution implements
		Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(CombiDistribution.class);

	protected List<AbstractDistribution> distributions = null;

	public static final String P_DEF_BASE = "combi_distribution";

	public static final String P_NUM = "n";

	public CombiDistribution(final List<AbstractDistribution> distributions) {
		this.distributions = distributions;
	}

	public CombiDistribution() {
		distributions = new LinkedList<AbstractDistribution>();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		final Parameter defBase = new Parameter(CombiDistribution.P_DEF_BASE);

		final int numLoggers = parameters.getInt(
				base.push(CombiDistribution.P_NUM), defBase
						.push(CombiDistribution.P_NUM), 1);

		for (int i = 0; i < numLoggers; i++) {
			final AbstractDistribution distribution = parameters
					.getInstanceForParameter(base.push(i + ""), defBase.push(i + ""),
							AbstractDistribution.class);
			if (distribution instanceof Parameterizable) {
				((Parameterizable) distribution).setup(parameters, base.push(i + ""));
			}
			addDistribution(distribution);
		}
	}

	/**
	 * Add a new distribution
	 */
	public void addDistribution(final AbstractDistribution distribution) {
		distributions.add(distribution);
	}

	public void reset() {
		for (final AbstractDistribution distribution : distributions) {
			if (CombiDistribution.logger instanceof Resetable) {
				((Resetable) distribution).reset();
			}
		}
	}

	public Iterator<AbstractDistribution> distributionIterator() {
		return distributions.iterator();
	}

	public AbstractDistribution getDistribution(final int index) {
		return distributions.get(index);
	}

	public <D extends AbstractDistribution> D getDistribution(
			final Class<D> distributionClass) {
		for (final AbstractDistribution distribution : distributions) {
			if (distributionClass.isInstance(distribution)) {
				return distributionClass.cast(distribution);
			} else if (distribution instanceof CombiDistribution) {
				final D d = ((CombiDistribution) distribution)
						.getDistribution(distributionClass);
				if (d != null) {
					return d;
				}
			}
		}

		return null;
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();

		for (final AbstractDistribution distribution : distributions) {
			s += Utils.indent(distribution.toString());
		}

		return s;
	}
}
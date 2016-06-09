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

package edu.cuny.cat.market.pricing;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * Abstract superclass for auctioneer pricing policies parameterised by k.
 * 
 * <p>
 * <b>Parameters </b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.k</tt><br>
 * <font size=-1>0 <=int <=1 </font></td>
 * <td valign=top>(determining a value in a given price range)</td>
 * <tr>
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>k_pricing</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.20 $
 */

public abstract class KPricingPolicy extends PricingPolicy {

	static Logger logger = Logger.getLogger(KPricingPolicy.class);

	public static final String P_K = "k";

	public static final String P_DEF_BASE = "k_pricing";

	public static final double DEFAULT_K = 0.5;

	protected double k;

	public KPricingPolicy() {
		this(KPricingPolicy.DEFAULT_K);
	}

	public KPricingPolicy(final double k) {
		this.k = k;
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		k = parameters.getDoubleWithDefault(base.push(KPricingPolicy.P_K),
				new Parameter(KPricingPolicy.P_DEF_BASE).push(KPricingPolicy.P_K), k);
	}

	public void setK(final double k) {
		this.k = k;
	}

	public double getK() {
		return k;
	}

	public double kInterval(final double a, final double b) {
		double p = k * b + (1 - k) * a;

		// adjust p because in some cases p may be a little bit outside the range
		// between a and b due to the discrete nature of k
		if (p > Math.max(a, b)) {
			p = Math.max(a, b);
		} else if (p < Math.min(a, b)) {
			p = Math.min(a, b);
		}

		return p;
	}

	@Override
	public String toString() {
		return super.toString() + " " + KPricingPolicy.P_K + ":" + k;
	}

}
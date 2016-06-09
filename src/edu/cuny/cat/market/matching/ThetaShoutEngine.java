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

package edu.cuny.cat.market.matching;

import java.util.Iterator;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * This class represents a continuum of shout matching policies, including
 * {@link FourHeapShoutEngine} and {@link LazyMaxVolumeShoutEngine}. It uses a
 * matching quantity coefficient, {@link #theta}, to determine a matching
 * quantity that falls between 0 and the maximal quantity calculated in
 * {@link LazyMaxVolumeShoutEngine}. When {@link #theta} is 1, the quantity is
 * the maximal; when {@link #theta} is 0, the quantity is the equilibrium
 * quantity; and when {@link #theta} is -1, no matches will be made.
 * </p>
 * 
 * <p>
 * <b>Parameters </b>
 * 
 * <table>
 * <tr>
 * <td valign=top><i>base </i> <tt>.theta</tt><br>
 * <font size=-1>-1 <= double <= 1 (0 by default)</font></td>
 * <td valign=top>(the matching quantity coefficient controlling the matching
 * quantity relative to the equilibrium quantity and the maximial quantity.)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>theta_matching</tt><br>
 * </td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class ThetaShoutEngine extends LazyMaxVolumeShoutEngine {

	static Logger logger = Logger.getLogger(ThetaShoutEngine.class);

	public static final String P_THETA = "theta";

	public static final String P_DEF_BASE = "theta_matching";

	/**
	 * by default, do maximal volume
	 */
	public static final double DEFAULT_THETA = 0;

	/**
	 * matching quantity coefficient
	 */
	protected double theta;

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(ThetaShoutEngine.P_DEF_BASE);

		theta = parameters.getDoubleWithDefault(
				base.push(ThetaShoutEngine.P_THETA), defBase
						.push(ThetaShoutEngine.P_THETA), ThetaShoutEngine.DEFAULT_THETA);

		if (theta < -1) {
			theta = -1;
		} else if (theta > 1) {
			theta = 1;
		}
	}

	/**
	 * @return the demand at the price of the lowest ask
	 */
	protected int getDemandAboveLowestAsk() {
		int q = 0;

		final ListIterator<Shout> askItor = asks.listIterator();
		final ListIterator<Shout> bidItor = bids.listIterator();

		Shout ask = null, bid = null;

		// 1. find lowest ask
		if (askItor.hasNext()) {
			ask = askItor.next();

			// 2. find lowest bid above the lowest ask
			while (true) {
				if (bidItor.hasNext()) {
					bid = bidItor.next();
					if (bid.getPrice() >= ask.getPrice()) {
						break;
					}
				} else {
					bid = null;
					break;
				}
			}

			while (bid != null) {
				q += bid.getQuantity();
				bid = bidItor.hasNext() ? bidItor.next() : null;
			}
		}

		return q;
	}

	/**
	 * @return the equilibrium quantity
	 */
	protected int getEquilibriumQuantity() {
		int demand = getDemandAboveLowestAsk();
		int supply = 0;
		int qe = 0;

		final Iterator<Shout> askItor = asks.iterator();
		final Iterator<Shout> bidItor = bids.iterator();

		Shout ask = null, bid = null;

		// 1. find lowest ask
		if (askItor.hasNext()) {
			ask = askItor.next();

			// 2. find lowest bid above the lowest ask
			while (true) {
				if (bidItor.hasNext()) {
					bid = bidItor.next();
					if (bid.getPrice() >= ask.getPrice()) {
						break;
					}
				} else {
					bid = null;
					break;
				}
			}

			// 3. find the equilibrium quantity
			while (bid != null) {
				if ((ask != null) && (ask.getPrice() <= bid.getPrice())) {
					supply += ask.getQuantity();
					ask = askItor.hasNext() ? askItor.next() : null;
				} else {
					demand -= bid.getQuantity();
					bid = bidItor.hasNext() ? bidItor.next() : null;
				}

				qe = Math.max(qe, Math.min(demand, supply));
				if (qe > demand) {
					// already find the equilibrium point, break out.
					break;
				}
			}
		}

		return qe;
	}

	@Override
	protected int getMatchingQuantity() {
		final int qmv = super.getMatchingQuantity();
		final int qe = getEquilibriumQuantity();

		if (qe > qmv) {
			ThetaShoutEngine.logger
					.error("The equilibrium quantity should NOT surpass the maxvolume-matching quantity !");
		}

		// calcuate matching quantity based on theta
		if (theta <= 0) {
			return (int) Math.round((1 + theta) * qe);
		} else {
			return (int) Math.round((1 - theta) * qe + theta * qmv);
		}

	}
}
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

/**
 * <p>
 * A queue with fixed length, which can be useful when tracking a sliding window
 * on a data series
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class FixedLengthQueue implements Resetable {

	protected double list[];

	protected int curIndex;

	protected double total;

	protected double totalSq;

	protected int n;

	static Logger logger = Logger.getLogger(FixedLengthQueue.class);

	public FixedLengthQueue(final int length) {
		list = new double[length];
		init0();
	}

	private void init0() {
		curIndex = 0;
		total = 0;
		totalSq = 0;
		n = 0;
	}

	public void reset() {
		for (int i = 0; i < list.length; i++) {
			list[i] = 0;
		}

		init0();
	}

	public void newData(final double value) {
		total -= list[curIndex];
		totalSq -= list[curIndex] * list[curIndex];
		list[curIndex] = value;
		total += value;
		totalSq += value * value;

		curIndex++;
		curIndex %= list.length;

		if (n < list.length) {
			n++;
		}
	}

	public int getN() {
		return n;
	}

	public double getMean() {
		return total / getN();
	}

	public double getTotal() {
		return total;
	}

	public double getVariance() {
		final double origin = getMean();
		if (n <= 1) {
			return 0;
		} else {
			return (totalSq - n * origin * origin) / (n - 1);
		}
	}

	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	public void log() {
		FixedLengthQueue.logger.info(Utils.indent("n:\t" + getN()));
		FixedLengthQueue.logger.info(Utils.indent("mean:\t" + getMean()));
		FixedLengthQueue.logger.info(Utils.indent("var:\t" + getVariance()));
		FixedLengthQueue.logger.info(Utils.indent("stdev:\t" + getStdDev()));
	}

	@Override
	public String toString() {
		String s = "[";
		int start;
		if (getN() < list.length) {
			start = 0;
		} else {
			start = curIndex;
		}

		for (int i = 0; i < n; i++) {
			s += list[(start + i) % list.length];
			if (i < n - 1) {
				s += ", ";
			} else {
				s += "]";
			}
		}
		return s;
	}
}

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

package edu.cuny.cat.valuation;

import org.apache.log4j.Logger;

/**
 * A framework of valuation policy in which a valuation is drawn from a
 * distribution. The drawing will be repeated if the valuation falls out of the
 * pre-determined range in {@link RandomValuerGenerator}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 */

public class RandomValuer extends AbstractRandomValuer {

	/**
	 * The generator instance that created this valuer
	 */
	protected RandomValuerGenerator generator;

	/**
	 * the maximum times to redraw the valuation from the distribution when
	 * previous drawings bring values out of the range defined in
	 * {@link RandomValuerGenerator}.
	 */
	public static final int MAX_REDRAWING_TIMES = 5;

	static Logger logger = Logger.getLogger(RandomValuer.class);

	public void setGenerator(final RandomValuerGenerator generator) {
		this.generator = generator;
	}

	public RandomValuerGenerator getGenerator() {
		return generator;
	}

	/**
	 * TODO: to check later if this non-deterministic behavior is supposed so.
	 */
	@Override
	public void drawRandomValue() {
		int count = 0;
		while (true) {
			super.drawRandomValue();

			// make sure the valuation is between minValue and maxValue
			if ((getValue() > generator.getMaxValue())
					|| (getValue() < generator.getMinValue())) {
				count++;
				if (count > RandomValuer.MAX_REDRAWING_TIMES) {
					RandomValuer.logger.error("Please check the parameters of "
							+ generator.getClass().getSimpleName()
							+ " to make sure the distribution covers the min-max range.");
				}
			} else {
				break;
			}
		}
	}
}

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

package edu.cuny.cat.trader.strategy;

import java.io.Serializable;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * An abstract implementation of FixedQuantityStrategy.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.quantity</tt><br>
 * <font size=-1>int &gt;= 0</font></td>
 * <td valign=top>(the quantity to bid for in each shout)</td>
 * </tr>
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>fixed_quantity_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.13 $
 */

public abstract class FixedQuantityStrategyImpl extends AbstractStrategy
		implements FixedQuantityStrategy, Parameterizable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected int quantity = 1;

	public static final String P_DEF_BASE = "fixed_quantity_strategy";

	static final String P_QUANTITY = "quantity";

	public FixedQuantityStrategyImpl() {
		this(null);
	}

	public FixedQuantityStrategyImpl(final AbstractTradingAgent agent) {
		super(agent);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		quantity = parameters.getIntWithDefault(base
				.push(FixedQuantityStrategyImpl.P_QUANTITY), new Parameter(
				FixedQuantityStrategyImpl.P_DEF_BASE)
				.push(FixedQuantityStrategyImpl.P_QUANTITY), quantity);
	}

	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public int determineQuantity() {
		return quantity;
	}

	@Override
	public boolean modifyShout(final Shout.MutableShout shout) {
		shout.setQuantity(quantity);
		return super.modifyShout(shout);
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += " " + FixedQuantityStrategyImpl.P_QUANTITY + ":" + quantity;
		return s;
	}

}
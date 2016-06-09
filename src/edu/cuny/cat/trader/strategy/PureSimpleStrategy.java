package edu.cuny.cat.trader.strategy;

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

import java.io.Serializable;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.trader.AbstractTradingAgent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;

/**
 * <p>
 * A trading strategy in which we bid a constant mark-up on the agent's private
 * value.
 * </p>
 * 
 * <b>Parameters</b>
 * 
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base.</i><tt>delta</tt><br>
 * <font size=-1>double</font></td>
 * <td valign=top>(the markup over our private valuation to bid for)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>pure_simple_strategy</tt></td>
 * </tr>
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.8 $
 */

public class PureSimpleStrategy extends FixedQuantityStrategyImpl implements
		Serializable, Prototypeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected double margin;

	public final static String P_DEF_BASE = "pure_simple_strategy";

	static final String P_DELTA = "delta";

	static final double DEFAULT_DELTA = 5;

	public PureSimpleStrategy() {
		this(null, PureSimpleStrategy.DEFAULT_DELTA, 1);
	}

	public PureSimpleStrategy(final AbstractTradingAgent agent,
			final double margin, final int quantity) {
		super(agent);
		this.margin = margin;
		this.quantity = quantity;
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);
		margin = parameters.getDoubleWithDefault(base
				.push(PureSimpleStrategy.P_DELTA), new Parameter(
				PureSimpleStrategy.P_DEF_BASE).push(PureSimpleStrategy.P_DELTA),
				PureSimpleStrategy.DEFAULT_DELTA);
	}

	@Override
	public Object protoClone() {
		Object clonedStrategy;
		try {
			clonedStrategy = clone();
		} catch (final CloneNotSupportedException e) {
			throw new Error(e);
		}
		return clonedStrategy;
	}

	@Override
	public boolean modifyShout(final Shout.MutableShout shout) {
		double delta;
		if (agent.isSeller()) {
			delta = margin;
		} else {
			delta = -margin;
		}
		shout.setPrice(agent.getPrivateValue() + delta);
		shout.setQuantity(quantity);
		if (shout.getPrice() < 0) {
			shout.setPrice(0);
		}
		return super.modifyShout(shout);
	}

	public void setMargin(final double margin) {
		this.margin = margin;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n" + Utils.indent(PureSimpleStrategy.P_DELTA + ":" + margin);
		return s;
	}
}
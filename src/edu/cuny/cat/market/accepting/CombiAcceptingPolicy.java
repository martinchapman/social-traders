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

package edu.cuny.cat.market.accepting;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.market.Auctioneer;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.Utils;

/**
 * <p>
 * An accepting policy that combines several different accepting policies.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><i>base</i><tt>.n</tt><br>
 * <font size=-1>int &gt;= 1</font></td>
 * <td valign=top>(the number of different accepting policies to configure)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.</tt><i>n</i><br>
 * <font size=-1>name of class, inheriting {@link ShoutAcceptingPolicy}</font></td>
 * <td valign=top>(the <i>n</i>th accepting policy)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>combi_accepting</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public abstract class CombiAcceptingPolicy extends ShoutAcceptingPolicy {

	public static final String P_DEF_BASE = "combi_accepting";

	public static final String P_NUM = "n";

	protected List<ShoutAcceptingPolicy> policies = null;

	public CombiAcceptingPolicy(final List<ShoutAcceptingPolicy> policies) {
		this.policies = policies;
	}

	public CombiAcceptingPolicy() {
		policies = new LinkedList<ShoutAcceptingPolicy>();
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		final Parameter defBase = new Parameter(CombiAcceptingPolicy.P_DEF_BASE);

		final int numOfPolicies = parameters.getInt(base
				.push(CombiAcceptingPolicy.P_NUM), defBase
				.push(CombiAcceptingPolicy.P_NUM), 0);

		for (int i = 0; i < numOfPolicies; i++) {
			final ShoutAcceptingPolicy policy = parameters.getInstanceForParameter(
					base.push(i + ""), defBase.push(i + ""), ShoutAcceptingPolicy.class);
			if (policy instanceof Parameterizable) {
				((Parameterizable) policy).setup(parameters, base.push(i + ""));
			}
			policy.initialize();
			addPolicy(policy);
		}
	}

	@Override
	public void reset() {
		super.reset();

		for (final ShoutAcceptingPolicy policy : policies) {
			policy.reset();
		}
	}

	/**
	 * Add a new policy
	 */
	public void addPolicy(final ShoutAcceptingPolicy policy) {
		policies.add(policy);
	}

	public Iterator<ShoutAcceptingPolicy> policyIterator() {
		return policies.iterator();
	}

	public <P extends ShoutAcceptingPolicy> P getPolicy(final Class<P> policyClass) {
		for (final ShoutAcceptingPolicy policy : policies) {
			if (policyClass.isInstance(policy)) {
				return policyClass.cast(policy);
			} else if (policy instanceof CombiAcceptingPolicy) {
				final P p = ((CombiAcceptingPolicy) policy).getPolicy(policyClass);
				if (p != null) {
					return p;
				}
			}
		}

		return null;
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		super.eventOccurred(event);

		for (final ShoutAcceptingPolicy policy : policies) {
			try {
				policy.eventOccurred(event);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setAuctioneer(final Auctioneer auctioneer) {
		super.setAuctioneer(auctioneer);

		for (final ShoutAcceptingPolicy policy : policies) {
			policy.setAuctioneer(auctioneer);
		}
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();

		int n = 0;
		for (final ShoutAcceptingPolicy policy : policies) {
			s += Utils.indent("\n" + n + ":" + Utils.indent(policy.toString()));
			n++;
		}

		return s;
	}
}
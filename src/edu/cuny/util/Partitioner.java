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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

/**
 * A class that iterates over all numerical partitions of <code>n</code> into
 * <code>k</code> distinct parts including commutative duplications and parts
 * containing zero.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.4 $
 */

public class Partitioner implements Iterator<int[]> {

	protected Stack<PartitionerState> stack;

	protected HashSet<PartitionerState> visitedStates;

	public Partitioner(final int n, final int k) {
		stack = new Stack<PartitionerState>();
		visitedStates = new HashSet<PartitionerState>();
		stack.push(new PartitionerState(new int[k], n));
	}

	public boolean hasNext() {
		return !stack.isEmpty();
	}

	public int[] next() {
		return partition();
	}

	public void remove() {
		throw new IllegalArgumentException("method remove() not implemented");
	}

	protected int[] partition() {
		while (!stack.isEmpty()) {
			final PartitionerState state = stack.pop();
			if (state.n == 0) {
				return state.p;
			}
			final int[] p = state.p;
			for (int i = 0; i < p.length; i++) {
				final int[] p1 = p.clone();
				p1[i]++;
				final PartitionerState newState = new PartitionerState(p1, state.n - 1);
				if (!visitedStates.contains(newState)) {
					stack.push(newState);
					visitedStates.add(newState);
				}
			}
		}
		return null;
	}

	public static void main(final String[] args) {
		final Partitioner p = new Partitioner(20, 3);
		while (p.hasNext()) {
			final int[] partition = p.next();
			for (final int element : partition) {
				System.out.print(element + " ");
			}
			System.out.println("");
		}
	}
}

class PartitionerState {

	protected int n;

	protected int[] p;

	public PartitionerState(final int[] p, final int n) {
		this.n = n;
		this.p = p;
	}

	@Override
	public boolean equals(final Object other) {
		final PartitionerState s = (PartitionerState) other;
		if (n != s.n) {
			return false;
		}
		for (int i = 0; i < p.length; i++) {
			if (p[i] != s.p[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = n;
		int m = 1;
		for (final int element : p) {
			hash += m * element;
			m <<= 1;
		}
		return hash;
	}
}
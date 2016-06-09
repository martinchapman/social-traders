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

package edu.cuny.cat;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.6 $
 */

public class MyTestCase extends TestCase {

	public MyTestCase(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		System.out.println("\n..............................................\n. "
				+ getClass().getSimpleName() + "\n");
	}

	protected static void checkEquals(final String msg, final int actual,
			final int expected) {
		Assert.assertEquals(msg, actual, expected);
	}

	protected static void checkEquals(final String msg, final int actual[],
			final int expected) {
		for (final int element : actual) {
			MyTestCase.checkEquals(msg, element, expected);
		}
	}

	protected static void checkEquals(final String msg, final int actual[],
			final int expected[]) {
		for (int i = 0; i < actual.length; i++) {
			MyTestCase.checkEquals(msg, actual[i], expected[i]);
		}
	}

	protected static void checkEquals(final String msg, final double actual,
			final double expected, final double errorBound) {
		Assert.assertEquals(msg, actual, expected, expected * errorBound);
	}

	protected static void checkEquals(final String msg, final double actual[],
			final double expected, final double errorBound) {
		for (final double element : actual) {
			MyTestCase.checkEquals(msg, element, expected, errorBound);
		}
	}

	protected static void checkEquals(final String msg, final double actual[],
			final double expected[], final double errorBound) {
		for (int i = 0; i < actual.length; i++) {
			MyTestCase.checkEquals(msg, actual[i], expected[i], errorBound);
		}
	}
}

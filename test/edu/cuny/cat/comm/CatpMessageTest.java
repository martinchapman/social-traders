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

package edu.cuny.cat.comm;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;

/**
 * @author Jinzhong Niu
 * @version $Revision: 1.5 $
 */

public class CatpMessageTest extends MyTestCase {

	static Logger logger = Logger.getLogger(CatpMessageTest.class);

	public CatpMessageTest(final String name) {
		super(name);
	}

	public void testConcatenate() {
		System.out.println("\n>>>>>>>>>\t " + "testConcatenate() \n");

		final String texts[] = { "1", null, "2", null, "" };

		final String result = Message.concatenate(texts);

		Assert
				.assertTrue(("1" + Message.VALUE_SEPARATOR + "2" + Message.VALUE_SEPARATOR)
						.equals(result));

	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(CatpMessageTest.suite());
	}

	public static Test suite() {
		return new TestSuite(CatpMessageTest.class);
	}
}
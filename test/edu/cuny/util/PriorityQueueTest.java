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

package edu.cuny.util;

import java.util.Comparator;
import java.util.PriorityQueue;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import edu.cuny.cat.MyTestCase;

/**
 * This test aims to test {@link java.util.PriorityQueue} with
 * {@link java.util.Comparator}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public class PriorityQueueTest extends MyTestCase {

	static Logger logger = Logger.getLogger(PriorityQueueTest.class);

	PriorityQueue<Task> queue;

	public PriorityQueueTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() {
		super.setUp();
		queue = new PriorityQueue<Task>(20, new TaskComparator());
	}

	public void testOrder() {
		System.out.println("\n>>>>>>>>>\t " + "testOrder() \n");

		queue.add(new RegularTask("0"));
		queue.add(new PriorityTask("1"));
		queue.add(new PriorityTask("2"));
		queue.add(new RegularTask("3"));
		queue.add(new RegularTask("4"));
		queue.add(new PriorityTask("5"));

		Task task = queue.remove();
		Assert.assertTrue((task.tid == 1) && (task instanceof PriorityTask));
		PriorityQueueTest.logger.info(task);

		task = queue.remove();
		Assert.assertTrue((task.tid == 2) && (task instanceof PriorityTask));
		PriorityQueueTest.logger.info(task);

		task = queue.remove();
		Assert.assertTrue((task.tid == 5) && (task instanceof PriorityTask));
		PriorityQueueTest.logger.info(task);

		task = queue.remove();
		Assert.assertTrue((task.tid == 0) && (task instanceof RegularTask));
		PriorityQueueTest.logger.info(task);

		task = queue.remove();
		Assert.assertTrue((task.tid == 3) && (task instanceof RegularTask));
		PriorityQueueTest.logger.info(task);

		task = queue.remove();
		Assert.assertTrue((task.tid == 4) && (task instanceof RegularTask));
		PriorityQueueTest.logger.info(task);

		Assert.assertTrue(queue.isEmpty());

	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(PriorityQueueTest.suite());
	}

	public static Test suite() {
		return new TestSuite(PriorityQueueTest.class);
	}

	static class Task {

		static int gid = 0;

		int tid;

		String value;

		public Task(String value) {
			tid = Task.gid++;
			this.value = value;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " tid:" + tid + " value: " + value;
		}
	}

	class RegularTask extends Task {
		public RegularTask(String value) {
			super(value);
		}
	}

	class PriorityTask extends Task {
		public PriorityTask(String value) {
			super(value);
		}
	}

	class TaskComparator implements Comparator<Task> {

		public int compare(Task t0, Task t1) {
			if (((t0 instanceof PriorityTask) && (t1 instanceof PriorityTask))
					|| ((t0 instanceof RegularTask) && (t1 instanceof RegularTask))) {
				return t0.tid - t1.tid;
			} else if ((t0 instanceof PriorityTask) && (t1 instanceof RegularTask)) {
				return -1;
			} else if ((t0 instanceof RegularTask) && (t1 instanceof PriorityTask)) {
				return 1;
			} else {
				PriorityQueueTest.logger.error("Invalid task type !");
				Assert.assertTrue(false);
				return 0;
			}
		}
	}
}
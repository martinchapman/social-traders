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

package edu.cuny.ai.learning;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.collections15.bag.TreeBag;
import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Prototypeable;
import edu.cuny.util.Utils;
import edu.cuny.util.io.DataWriter;

/**
 * maintains a sorted sliding window over the trained data series and use a
 * percentage to determine a data item in the window as the output learned.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.windowsize</tt><br>
 * <font size=-1>int >= 1 (4 by default)</font></td>
 * <td valign=top>(the size of sliding window)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.percent</tt><br>
 * <font size=-1>0 <= double <= 1 (0.5 by default)</font></td>
 * <td valign=top>(the position in the sorted sliding window with 1 for the
 * largest)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>sorted_sliding_window_learner</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class SortedSlidingWindowLearner extends AbstractLearner implements
		MimicryLearner, SelfKnowledgable, Prototypeable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_DEF_BASE = "sorted_sliding_window_learner";

	public static final String P_WINDOWSIZE = "windowsize";

	public static final String P_PERCENT = "percent";

	public static final int DEFAULT_WINDOWSIZE = 4;

	public static final double DEFAULT_PERCENT = 0.5;

	/**
	 * A parameter used to adjust the size of the window
	 */
	protected int windowSize = SortedSlidingWindowLearner.DEFAULT_WINDOWSIZE;

	protected double percent = SortedSlidingWindowLearner.DEFAULT_PERCENT;

	private int index;

	/**
	 * The current output level.
	 */
	protected double currentOutput;

	protected TreeBag<DataItem> memory;

	protected DataItem head, tail;

	static Logger logger = Logger.getLogger(SortedSlidingWindowLearner.class);

	public SortedSlidingWindowLearner() {
		memory = new TreeBag<DataItem>(new DataItemComparator());
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		windowSize = parameters.getIntWithDefault(base
				.push(SortedSlidingWindowLearner.P_WINDOWSIZE), new Parameter(
				SortedSlidingWindowLearner.P_DEF_BASE)
				.push(SortedSlidingWindowLearner.P_WINDOWSIZE), windowSize);

		if (windowSize < 1) {
			SortedSlidingWindowLearner.logger
					.error(SortedSlidingWindowLearner.P_WINDOWSIZE
							+ " cannot be less than 1 and the default value is used instead !");
			windowSize = SortedSlidingWindowLearner.DEFAULT_WINDOWSIZE;
		}

		percent = parameters.getDoubleWithDefault(base
				.push(SortedSlidingWindowLearner.P_PERCENT), new Parameter(
				SortedSlidingWindowLearner.P_DEF_BASE)
				.push(SortedSlidingWindowLearner.P_PERCENT), percent);
		if ((percent < 0) || (percent > 1)) {
			SortedSlidingWindowLearner.logger
					.error(SortedSlidingWindowLearner.P_PERCENT
							+ " should be between 0 and 1 both inclusive and the default value is used instead !");
			percent = SortedSlidingWindowLearner.DEFAULT_PERCENT;
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		init1();
	}

	private void init1() {
		index = (int) (windowSize * percent);
		if (index < 0) {
			index = 0;
		} else if (index >= windowSize) {
			index = windowSize - 1;
		}

		head = tail = null;
	}

	public void reset() {
		memory.clear();
		init1();
	}

	public void randomInitialise() {
	}

	public void setWindowSize(final int windowSize) {
		this.windowSize = windowSize;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public double act() {
		return currentOutput;
	}

	public void train(final double target) {

		DataItem item = new DataItem(target);
		if (tail != null) {
			tail.next = item;
			tail = tail.next;
		} else {
			tail = item;
			head = item;
		}

		if (goodEnough()) {
			memory.remove(head);
			head = head.next;
		}

		memory.add(item);

		final Iterator<DataItem> iterator = memory.iterator();
		item = null;
		for (int i = 0; i < index; i++) {
			if (iterator.hasNext()) {
				item = iterator.next();
			} else {
				item = memory.first();
				break;
			}
		}

		if (item != null) {
			currentOutput = item.value;
		}
	}

	@Override
	public void dumpState(final DataWriter out) {
		// TODO
	}

	public double getCurrentOutput() {
		return currentOutput;
	}

	/**
	 * no effect on FixedLengthQueue-based next output!
	 */
	public void setOutputLevel(final double currentOutput) {
		this.currentOutput = currentOutput;
	}

	@Override
	public double getLearningDelta() {
		return 0;
	}

	public Object protoClone() {
		final SortedSlidingWindowLearner clone = new SortedSlidingWindowLearner();
		clone.setWindowSize(windowSize);
		return clone;
	}

	public boolean goodEnough() {
		return memory.size() >= windowSize;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "\n"
				+ Utils.indent("windowSize:" + windowSize + " percent:" + percent);
		return s;
	}

	class DataItem {
		double value;

		DataItem next;

		public DataItem(final double value) {
			this.value = value;
		}
	}

	class DataItemComparator implements Comparator<DataItem> {

		public int compare(final DataItem item0, final DataItem item1) {
			if (item0.value > item1.value) {
				return 1;
			} else if (item0.value < item1.value) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}

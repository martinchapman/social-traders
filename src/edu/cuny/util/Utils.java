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
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cuny.prng.GlobalPRNG;
import edu.cuny.random.Uniform;

/**
 * A collection of utility functions.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 */

public class Utils {

	protected static Logger logger = Logger.getLogger(Utils.class);

	public final static DecimalFormat formatter = new DecimalFormat(
			"+#########0.000;-#########.000");

	public final static int DEFAULT_ALIGNMENT_LENGTH = 12;

	/**
	 * @param value
	 *          a double to be formatted
	 * @return a nicely formatted string represetation
	 */
	public static String format(final double value) {
		return Utils.formatter.format(value);
	}

	/**
	 * indents the specified multiple lines with a string as prefix. This is
	 * useful to display strings nicely on screen.
	 * 
	 * @param lines
	 *          multi-line string to be indented
	 * @param prefix
	 *          a string, most likely a certain number of <code>\t<code>
	 * @return the indented string
	 */
	public static String indent(final String lines, final String prefix) {
		return prefix + lines.replaceAll("\n", "\n" + prefix);
	}

	/**
	 * indents the specified mutiple lines with 2 space characters.
	 * 
	 * @param lines
	 *          multi-line string to be indented
	 * @return the indented string
	 */
	public static String indent(final String lines) {
		return Utils.indent(lines, "  ");
	}

	public static final String SPACES[] = { " ", "  ", "   ", "    ", "     ",
			"      ", "       ", "        ", "         ", "          " };

	/**
	 * 
	 * @param str
	 * @return a string that starts with <code>str</code> and is appended with
	 *         spaces to be at least as long as {@link #DEFAULT_ALIGNMENT_LENGTH}
	 *         characters.
	 */
	public static String align(final String str) {
		return Utils.align(str, Utils.DEFAULT_ALIGNMENT_LENGTH);
	}

	/**
	 * @param str
	 * @param length
	 * @return a string that starts with <code>str</code> and is appended with
	 *         spaces to be at least of the specified length.
	 */
	public static String align(final String str, int length) {
		int curLength = 0;
		final StringBuilder builder = new StringBuilder(str);
		if (str != null) {
			curLength = str.length();
		}

		while (curLength < length) {
			if (length - curLength <= Utils.SPACES.length) {
				builder.append(Utils.SPACES[length - curLength - 1]);
				curLength = length;
			} else {
				builder.append(Utils.SPACES[Utils.SPACES.length - 1]);
				curLength += Utils.SPACES.length;
			}
		}

		return builder.toString();
	}

	/**
	 * shows an error message and exits.
	 * 
	 * @param message
	 */
	public static void fatalError(final String message) {
		System.err.println("ERROR: " + message);
		Utils.fatalError();
	}

	public static void fatalError() {
		Utils.printStackTraces();
		System.exit(1);
	}

	/**
	 * converts an array of elements of type U to an array of elements of type V.
	 * It calls {@link #convert(String, Class)} to do smart conversion into
	 * numbers.
	 * 
	 * @param <U>
	 *          the type of the elements of the original array.
	 * @param <V>
	 *          the type of the elements of the new array.
	 * @param original
	 *          the array of elements of type U.
	 * @param type
	 *          the class object that represents class V.
	 * @return the array of elements of type V.
	 */
	public static <U, V> V[] convert(final U[] original, final Class<V> type) {
		final V[] result = GenericReflection.newInstance(type, original.length);
		for (int i = 0; i < original.length; i++) {
			if (type.isInstance(original[i])) {
				result[i] = type.cast(original[i]);
			} else if (original[i] instanceof String) {
				result[i] = Utils.convert((String) original[i], type);
			} else {
				Utils.logger.error("Incompatible type in converting array of "
						+ type.getSimpleName() + " !");
				return null;
			}
		}

		return result;
	}

	/**
	 * converts a string to a value of type V that the string represents. If V
	 * extends {@link java.lang.Number}, it tries to parse the string and obtain a
	 * numeric value.
	 * 
	 * @param <V>
	 * @param str
	 * @param type
	 * @return the value of type V that the string represents.
	 */
	public static <V> V convert(final String str, final Class<V> type) {

		if (type.isInstance(str)) {
			return type.cast(str);
		} else {
			Method valueOf = null;
			try {
				valueOf = type.getDeclaredMethod("valueOf", String.class);
			} catch (final NoSuchMethodException e) {
				valueOf = null;
			}

			if (valueOf != null) {
				try {
					return type.cast(valueOf.invoke(type, str));
				} catch (final InvocationTargetException e) {
					Utils.logger.error(new NumberFormatException(str));
				} catch (final IllegalArgumentException e) {
					Utils.logger.error(e);
				} catch (final IllegalAccessException e) {
					Utils.logger.error(e);
				}
			} else {
				// No valueOf method, just return a null
				return null;
			}
			return null;
		}
	}

	/**
	 * obtains the stack trace of a given thread. This is useful in locating the
	 * cause of a deadlock.
	 * 
	 * @param thread
	 * 
	 * @return the string that represents the stack trace of the given thread.
	 */
	public static String getStackTrace(final Thread thread) {
		final StackTraceElement[] ste = thread.getStackTrace();
		String s = "";
		for (int i = 0; i < ste.length; i++) {
			s += ((i == 0) ? "" : "\t") + ste[i].toString() + "\n";
		}

		return s;
	}

	/**
	 * displays the stack traces of all threads active in the system.
	 */
	public static void printStackTraces() {
		Utils.printStackTraces(false);
	}

	public static void printStackTraces(boolean allThreads) {
		Utils.logger.info("Threads: " + Thread.activeCount() + ".");

		Utils.logger.info(Utils.getStackTrace(Thread.currentThread()) + "\n");

		if (allThreads) {
			final Thread[] threads = new Thread[Thread.activeCount()];
			Thread.currentThread().getThreadGroup().enumerate(threads);
			for (final Thread thread : threads) {
				if (thread != Thread.currentThread()) {
					Utils.logger.info(Utils.getStackTrace(thread) + "\n");
				}
			}
		}
	}

	/**
	 * generates a random number and displays it. This is useful in checking
	 * whether a game configured in the same way (including the same seed for the
	 * random number generator) will lead to the exactly same results across
	 * different runs.
	 * 
	 * @param tag
	 *          a string to indicate where an output is generated from.
	 */
	public static void randomTest(final String tag) {
		final Uniform uniform = new Uniform(0, 1, Galaxy.getInstance()
				.getDefaultTyped(GlobalPRNG.class).getEngine());

		Utils.logger.info("");
		Utils.logger.info("Test " + tag + ": " + uniform.nextDouble());
		Utils.logger.info("");
	}

	/**
	 * @param value
	 * @param length
	 * @return an array of the specified length that has the given value in each
	 *         entry, or null if length is not positive.
	 */
	public static double[] newDuplicateArray(final double value, final int length) {
		if (length > 0) {
			final double array[] = new double[length];
			Arrays.fill(array, value);
			return array;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param list
	 * @return an array of double values from the given list in the original
	 *         order;
	 */
	public static double[] toArray(final List<Double> list) {
		final double array[] = new double[list.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i).doubleValue();
		}

		return array;
	}
}
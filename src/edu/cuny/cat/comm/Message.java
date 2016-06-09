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

/**
 * <p>
 * An abstract class representing a plain-text message.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public abstract class Message {

	/**
	 * 
	 */
	public static String CRLF = "\r\n";

	/**
	 * the string separating items in a list.
	 */
	public static String VALUE_SEPARATOR = ",";

	/**
	 * @return the plain-text presentation of this message.
	 */
	@Override
	public abstract String toString();

	/**
	 * parses a list of plain-text words, separated by {@link #VALUE_SEPARATOR}.
	 * 
	 * @param s
	 *          a word list
	 * @return the list of words in an array
	 */
	public static String[] parseStrings(final String s) {
		if (s == null) {
			return null;
		} else if (s.length() == 0) {
			return new String[0];
		} else {
			// try to match as many times as possible
			return s.split(Message.VALUE_SEPARATOR, -1);
		}
	}

	/**
	 * parses a <code>String</code> into a list of doubles.
	 * 
	 * @param s
	 *          a double list
	 * @return the list of doubles in an array
	 * @throws MessageException
	 *           if an entry in the list fails to be parsed into double.
	 * 
	 * @see #parseStrings(String)
	 */
	public static double[] parseDoubles(final String s) throws MessageException {
		try {
			final String texts[] = Message.parseStrings(s);
			final double numbers[] = new double[texts.length];
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = Double.parseDouble(texts[i]);
			}
			return numbers;
		} catch (final NumberFormatException e) {
			e.printStackTrace();
			throw new MessageException(e.toString());
		}
	}

	/**
	 * parses a {@link String} into a list of integers.
	 * 
	 * @param s
	 *          an integer list
	 * @return the list of integers in an array
	 * @throws CatException
	 *           if an entry in the list fails to be parsed into integer.
	 * 
	 * @see #parseStrings(String)
	 */
	public static int[] parseIntegers(final String s) throws MessageException {
		try {
			final String texts[] = Message.parseStrings(s);
			final int numbers[] = new int[texts.length];
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = Integer.parseInt(texts[i]);
			}
			return numbers;
		} catch (final NumberFormatException e) {
			e.printStackTrace();
			throw new MessageException(e.toString());
		}
	}

	/**
	 * concatenates an array of strings with the default {@link #VALUE_SEPARATOR},
	 * and return its string representation.
	 * 
	 * @see #concatenate(String[], String)
	 */
	public static String concatenate(final String texts[]) {
		return Message.concatenate(texts, Message.VALUE_SEPARATOR);
	}

	/**
	 * concatenates an array of strings with the separator, and return its string
	 * representation.
	 * 
	 * Null strings are overlooked, while empty string is considered as valid
	 * item.
	 * 
	 * @param texts
	 *          the array of strings.
	 * @param separator
	 *          the string to separate the strings.
	 * 
	 * @return a {@link String} instance for this representation.
	 */
	public static String concatenate(final String texts[], final String separator) {
		String s = null;
		for (final String text : texts) {
			if (text == null) {
				continue;
			} else {
				if (s == null) {
					s = text;
				} else {
					s += separator + text;
				}
			}
		}

		if (s == null) {
			s = "";
		}

		return s;
	}

	/**
	 * concatenates the string representations of doubles in an array.
	 * 
	 * @param numbers
	 *          an array of doubles
	 * @return a {@link String} instance for this representation.
	 * 
	 * @see #concatenate(String[])
	 */
	public static String concatenate(final double numbers[]) {
		final String texts[] = new String[numbers.length];
		for (int i = 0; i < numbers.length; i++) {
			texts[i] = String.valueOf(numbers[i]);
		}
		return Message.concatenate(texts);

	}

	/**
	 * concatenates the string representations of integers in an array.
	 * 
	 * @param numbers
	 *          an array of intergers
	 * @return a {@link String} instance for this representation.
	 * 
	 * @see #concatenate(String[])
	 */
	public static String concatenate(final int numbers[]) {
		final String texts[] = new String[numbers.length];
		for (int i = 0; i < numbers.length; i++) {
			texts[i] = String.valueOf(numbers[i]);
		}
		return Message.concatenate(texts);
	}

}

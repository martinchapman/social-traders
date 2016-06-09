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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * provides constant values used in catp messages and utility functions helpful
 * to compose a catp message. Fore more details, please refer to the cat
 * protocol specification.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.35 $
 */

public abstract class CatpMessage extends Message {

	/**
	 * catp version information.
	 */
	public static String CURRENT_VERSION = "CATP/2.0";

	/**
	 * the string separating the name of a header field from its value.
	 */
	public static String HEADER_SEPARATOR = ":";

	/*
	 * request types
	 */

	/**
	 * CHECKIN request type
	 */
	public final static String CHECKIN = "CHECKIN";

	/**
	 * OPTIONS request type
	 */
	public final static String OPTIONS = "OPTIONS";

	/**
	 * POST request type
	 */
	public final static String POST = "POST";

	/**
	 * GET request type
	 */
	public final static String GET = "GET";

	/**
	 * REGISTER request type
	 */
	public final static String REGISTER = "REGISTER";

	/**
	 * SUBSCRIBE request type
	 */
	public final static String SUBSCRIBE = "SUBSCRIBE";

	/**
	 * ASK request type
	 */
	public final static String ASK = "ASK";

	/**
	 * BID request type
	 */
	public final static String BID = "BID";

	/**
	 * TRANSACTION request type
	 * <p>
	 * Also used as an identifier telling the context is about a transaction.
	 */
	public final static String TRANSACTION = "TRANSACTION";

	/*
	 * response status codes
	 */

	/**
	 * OK response status code
	 */
	public final static String OK = "OK";

	/**
	 * INVALID response status code
	 */
	public final static String INVALID = "INVALID";

	/**
	 * ERROR response status code
	 */
	public final static String ERROR = "ERROR";

	/*
	 * header names
	 */

	/**
	 * ID header name.
	 */
	public final static String ID = "ID";

	/**
	 * TYPE header name.
	 */
	public final static String TYPE = "TYPE";

	/**
	 * VALUE header name.
	 */
	public final static String VALUE = "VALUE";

	/**
	 * TEXT header name.
	 */
	public final static String TEXT = "TEXT";

	/**
	 * VERSION header name.
	 */
	public final static String VERSION = "VERSION";

	/**
	 * TAG header name.
	 */
	public final static String TAG = "TAG";

	/**
	 * TIME header name.
	 */
	public final static String TIME = "TIME";

	/*
	 * header values
	 */

	/**
	 * a header field value, telling a game is starting.
	 */
	public final static String GAMESTARTING = "GAMESTARTING";

	/**
	 * a header field value, telling a game is started.
	 */
	public final static String GAMESTARTED = "GAMESTARTED";

	/**
	 * a header field value, telling a game is over.
	 */
	public final static String GAMEOVER = "GAMEOVER";

	/**
	 * a header field value, telling a day is opening.
	 */
	public final static String DAYOPENING = "DAYOPENING";

	/**
	 * a header field value, telling a day is opened.
	 */
	public final static String DAYOPENED = "DAYOPENED";

	/**
	 * a header field value, telling a day is closed.
	 */
	public final static String DAYCLOSED = "DAYCLOSED";

	/**
	 * a header field value, telling a round is opening.
	 */
	public final static String ROUNDOPENING = "ROUNDOPENING";

	/**
	 * a header field value, telling a round is opened.
	 */
	public final static String ROUNDOPENED = "ROUNDOPENED";

	/**
	 * a header field value, telling a round is closing.
	 */
	public final static String ROUNDCLOSING = "ROUNDCLOSING";

	/**
	 * a header field value, telling a round is closed.
	 */
	public final static String ROUNDCLOSED = "ROUNDCLOSED";

	/**
	 * a header field value, telling the involved entity is a specialist.
	 */
	public final static String SPECIALIST = "SPECIALIST";

	/**
	 * a header field value, telling the involved entity is a trader.
	 */
	public final static String TRADER = "TRADER";

	/**
	 * a header field value, telling the involved entity is a buyer.
	 */
	public final static String BUYER = "BUYER";

	/**
	 * a header field value, telling the involved entity is a seller.
	 */
	public final static String SELLER = "SELLER";

	/**
	 * a header field value, telling it is concerning market charging information.
	 */
	public final static String FEE = "FEE";

	/**
	 * a header field value, telling it is concerning profit of traders or
	 * specialists.
	 * <p>
	 * Also used as an identifier telling the context is about a shout.
	 */
	public final static String PROFIT = "PROFIT";

	/**
	 * a header field value used in a response message, telling the corresponding
	 * request arrives at a wrong time.
	 */
	public final static String WRONGTIME = "WRONGTIME";

	/*
	 * others
	 */

	/**
	 * an identifier telling the context is about a shout.
	 */
	public final static String SHOUT = "SHOUT";

	/**
	 * an identifier telling the context is about market information subscription.
	 */
	public final static String INFORMATION = "INFORMATION";

	/**
	 * an identifier telling the context is about trader's registration with a
	 * market.
	 */
	public final static String REGISTRATION = "REGISTRATION";

	/**
	 * a constant telling the context is about a catp client.
	 */
	public final static String CLIENT = "CLIENT";

	/**
	 * stores header fields parsed out from the message
	 */
	protected Map<String, String> headers = new HashMap<String, String>();

	/**
	 * the first line of the message
	 */
	protected String startLine;

	/**
	 * gets the starting line of this message.
	 * 
	 * @return the starting line.
	 */
	public String getStartLine() {
		return startLine;
	}

	/**
	 * sets the starting line of this message.
	 * 
	 * @param line
	 *          the string to be used as the starting line.
	 */
	public void setStartLine(final String line) {
		startLine = line;
	}

	/**
	 * @return a set of the header fields' names in this message.
	 */
	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	/**
	 * retrieves the value of a header field.
	 * 
	 * @param name
	 *          the header field's name.
	 * @return the value of the header field.
	 */
	public String getHeader(final String name) {
		return headers.get(name);
	}

	/**
	 * retrieves the value of a header field as integer.
	 * 
	 * @param name
	 *          the header field's name.
	 * @return the integer value of the header field.
	 * @throws CatpMessageErrorException
	 *           if the value cannot be parsed as integer.
	 */
	public int getIntHeader(final String name) throws CatpMessageErrorException {
		try {
			if (getHeader(name) == null) {
				throw new NumberFormatException("No value given");
			}
			return Integer.parseInt(getHeader(name));
		} catch (final NumberFormatException e) {
			throw new CatpMessageErrorException(e.toString());
		}
	}

	/**
	 * retrieves the value of a header field as double.
	 * 
	 * @param name
	 *          the header field's name.
	 * @return the double value of the header field.
	 * @throws CatpMessageErrorException
	 *           if the value cannot be parsed as double.
	 */
	public double getDoubleHeader(final String name)
			throws CatpMessageErrorException {
		try {
			if (getHeader(name) == null) {
				throw new NumberFormatException("No value given");
			}
			return Double.parseDouble(getHeader(name));
		} catch (final NumberFormatException e) {
			throw new CatpMessageErrorException(e.toString());
		}
	}

	/**
	 * appends a value to a message header field's value list, or sets it if it
	 * does not exist.
	 * 
	 * @param name
	 *          header name
	 * @param header
	 *          header value
	 */
	public void addHeader(final String name, final String header) {
		if (headers.containsKey(name)) {
			final String oldHeader = getHeader(name);
			if (oldHeader.endsWith(",")) {
				headers.put(name, oldHeader + header);
			} else {
				headers.put(name, oldHeader + "," + header);
			}
		} else {
			headers.put(name, header);
		}
	}

	/**
	 * sets a message header field.
	 * 
	 * @param name
	 *          header name
	 * @param header
	 *          header value
	 */
	public void setHeader(final String name, final String header) {
		headers.put(name, header);
	}

	/**
	 * sets message fields in a batch mode.
	 * 
	 * @param pairs
	 *          an array of odd length, in the format: <i>field1, value1, field2,
	 *          value2, ...</i>
	 */
	public void setHeaders(final String pairs[]) {

		if (pairs != null) {
			final int n = pairs.length / 2;
			for (int i = 0; i < n; i++) {
				setHeader(pairs[i * 2], pairs[i * 2 + 1]);
			}
		}
	}

	/**
	 * sets a string as the value of the {@link #TAG} field in the message.
	 * 
	 * @param tag
	 */
	public void setTag(final String tag) {
		setHeader(CatpMessage.TAG, tag);
	}

	/**
	 * sets an integer as the value of the {@link #TAG} field in the message.
	 * 
	 * @param tag
	 */
	public void setTag(final int tag) {
		setTag(String.valueOf(tag));
	}

	/**
	 * @return the value of the {@link #TAG} field in the message.
	 */
	public String getTag() {
		return getHeader(CatpMessage.TAG);
	}

	/**
	 * @return the plain-text representation of this catp message.
	 */
	@Override
	public String toString() {
		String s = startLine + Message.CRLF;
		for (final String name : getHeaderNames()) {
			final String header = getHeader(name);
			s += name + CatpMessage.HEADER_SEPARATOR + " " + header + Message.CRLF;
		}

		s += Message.CRLF;

		return s;
	}
}

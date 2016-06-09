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
 * represents a catp request message.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class CatpRequest extends CatpMessage {

	/**
	 * the entity containing information on why this request is made
	 */
	private Object trigger;

	/**
	 * type identifier of this request - the first word in the starting line.
	 */
	private String type;

	/**
	 * @return the type of this request message.
	 */
	public String getType() {
		if (type == null) {
			final String segments[] = getStartLine().split("\\s");
			if ((segments != null) && (segments.length >= 1)) {
				type = segments[0];
			}
		}

		return type;
	}

	/**
	 * @return the trigger object that causes this request to occur
	 */
	public Object getTrigger() {
		return trigger;
	}

	/**
	 * @param trigger
	 *          tells how this request is caused
	 */
	public void setTrigger(final Object trigger) {
		this.trigger = trigger;
	}

	/**
	 * constructs a catp request message containing only a starting line.
	 * 
	 * @param startLine
	 *          the starting line.
	 * @return an instance of <code>CatpRequest</code>
	 */
	public static CatpRequest createRequest(final String startLine) {
		return CatpRequest.createRequest(startLine, null);
	}

	/**
	 * constructs a catp request message containing a starting line and a list of
	 * header fields.
	 * 
	 * @param startLine
	 *          the starting line.
	 * @param pairs
	 *          the header field name-value pairs.
	 * @return an instance of <code>CatpRequest</code>
	 */
	public static CatpRequest createRequest(final String startLine,
			final String pairs[]) {
		final CatpRequest req = new CatpRequest();
		req.setStartLine(startLine);
		req.setHeaders(pairs);
		return req;
	}
}

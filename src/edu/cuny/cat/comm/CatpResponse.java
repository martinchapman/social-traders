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
 * represents a catp response message.
 * </p>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class CatpResponse extends CatpMessage {

	/**
	 * status code of this response - the first word in the starting line.
	 */
	private String statusCode;

	/**
	 * @return the status code in the response.
	 */
	public String getStatusCode() {
		if (statusCode == null) {
			final String segments[] = getStartLine().split("\\s");
			if ((segments != null) && (segments.length >= 1)) {
				statusCode = segments[0];
			}
		}

		return statusCode;
	}

	/**
	 * constructs a catp response message containing only a starting line.
	 * 
	 * @param startLine
	 *          the starting line.
	 * @return an instance of <code>CatpResponse</code>
	 */
	public static CatpResponse createResponse(final String startLine) {
		return CatpResponse.createResponse(startLine, null);
	}

	/**
	 * constructs a catp response message containing a starting line and a list of
	 * header fields.
	 * 
	 * @param startLine
	 *          the starting line.
	 * @param pairs
	 *          the header field name-value pairs.
	 * @return an instance of <code>CatpResponse</code>
	 */
	public static CatpResponse createResponse(final String startLine,
			final String pairs[]) {
		final CatpResponse resp = new CatpResponse();
		resp.setStartLine(startLine);
		resp.setHeaders(pairs);
		return resp;
	}

}

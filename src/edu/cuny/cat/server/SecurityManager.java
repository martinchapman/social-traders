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

package edu.cuny.cat.server;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * manages security issues in game server.
 * </p>
 * 
 * <p>
 * It detects malicious clients by examining the value of the <code>Type</code>
 * header in the <code>CHECKIN</code> request from a client. If it contains a
 * specified security token, the client is considered valid, otherwise invalid.
 * Traders and specialists have different tokens.
 * </p>
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.tradertoken</tt><br>
 * <font size=-1>String (<code>null</code> by default)</font></td>
 * <td valign=top>(token for traders)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.specialisttoken</tt><br>
 * <font size=-1>String (<code>null</code> by default)</font></td>
 * <td valign=top>(token for specialists)</td>
 * <tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>security</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class SecurityManager implements Parameterizable {

	public static final String P_TRADERTOKEN = "tradertoken";

	public static final String P_SPECIALISTTOKEN = "specialisttoken";

	public static final String P_DEF_BASE = "security";

	static final Logger logger = Logger.getLogger(SecurityManager.class);

	protected String traderToken = null;

	protected String specialistToken = null;

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		final Parameter defBase = new Parameter(SecurityManager.P_DEF_BASE);
		traderToken = parameters.getStringWithDefault(base
				.push(SecurityManager.P_TRADERTOKEN), defBase
				.push(SecurityManager.P_TRADERTOKEN), traderToken);
		specialistToken = parameters.getStringWithDefault(base
				.push(SecurityManager.P_SPECIALISTTOKEN), defBase
				.push(SecurityManager.P_SPECIALISTTOKEN), specialistToken);
	}

	/**
	 * checks if the given type string contains the valid security token.
	 * 
	 * @param isTrader
	 *          whether the client to be checked up is a trader or not
	 * @param type
	 *          the type string of the client
	 * @return true if the desired security token is found; false otherwise
	 */
	public boolean isAuthorizedClient(final boolean isTrader, final String type) {
		String token = null;
		if (isTrader) {
			token = traderToken;
		} else {
			token = specialistToken;
		}

		if ((token != null) && (token.length() != 0)) {
			return type.contains(token);
		} else {
			return true;
		}
	}

	public String getToken(final boolean isTrader) {
		if (isTrader) {
			return traderToken;
		} else {
			return specialistToken;
		}
	}

	/**
	 * removes the security token in a string, which otherwise may be exposed to
	 * outsiders.
	 * 
	 * @param isTrader
	 * @param text
	 * 
	 * @return a string from which security token is removed.
	 */
	public String removeToken(final boolean isTrader, final String text) {
		final String token = getToken(isTrader);
		if (token == null) {
			return text;
		} else {
			return text.replace(token, "");
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " traderToken:" + traderToken
				+ " specialistToken:" + specialistToken;
	}
}

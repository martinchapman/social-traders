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

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.registry.Registry;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * <p>
 * The class managing the identities of clients.
 * </p>
 * 
 * <p>
 * Currently, this manages only a list of expected specialist names.
 * </p>
 * 
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base.expected_specialist</i><tt>.n</tt><br>
 * <font size=-1>int >= 0 (0 by default)</font></td>
 * <td valign=top>(number of specialists expected to participate in the game)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base.expected_specialist</i><tt>.</tt><i>n</i><br>
 * <font size=-1>String</font></td>
 * <td valign=top>(the name of the <i>n</i>th expected specialist)</td>
 * <tr>
 * 
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b>
 * </p>
 * <table>
 * <tr>
 * <td valign=top><tt>identity_controller</tt></td>
 * </tr>
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.4 $
 */

public class ClientIdentityController implements Parameterizable {

	public static final String P_DEF_BASE = "identity_controller";

	public static final String P_EXPECTED_SPECIALIST = "expected_specialist";

	public static final String P_NUM = "n";

	static Logger logger = Logger.getLogger(ClientIdentityController.class);

	public void setup(final ParameterDatabase parameters, Parameter base) {

		final Registry registry = GameController.getInstance().getRegistry();

		base = base.push(ClientIdentityController.P_EXPECTED_SPECIALIST);

		final int num = parameters.getIntWithDefault(base
				.push(ClientIdentityController.P_NUM), null, 0);

		for (int i = 0; i < num; i++) {
			final String id = parameters.getString(base.push(i + ""), null);

			if (registry.getExpectedSpecialist(id) != null) {
				ClientIdentityController.logger
						.error("Duplicate expected specialist id: " + id + " !");
			} else {
				registry.addExpectedSpecialist(new Specialist(id));
			}
		}
	}
}

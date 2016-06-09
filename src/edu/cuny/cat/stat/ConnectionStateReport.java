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
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.cat.stat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Specialist;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.SpecialistCheckInEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.ClientState;
import edu.cuny.cat.server.GameController;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;
import edu.cuny.util.io.CSVWriter;

/**
 * <p>
 * A report that records the states of specialists in a CSV file based on
 * {@link edu.cuny.util.io.CSVWriter}.
 * </p>
 * 
 * @see edu.cuny.cat.server.ClientState
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class ConnectionStateReport implements GameReport, Parameterizable {

	static Logger logger = Logger.getLogger(ConnectionStateReport.class);

	static String[] HEADERS = { "name", "state" };

	protected CSVWriter log = null;

	protected Map<String, String> stateMap;

	protected Registry registry;

	public ConnectionStateReport() {
		stateMap = Collections.synchronizedMap(new HashMap<String, String>());
		registry = GameController.getInstance().getRegistry();
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		log = new CSVWriter();
		log.setAutowrap(false);
		log.setup(parameters, base);
		log.setAppend(false);
	}

	protected void generateHeader() {

		for (final String element : ConnectionStateReport.HEADERS) {
			log.newData(element);
		}

		log.endRecord();
	}

	protected synchronized void generateFile() {
		log.open();

		generateHeader();

		final String specialistIds[] = registry.getSpecialistIds();

		for (final String specialistId : specialistIds) {

			log.newData(specialistId);

			/* TODO: a quick fix for unknown expected specialists */
			if (stateMap.containsKey(specialistId)) {
				log.newData(stateMap.get(specialistId));
			} else {
				stateMap
						.put(specialistId, ClientState.getCodeDesc(ClientState.UNKNOWN));
			}

			log.endRecord();
		}

		log.flush();
		log.close();
	}

	public void eventOccurred(final AuctionEvent event) {

		if (event instanceof ClientStateUpdatedEvent) {
			final ClientStateUpdatedEvent csuEvent = (ClientStateUpdatedEvent) event;
			if (csuEvent.getClient() instanceof Specialist) {
				if (csuEvent.getPreviousState().getCode() != csuEvent.getCurrentState()
						.getCode()) {
					// regenerate file when a specialist changes state

					final Specialist specialist = (Specialist) csuEvent.getClient();
					stateMap.put(specialist.getId(), ClientState.getCodeDesc(csuEvent
							.getCurrentState().getCode()));

					generateFile();
				}
			}
		} else if (event instanceof SpecialistCheckInEvent) {
			final Specialist specialist = ((SpecialistCheckInEvent) event)
					.getSpecialist();
			stateMap.put(specialist.getId(), ClientState.getCodeDesc(ClientState.OK));
			generateFile();
		}
	}

	public void produceUserOutput() {
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
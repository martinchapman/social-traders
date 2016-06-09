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

package edu.cuny.cat.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.ClientStateUpdatedEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpenedEvent;
import edu.cuny.cat.event.RoundClosedEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.SpecialistCheckInEvent;
import edu.cuny.cat.event.TraderCheckInEvent;
import edu.cuny.cat.registry.Registry;
import edu.cuny.cat.server.GameController;

/**
 * displays in a <code>JLable</code> the number of specialists and trading
 * agents that have connected to the cat game server.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */
public class PlayerLabelPanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(PlayerLabelPanel.class);

	JLabel playerLabel;

	Registry registry;

	public PlayerLabelPanel() {

		registry = GameController.getInstance().getRegistry();

		playerLabel = new JLabel();
		playerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(playerLabel, BorderLayout.CENTER);

		updatePlayerLabel();

		setTitledBorder("Players");

	}

	protected void updatePlayerLabel() {
		playerLabel.setText(registry.getClientStatInfo().replaceAll("\t",
				"             "));
	}

	@Override
	protected synchronized void processTraderCheckIn(
			final TraderCheckInEvent event) {
		updatePlayerLabel();
	}

	@Override
	protected synchronized void processSpecialistCheckIn(
			final SpecialistCheckInEvent event) {
		updatePlayerLabel();
	}

	@Override
	protected void processDayOpened(final DayOpenedEvent event) {
		updatePlayerLabel();
	}

	@Override
	protected void processDayClosed(final DayClosedEvent event) {
		updatePlayerLabel();
	}

	@Override
	protected void processRoundOpened(final RoundOpenedEvent event) {
		updatePlayerLabel();
	}

	@Override
	protected void processRoundClosed(final RoundClosedEvent event) {
		updatePlayerLabel();
	}

	@Override
	protected void processClientStatusUpdated(final ClientStateUpdatedEvent event) {
		updatePlayerLabel();
	}
}

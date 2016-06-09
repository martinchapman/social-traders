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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JButton;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.SpecialistCheckInEvent;

/**
 * displays a grid of buttons, each for a specialist. Clicking a button will
 * open a {@link GameView} dedicated for the specialist.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.11 $
 */
public class SpecialistGridPanel extends ViewPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(SpecialistGridPanel.class);

	public SpecialistGridPanel() {
		setTitledBorder("Specialists");

		setLayout(new GridLayout(0, 3, 5, 5));
	}

	private void addSpecialistButton(final String specialistId) {
		final JButton button = new JButton(specialistId);
		add(button);
		button.addActionListener(this);
	}

	@Override
	protected void processSpecialistCheckIn(final SpecialistCheckInEvent event) {
		addSpecialistButton(event.getSpecialist().getId());
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			final String specialistId = e.getActionCommand();
			SpecialistView view = (SpecialistView) GuiConsole.getInstance().getView(
					specialistId);
			if (view == null) {
				view = new SpecialistView(specialistId);
				GuiConsole.getInstance().addView(specialistId, view);
			}
			if (view.isIcon()) {
				try {
					view.setIcon(false);
				} catch (final PropertyVetoException e1) {
					e1.printStackTrace();
					SpecialistGridPanel.logger.error(e1);
				}
			}
			view.moveToFront();
		}
	}

}

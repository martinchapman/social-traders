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
import java.beans.PropertyVetoException;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * <p>
 * The main display in the cat game console. It may be configured to include
 * multiple panels. Each panel may be used to control the game in some way or
 * show certain information of the game.
 * </p>
 * 
 * @see GuiConsole
 * @see ViewPanel
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.14 $
 */
public class OverView extends GameView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(OverView.class);

	CombiViewPanel viewPanel;

	public OverView() {
		super("OverView");
		setClosable(false);
		viewPanel = new CombiViewPanel();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(viewPanel, BorderLayout.CENTER);
	}

	@Override
	public void setup(final ParameterDatabase parameters, final Parameter base) {
		super.setup(parameters, base);

		turnOffBorder();

		viewPanel.setup(parameters, base);
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		viewPanel.eventOccurred(event);
	}

	private void turnOffBorder() {
		try {
			setMaximum(true);
		} catch (final PropertyVetoException e) {
			e.printStackTrace();
		}

		// // remove title bar
		// ((javax.swing.plaf.basic.BasicInternalFrameUI)
		// getUI()).setNorthPane(null);
		// setBorder(null);
	}
}

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
 * JAF - Java Application Framework
 * Copyright (C) 1999-2006 Jinzhong Niu
 */

package edu.cuny.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A {@link edu.cuny.util.Parameterizable} {@link javax.swing.JFrame}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class UserFrame extends JFrame implements Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		UiUtils.setup(this, parameters, base);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				exit();
			}
		});
	}

	/**
	 * called when the window is being closed.
	 */
	public void exit() {
		setVisible(false);
	}
}
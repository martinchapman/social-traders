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

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JWindow;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A {@link edu.cuny.util.Parameterizable} {@link javax.swing.JWindow} that
 * works as a splash window during a program launches.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class SplashWindow extends JWindow implements Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SplashWindow(final Frame parent) {
		super(parent);
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		UiUtils.setup(this, parameters, base);

		final UserLabel label = new UserLabel();
		getContentPane().add(BorderLayout.CENTER, label);
		label.setup(parameters, base);

		pack();
	}

}
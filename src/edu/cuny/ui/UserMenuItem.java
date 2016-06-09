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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.util.Galaxy;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A {@link edu.cuny.util.Parameterizable} {@link javax.swing.JMenuItem}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */

public class UserMenuItem extends JMenuItem implements Parameterizable,
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_PARAM = "param";

	/**
	 * The parameter <code>param</code> is a string indicating action command for
	 * this menu component. The preceeding '*' indicates the component is disabled
	 * at the very beginning. Then ~X~ may follow defining the shortcut key, where
	 * X maybe A .. Z or < for left arrow or > for right arrow. The rest of <code>
	 * param</code>
	 * is the action command of this componenet, based on which the listener may
	 * determine which component is pressed.
	 */

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		UiUtils.setup(this, parameters, base);

		boolean flag = true;
		final String param = parameters.getStringWithDefault(base
				.push(UserMenuItem.P_PARAM), null, null);

		if (param != null) {

			int i = 0;
			if (param.charAt(i) == '*') {
				flag = false;
				i++;
			}
			setEnabled(flag);

			if (param.charAt(i) == '^') {
				final char mnemonic = param.charAt(++i);
				setMnemonic(mnemonic);
				i += 2;
			}

			if (param.charAt(i) == '~') {
				final char c = param.charAt(++i);
				int keycode = c;
				if (c == '<') {
					keycode = KeyEvent.VK_LEFT;
				} else if (c == '>') {
					keycode = KeyEvent.VK_RIGHT;
				}
				i += 2;

				setAccelerator(KeyStroke.getKeyStroke(keycode, InputEvent.CTRL_MASK));
			}
		}

		setActionCommand(getName());

		addActionListener(this);

	}

	public void actionPerformed(final ActionEvent event) {
		Galaxy.getInstance().getDefaultTyped(EventEngine.class).dispatchEvent(
				getClass(), new Event(this, event));
	}

}

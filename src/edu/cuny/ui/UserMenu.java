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

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A {@link edu.cuny.util.Parameterizable} {@link javax.swing.JMenu}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class UserMenu extends JMenu implements Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_PARAM = "param";

	public static final String P_ITEM = "item";

	public static final String P_NUM = "n";

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		UiUtils.setup(this, parameters, base);

		final String param = parameters.getStringWithDefault(base
				.push(UserMenu.P_PARAM), null, null);
		if (param != null) {
			int i = 0;
			if (param.charAt(i) == '^') {
				final char mnemonic = param.charAt(++i);
				setMnemonic(mnemonic);
				i += 2;
			}
		}

		final int n = parameters.getIntWithDefault(base.push(UserMenu.P_ITEM).push(
				UserMenu.P_NUM), null, 0);
		final Parameter itemBase = base.push(UserMenu.P_ITEM);
		for (int i = 0; i < n; i++) {
			final String flag = parameters.getString(
					itemBase.push(String.valueOf(i)), null);
			if ("@".equals(flag)) {
				final UserMenu menu = new UserMenu();
				add(menu);
				menu.setup(parameters, itemBase.push(String.valueOf(i)));
			} else if ("-".equals(flag)) {
				addSeparator();
			} else if ("?".equals(flag)) {
				final UserCheckboxMenuItem menuItem = new UserCheckboxMenuItem();
				add(menuItem);
				menuItem.setup(parameters, itemBase.push(String.valueOf(i)));
			} else {
				final UserMenuItem menuItem = new UserMenuItem();
				add(menuItem);
				menuItem.setup(parameters, itemBase.push(String.valueOf(i)));
			}
		}

	}

	public JMenuItem getItem(final String s) {
		return getItem(s, true);
	}

	public JMenuItem getItem(final String s, final boolean flag) {
		final int i = getItemCount();
		for (int j = 0; j < i; j++) {
			JMenuItem menuitem = getItem(j);
			if (menuitem instanceof UserMenuItem) {
				if (((UserMenuItem) menuitem).getName().equals(s)) {
					return menuitem;
				}
			} else if (menuitem instanceof UserCheckboxMenuItem) {
				if (((UserCheckboxMenuItem) menuitem).getName().equals(s)) {
					return menuitem;
				}
			} else if (menuitem instanceof UserMenu) {
				if (menuitem.getName().equals(s)) {
					return menuitem;
				}
				if (flag) {
					menuitem = ((UserMenu) menuitem).getItem(s);
					if (menuitem != null) {
						return menuitem;
					}
				}
			}
		}

		return null;
	}

	public void removeFirstSeparator() {
		final int i = getItemCount();
		for (int j = 0; j < i; j++) {
			final JMenuItem menuitem = getItem(j);
			if (menuitem.getText().equals("-")) {
				remove(j);
				return;
			}
		}

	}

	public int getItemPosition(final String s) {
		final int i = getItemCount();
		for (int j = 0; j < i; j++) {
			final JMenuItem menuitem = getItem(j);
			if (menuitem.getName().equals(s)) {
				return j;
			}
		}

		return -1;
	}
}

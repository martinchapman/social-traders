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
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A {@link edu.cuny.util.Parameterizable} {@link javax.swing.JMenuBar}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class UserMenuBar extends JMenuBar implements Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_MENU = "menu";

	public static final String P_NUM = "n";

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		UiUtils.setup(this, parameters, base);

		final int n = parameters.getIntWithDefault(base.push(UserMenuBar.P_MENU)
				.push(UserMenuBar.P_NUM), null, 0);
		final Parameter menuBase = base.push(UserMenuBar.P_MENU);
		for (int i = 0; i < n; i++) {
			final UserMenu menu = new UserMenu();
			add(menu);
			menu.setup(parameters, menuBase.push(String.valueOf(i)));
			final String flag = parameters.getString(
					menuBase.push(String.valueOf(i)), null);

			// '?' is used to indicate a HELP menu
			if ("?".equals(flag)) {

				// Not yet implemented up to JDK1.5.0
				// setHelpMenu(usermenu);

			}
		}

	}

	public UserMenu getMenu(final String s) {
		final int i = getMenuCount();
		for (int j = 0; j < i; j++) {
			final UserMenu usermenu = (UserMenu) getMenu(j);
			if (usermenu.getName().equals(s)) {
				return usermenu;
			}
		}

		return null;
	}

	public UserMenu getSubMenu(final JMenu menu, final String s) {
		final int i = menu.getItemCount();
		for (int j = 0; j < i; j++) {
			final JMenuItem menuitem = menu.getItem(j);
			if (menuitem instanceof UserMenu) {
				final UserMenu usermenu = (UserMenu) menuitem;
				if (usermenu.getName().equals(s)) {
					return usermenu;
				}
			}
		}

		return null;
	}

	public JMenuItem getMenuItem(final String s) {
		final int i = getMenuCount();
		for (int j = 0; j < i; j++) {
			final UserMenu usermenu = (UserMenu) getMenu(j);
			final JMenuItem menuitem = usermenu.getItem(s);
			if (menuitem != null) {
				return menuitem;
			}
		}

		return null;
	}
}

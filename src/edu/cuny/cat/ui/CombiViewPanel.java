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

import javax.swing.BoxLayout;

import org.apache.log4j.Logger;

import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.util.ParamClassLoadException;
import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * A special {@link ViewPanel} that may combine several panels into one
 * horizontally or vertically in a <code>BoxLayout</code>.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.orientation</tt><br>
 * <font size=-1><code>'x'</code> or <code>'y'</code></font></td>
 * <td valign=top>(the direction to place the panels)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.panel.n</tt><br>
 * <font size=-1>int >= 0</font></td>
 * <td valign=top>(the number of panels to combine)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.panel.<i>n</i></tt><br>
 * <font size=-1></font></td>
 * <td valign=top>(the parameter base for the <i>n</i>th panel)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */
public class CombiViewPanel extends ViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(CombiViewPanel.class);

	static String P_NUM = "n";

	static String P_PANEL = "panel";

	static String P_ORIENTATION = "orientation";

	ViewPanel panels[];

	public CombiViewPanel() {
	}

	@Override
	public void setup(final ParameterDatabase parameters, Parameter base) {
		final String orient = parameters.getStringWithDefault(base
				.push(CombiViewPanel.P_ORIENTATION), null, "x");
		if ("x".equalsIgnoreCase(orient)) {
			setOrientation(BoxLayout.X_AXIS);
		} else {
			setOrientation(BoxLayout.Y_AXIS);
		}

		base = base.push(CombiViewPanel.P_PANEL);
		final int n = parameters.getIntWithDefault(base.push(CombiViewPanel.P_NUM),
				null, 0);
		panels = new ViewPanel[n];

		Class<?> type = null;
		for (int i = 0; i < n; i++) {
			try {
				type = parameters.getClassForParameter(base.push(String.valueOf(i)),
						null, ViewPanel.class);
			} catch (final ParamClassLoadException e) {
				type = CombiViewPanel.class;
			}
			try {
				panels[i] = (ViewPanel) type.newInstance();
				add(panels[i]);
				panels[i].setup(parameters, base.push(String.valueOf(i)));
			} catch (final InstantiationException e) {
				e.printStackTrace();
				CombiViewPanel.logger.error(e);
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
				CombiViewPanel.logger.error(e);
			}
		}
	}

	private void setOrientation(final int axis) {
		setLayout(new BoxLayout(this, axis));
	}

	@Override
	public void eventOccurred(final AuctionEvent event) {
		for (final ViewPanel panel : panels) {
			panel.eventOccurred(event);
		}
	}
}

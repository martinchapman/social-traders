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

import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A {@link edu.cuny.util.Parameterizable} {@link javax.swing.JLabel}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class UserLabel extends JLabel implements Parameterizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String P_ALIGN = "align";

	public static final String P_CHARS = "chars";

	public void setup(final ParameterDatabase parameters, final Parameter base) {

		UiUtils.setup(this, parameters, base);
		chars = parameters.getInt(base.push(UserLabel.P_CHARS), null, 0);
		setHorizontalAlignment(getAlign(parameters.getString(base
				.push(UserLabel.P_ALIGN), null)));
	}

	private int getAlign(final String s) {
		if ("right".equalsIgnoreCase(s)) {
			return SwingConstants.RIGHT;
		} else if ("left".equalsIgnoreCase(s)) {
			return SwingConstants.CENTER;
		} else {
			return SwingConstants.LEFT;
		}
	}

	@Override
	public Dimension getMinimumSize() {
		final Dimension dimension = super.getMinimumSize();
		if (chars == 0) {
			return dimension;
		} else {
			final FontMetrics fontmetrics = getFontMetrics(getFont());
			dimension.width = fontmetrics.stringWidth("0") * chars;
			return dimension;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (chars == 0) {
			return super.getPreferredSize();
		} else {
			return getMinimumSize();
		}
	}

	int chars;

}

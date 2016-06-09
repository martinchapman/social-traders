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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;

/**
 * Implements a panel with a line. The line may be at the TOP, BOTTOM, LEFT, or
 * RIGHT, or even no line.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.7 $
 */

public class NPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NPanel(final int i) {
		where = i;
	}

	@Override
	public void paint(final Graphics g) {
		final Dimension dimension = getSize();
		g.setColor(Color.black);
		switch (where) {
		case TOP: // '\0'
			g.drawLine(2, 2, dimension.width - 2, 2);
			return;

		case BOTTOM: // '\001'
			g.drawLine(2, dimension.height - 2, dimension.width - 2,
					dimension.height - 2);
			return;

		case LEFT: // '\002'
			g.drawLine(2, 2, 2, dimension.height - 2);
			return;

		case RIGHT: // '\003'
			g.drawLine(dimension.width - 2, 2, dimension.width - 2,
					dimension.height - 2);
			return;
		}
	}

	public static final int TOP = 0;

	public static final int BOTTOM = 1;

	public static final int LEFT = 2;

	public static final int RIGHT = 3;

	public static final int NO = -1;

	private final int where;
}

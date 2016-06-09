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

package edu.cuny.util;

import java.awt.Color;
import java.util.Hashtable;

/**
 * defines a table mapping readable names to <code>Color</code>s.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.9 $
 */

public class ColorNameTable {

	public static Color getColor(final String s) {
		return ColorNameTable.colorTable.get(s.toLowerCase());
	}

	private static Hashtable<String, Color> colorTable;

	static {
		ColorNameTable.colorTable = new Hashtable<String, Color>(16);
		ColorNameTable.colorTable.put("black", new Color(0));
		ColorNameTable.colorTable.put("silver", new Color(0xc0c0c0));
		ColorNameTable.colorTable.put("gray", new Color(0x808080));
		ColorNameTable.colorTable.put("white", new Color(0xffffff));
		ColorNameTable.colorTable.put("maroon", new Color(0x800000));
		ColorNameTable.colorTable.put("red", new Color(0xff0000));
		ColorNameTable.colorTable.put("purple", new Color(0x800080));
		ColorNameTable.colorTable.put("fuchsia", new Color(0xff00ff));
		ColorNameTable.colorTable.put("green", new Color(32768));
		ColorNameTable.colorTable.put("lime", new Color(65280));
		ColorNameTable.colorTable.put("olive", new Color(0x808000));
		ColorNameTable.colorTable.put("yellow", new Color(0xffff00));
		ColorNameTable.colorTable.put("navy", new Color(128));
		ColorNameTable.colorTable.put("blue", new Color(255));
		ColorNameTable.colorTable.put("teal", new Color(32896));
		ColorNameTable.colorTable.put("aqua", new Color(65535));
	}
}

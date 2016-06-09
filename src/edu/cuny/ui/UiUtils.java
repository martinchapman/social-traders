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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.TextComponent;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.RootPaneContainer;
import javax.swing.text.JTextComponent;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;

/**
 * A collection of methods for GUI components.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class UiUtils {

	public static final String P_NAME = "name";

	public static final String P_TITLE = "title";

	public static final String P_TEXT = "text";

	public static final String P_ICON = "icon";

	public static final String P_FONT = "font";

	public static final String P_BACKGROUND = "background";

	public static final String P_FOREGROUND = "foreground";

	public static final String P_X = "x";

	public static final String P_Y = "y";

	public static final String P_WIDTH = "width";

	public static final String P_HEIGHT = "height";

	public static void setup(final Component component,
			final ParameterDatabase parameters, final Parameter base) {

		component.setName(parameters.getStringWithDefault(
				base.push(UiUtils.P_NAME), null, ""));

		if (component instanceof Frame) {
			((Frame) component).setTitle(parameters.getStringWithDefault(base
					.push(UiUtils.P_TITLE), null, ""));
		} else if (component instanceof Dialog) {
			((Dialog) component).setTitle(parameters.getStringWithDefault(base
					.push(UiUtils.P_TITLE), null, ""));
		} else if (component instanceof JInternalFrame) {
			((JInternalFrame) component).setTitle(parameters.getStringWithDefault(
					base.push(UiUtils.P_TITLE), null, ""));
		}

		if (component instanceof TextComponent) {
			((TextComponent) component).setText(parameters.getStringWithDefault(base
					.push(UiUtils.P_TEXT), null, ""));
		} else if (component instanceof JTextComponent) {
			((JTextComponent) component).setText(parameters.getStringWithDefault(base
					.push(UiUtils.P_TEXT), null, ""));
		} else if (component instanceof AbstractButton) {
			((AbstractButton) component).setText(parameters.getStringWithDefault(base
					.push(UiUtils.P_TEXT), null, ""));
		}

		final Image image = parameters.getImage(base.push(UiUtils.P_ICON), null);
		if (image != null) {
			if (component instanceof AbstractButton) {
				((AbstractButton) component).setIcon(new ImageIcon(image));
			} else if (component instanceof JLabel) {
				((JLabel) component).setIcon(new ImageIcon(image));
			} else if (component instanceof JOptionPane) {
				((JOptionPane) component).setIcon(new ImageIcon(image));
			} else if (component instanceof Frame) {
				((Frame) component).setIconImage(image);
			} else if (component instanceof JInternalFrame) {
				((JInternalFrame) component).setFrameIcon(new ImageIcon(image));
			}
		}

		final Font font = parameters.getFont(base.push(UiUtils.P_FONT), null);
		if (font != null) {
			component.setFont(font);
		}

		Color color = parameters.getColor(base.push(UiUtils.P_BACKGROUND), null);
		if (color != null) {
			component.setBackground(color);
		}

		color = parameters.getColor(base.push(UiUtils.P_FOREGROUND), null);
		if (color != null) {
			component.setForeground(color);
		}

		final int i = parameters.getIntWithDefault(base.push(UiUtils.P_WIDTH),
				null, -1);
		final int j = parameters.getIntWithDefault(base.push(UiUtils.P_HEIGHT),
				null, -1);
		if ((i != -1) && (j != -1)) {
			final Dimension dimension = new Dimension(i, j);
			component.setSize(dimension);
		} else {
			component.setSize(component.getPreferredSize());
		}

		final int k = parameters
				.getIntWithDefault(base.push(UiUtils.P_X), null, -1);
		final int l = parameters
				.getIntWithDefault(base.push(UiUtils.P_Y), null, -1);
		if ((k != -1) && (l != -1)) {
			component.setLocation(k, l);
		}
	}

	public static Container getTopContainer(final Component com) {
		Container container = com.getParent();
		while (container != null) {
			if (container instanceof RootPaneContainer) {
				return container;
			} else {
				container = container.getParent();
			}
		}
		return null;
	}

	public static Frame getTopFrame(Component com) {
		while (com != null) {
			com = UiUtils.getTopContainer(com);
			if (com instanceof Frame) {
				return (Frame) com;
			}
		}
		return null;
	}

	public static void center(final Window window) {
		final Dimension dimension = window.getSize();
		Point point = null;

		final Container container = window.getParent();
		final boolean flag = container.isVisible();
		if (flag) {
			point = container.getLocationOnScreen();
			final Dimension dimension1 = container.getSize();
			point.x += (dimension1.width - dimension.width) / 2;
			point.y += (dimension1.height - dimension.height) / 2;
		} else {
			point = new Point(0, 0);
		}

		final Dimension dimension2 = Toolkit.getDefaultToolkit().getScreenSize();
		if (!flag || (point.x < 0) || (point.y < 0)
				|| (point.x + dimension.width > dimension2.width)
				|| (point.y + dimension.height > dimension2.height)) {
			point.x = (dimension2.width - dimension.width) / 2;
			point.y = (dimension2.height - dimension.height) / 2;
		}

		window.setLocation(point.x, point.y);
	}

}

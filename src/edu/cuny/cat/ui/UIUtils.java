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

import java.awt.Color;

import org.apache.log4j.Logger;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

/**
 * Utility methods for GUI.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.21 $
 */
public class UIUtils {

	static Logger logger = Logger.getLogger(UIUtils.class);

	public static void setDefaultBarRendererStyle(BarRenderer barRenderer) {
		// barRenderer.setShadowVisible(false);
		barRenderer.setDrawBarOutline(false);
	}

	public static void setDefaultLineAndShapeRendererStyle(
			LineAndShapeRenderer lsRenderer) {
		lsRenderer.setBaseShapesVisible(true);
		lsRenderer.setDrawOutlines(true);
		lsRenderer.setBaseItemLabelsVisible(true);
		lsRenderer.setBaseLinesVisible(true);
		lsRenderer.setUseFillPaint(true);
		lsRenderer.setBaseFillPaint(Color.white);
	}
}

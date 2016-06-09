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

package edu.cuny.util.log4j;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * displays log messages in gui components each for a named logger. It can be
 * embedded into a <code>JPanel</code>, or create a standalone
 * <code>JFrame</code> to display.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.12 $
 */

public class GraphicalAppender extends AppenderSkeleton {

	protected JTabbedPane tabbedPane;

	protected JFrame frame;

	protected Map<String, JEditorPane> loggerPanes;

	protected Map<String, String> texts;

	@Override
	public void activateOptions() {
		loggerPanes = new TreeMap<String, JEditorPane>();
		texts = new TreeMap<String, String>();
		tabbedPane = new JTabbedPane();
	}

	@Override
	protected void append(final LoggingEvent event) {

		if (loggerPanes == null) {
			return;
		}

		final JEditorPane loggerPane = getLoggerPane(event.getLoggerName());
		String text = texts.get(event.getLoggerName());
		if (text == null) {
			text = "";
		}

		text += (layout.format(event));

		if (layout.ignoresThrowable()) {
			final String[] s = event.getThrowableStrRep();
			if (s != null) {

				final int len = s.length;
				for (int i = 0; i < len; i++) {
					text += s[i];
					text += Layout.LINE_SEP;
				}
			}
		}

		if (getLayout() instanceof HTMLLayout) {
			final HTMLLayout htmlLayout = (HTMLLayout) getLayout();
			loggerPane
					.setText(htmlLayout.getHeader() + text + htmlLayout.getFooter());
		} else {
			SwingUtilities.invokeLater(new SetTextThread(loggerPane, text));
		}

		texts.put(event.getLoggerName(), text);
	}

	private class SetTextThread extends Thread {
		private final JEditorPane loggerPane;

		private final String text;

		public SetTextThread(final JEditorPane loggerPane, final String text) {
			this.loggerPane = loggerPane;
			this.text = text;
		}

		@Override
		public void run() {
			loggerPane.setText(text);
		}

	}

	private JEditorPane getLoggerPane(final String name) {

		JEditorPane loggerPane = loggerPanes.get(name);
		if (loggerPane == null) {
			loggerPane = createLoggerPane(name);
		}

		return loggerPane;
	}

	private JEditorPane createLoggerPane(final String name) {
		final JEditorPane loggerPane = new JEditorPane();
		if (getLayout() instanceof HTMLLayout) {
			loggerPane.setContentType("text/html");
		} else {
			loggerPane.setContentType("text/plain");
		}
		loggerPane.setEditable(false);

		final JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(loggerPane);

		tabbedPane.addTab(name, null, scroller, name);
		loggerPanes.put(name, loggerPane);

		return loggerPane;

	}

	public boolean requiresLayout() {
		return true;
	}

	public void close() {
		loggerPanes.clear();
		loggerPanes = null;
		if (frame != null) {
			frame.setVisible(false);
			frame.dispose();
		}
	}

	public Component getComponent() {
		return tabbedPane;
	}

	public Frame createFrame() {
		if (tabbedPane != null) {
			if (frame != null) {
				frame.getContentPane().removeAll();
				frame.setVisible(false);
				frame.dispose();
			}
			frame = new JFrame();
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

			frame.setBounds(100, 100, 600, 400);
			frame.setTitle("Logger Viewer");

			frame.setVisible(true);

			return frame;

		} else {
			return null;
		}
	}
}

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

import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * a more user-friendly HTML layout for log4j.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.10 $
 */

public class MyHTMLLayout extends HTMLLayout {

	static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";

	private StringBuffer sbuf = new StringBuffer(BUF_SIZE);

	public static final String LOCATION_INFO_OPTION = "LocationInfo";

	public static final String TITLE_OPTION = "Title";

	// Print no location info by default
	boolean locationInfo = false;

	String title = "Log4J Log Messages";

	String conversionPattern;

	@Override
	public void setLocationInfo(final boolean flag) {
		locationInfo = flag;
	}

	@Override
	public boolean getLocationInfo() {
		return locationInfo;
	}

	@Override
	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setConversionPattern(final String s) {
		conversionPattern = s;
	}

	public String getConversionPattern() {
		return conversionPattern;
	}

	@Override
	public String format(final LoggingEvent event) {

		if (sbuf.capacity() > MAX_CAPACITY) {
			sbuf = new StringBuffer(BUF_SIZE);
		} else {
			sbuf.setLength(0);
		}

		sbuf.append(Layout.LINE_SEP + "<tr>" + Layout.LINE_SEP);

		// sbuf.append("<td>");
		// sbuf.append(event.timeStamp - event.getStartTime());
		// sbuf.append("</td>" + Layout.LINE_SEP);

		// sbuf.append("<td title=\"" + event.getThreadName() + " thread\">");
		// sbuf.append(Transform.escapeTags(event.getThreadName()));
		// sbuf.append("</td>" + Layout.LINE_SEP);

		sbuf.append("<td title=\"Level\">");
		if (event.getLevel().equals(Level.DEBUG)) {
			sbuf.append("<font color=\"#339933\">");
			sbuf.append(event.getLevel());
			sbuf.append("</font>");
		} else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
			sbuf.append("<font color=\"#993300\"><strong>");
			sbuf.append(event.getLevel());
			sbuf.append("</strong></font>");
		} else {
			sbuf.append(event.getLevel());
		}
		sbuf.append("</td>" + Layout.LINE_SEP);

		// sbuf.append("<td title=\"" + event.getLoggerName() + " category\">");
		// sbuf.append(Transform.escapeTags(event.getLoggerName()));
		// sbuf.append("</td>" + Layout.LINE_SEP);

		if (locationInfo) {
			final LocationInfo locInfo = event.getLocationInformation();
			sbuf.append("<td>");
			sbuf.append(Transform.escapeTags(locInfo.getFileName()));
			sbuf.append(':');
			sbuf.append(locInfo.getLineNumber());
			sbuf.append("</td>" + Layout.LINE_SEP);
		}

		sbuf.append("<td title=\"Message\">");
		String msg = Transform.escapeTags(event.getRenderedMessage());
		msg = msg.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		msg = msg.replaceAll("\n", "<br>");
		sbuf.append(msg);
		sbuf.append("</td>" + Layout.LINE_SEP);
		sbuf.append("</tr>" + Layout.LINE_SEP);

		if (event.getNDC() != null) {
			sbuf
					.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : small;\" colspan=\"3\" title=\"Nested Diagnostic Context\">");
			sbuf.append("NDC: " + Transform.escapeTags(event.getNDC()));
			sbuf.append("</td></tr>" + Layout.LINE_SEP);
		}

		final String[] s = event.getThrowableStrRep();
		if (s != null) {
			sbuf
					.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : small;\" colspan=\"3\">");
			appendThrowableAsHTML(s, sbuf);
			sbuf.append("</td></tr>" + Layout.LINE_SEP);
		}

		return sbuf.toString();
	}

	public void appendThrowableAsHTML(final String[] s, final StringBuffer sbuf) {
		if (s != null) {
			final int len = s.length;
			if (len == 0) {
				return;
			}
			sbuf.append(Transform.escapeTags(s[0]));
			sbuf.append(Layout.LINE_SEP);
			for (int i = 1; i < len; i++) {
				sbuf.append(MyHTMLLayout.TRACE_PREFIX);
				sbuf.append(Transform.escapeTags(s[i]));
				sbuf.append(Layout.LINE_SEP);
			}
		}
	}

	@Override
	public String getHeader() {
		final StringBuffer sbuf = new StringBuffer();
		sbuf
				.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
						+ Layout.LINE_SEP);
		sbuf.append("<html>" + Layout.LINE_SEP);
		sbuf.append("<head>" + Layout.LINE_SEP);
		sbuf.append("<title>" + title + "</title>" + Layout.LINE_SEP);
		sbuf.append("<style type=\"text/css\">" + Layout.LINE_SEP);
		sbuf.append("<!--" + Layout.LINE_SEP);
		sbuf
				.append("body, table {font-family: monospace,sans-serif,arial; font-size: normal; background: #DDDDDD}"
						+ Layout.LINE_SEP);
		sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}"
				+ Layout.LINE_SEP);
		sbuf.append("-->" + Layout.LINE_SEP);
		sbuf.append("</style>" + Layout.LINE_SEP);
		sbuf.append("</head>" + Layout.LINE_SEP);
		sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">"
				+ Layout.LINE_SEP);
		sbuf.append("<hr size=\"1\" noshade>" + Layout.LINE_SEP);
		sbuf.append("Log session start time " + new java.util.Date() + "<br>"
				+ Layout.LINE_SEP);
		sbuf.append("<br>" + Layout.LINE_SEP);
		// sbuf.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\"
		// bordercolor=\"#224466\" width=\"100%\">" + Layout.LINE_SEP);
		sbuf
				.append("<table cellspacing=\"0\" cellpadding=\"2\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">"
						+ Layout.LINE_SEP);
		sbuf.append("<tr>" + Layout.LINE_SEP);
		// sbuf.append("<th>Time</th>" + Layout.LINE_SEP);
		// sbuf.append("<th>Thread</th>" + Layout.LINE_SEP);
		sbuf.append("<th width=10%>Level</th>" + Layout.LINE_SEP);
		// sbuf.append("<th>Category</th>" + Layout.LINE_SEP);
		if (locationInfo) {
			sbuf.append("<th width=10%>File:Line</th>" + Layout.LINE_SEP);
		}
		sbuf.append("<th>Message</th>" + Layout.LINE_SEP);
		sbuf.append("</tr>" + Layout.LINE_SEP);
		return sbuf.toString();
	}

}

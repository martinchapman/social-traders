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
 * JASA Java Auction Simulator API
 * Copyright (C) 2001-2005 Steve Phelps
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

package edu.cuny.util.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.cuny.util.Parameter;
import edu.cuny.util.ParameterDatabase;
import edu.cuny.util.Parameterizable;

/**
 * A class for writing data to a CSV (comma-separated variables) text file.
 * 
 * <p>
 * <b>Parameters</b>
 * </p>
 * <table>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.filename</tt><br>
 * <font size=-1>string</font></td>
 * <td valign=top>(the name of the file to store data)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.separator</tt><br>
 * <font size=-1>string</font></td>
 * <td valign=top>(the first char of string used to separate columns in the text
 * file)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.autowrap</tt><br>
 * <font size=-1>boolean (<code>false</code> by default)</font></td>
 * <td valign=top>(whether or not line wrapping is automatic)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.columns</tt><br>
 * <font size=-1>int > 0</font></td>
 * <td valign=top>(used only when line wrapping is automatic to determine if a
 * new line should be started)</td>
 * <tr>
 * 
 * <tr>
 * <td valign=top><i>base</i><tt>.append</tt><br>
 * <font size=-1>boolean (<code>true</code> by default)</font></td>
 * <td valign=top>(data written to the end of file if <code>true</code>; to the
 * beginning otherwise)</td>
 * <tr>
 * 
 * </table>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.22 $
 */

public class CSVWriter implements Parameterizable, Serializable, DataWriter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected PrintStream out;

	protected boolean autowrap = true;

	protected int numColumns;

	protected int currentColumn = 0;

	protected char separator = CSVWriter.DEFAULT_SEPARATOR;

	protected boolean append = true;

	protected String fileName = null;

	static final char DEFAULT_SEPARATOR = '\t';

	public static final String P_FILENAME = "filename";

	public static final String P_SEPARATOR = "separator";

	public static final String P_AUTOWRAP = "autowrap";

	public static final String P_COLUMNS = "columns";

	public static final String P_APPEND = "append";

	static Logger logger = Logger.getLogger(CSVWriter.class);

	public CSVWriter(final OutputStream out, final int numColumns,
			final char seperator) {
		this.out = new PrintStream(new BufferedOutputStream(out));
		this.numColumns = numColumns;
		separator = seperator;
	}

	public CSVWriter(final OutputStream out, final char seperator) {
		this.out = new PrintStream(new BufferedOutputStream(out));
		autowrap = false;
		separator = seperator;
	}

	public CSVWriter(final OutputStream out, final int numColumns) {
		this(out, numColumns, CSVWriter.DEFAULT_SEPARATOR);
	}

	public CSVWriter(final OutputStream out) {
		this(out, CSVWriter.DEFAULT_SEPARATOR);
	}

	public CSVWriter() {
	}

	public void setup(final ParameterDatabase parameters, final Parameter base) {
		fileName = parameters.getStringWithDefault(base.push(CSVWriter.P_FILENAME),
				null, fileName);
		if (fileName == null) {
			CSVWriter.logger.error(base.push(CSVWriter.P_FILENAME) + " is NOT set!");
		}

		append = parameters.getBoolean(base.push(CSVWriter.P_APPEND), null, append);

		autowrap = parameters.getBoolean(base.push(CSVWriter.P_AUTOWRAP), null,
				autowrap);
		if (autowrap) {
			numColumns = parameters.getIntWithDefault(base.push(CSVWriter.P_COLUMNS),
					null, numColumns);
		}

		final String sep = parameters.getStringWithDefault(base
				.push(CSVWriter.P_SEPARATOR), null, String
				.valueOf(CSVWriter.DEFAULT_SEPARATOR));
		if (sep.length() == 0) {
			separator = CSVWriter.DEFAULT_SEPARATOR;
		} else if (sep.equals("\t")) {
			separator = '\t';
		} else {
			separator = sep.charAt(0);
		}
	}

	public void newData(final Iterator<?> i) {
		while (i.hasNext()) {
			newData(i.next());
		}
	}

	public void newData(final Object[] data) {
		for (final Object element : data) {
			newData(element);
		}
	}

	public void newData(final Boolean data) {
		newData(data.booleanValue());
	}

	public void newData(final Integer data) {
		newData(data.intValue());
	}

	public void newData(final Double data) {
		newData(data.doubleValue());
	}

	public void newData(final Long data) {
		newData(data.longValue());
	}

	public void newData(final String data) {
		prepareColumn();
		out.print(data);
		nextColumn();
	}

	public void newData(final int data) {
		prepareColumn();
		out.print(data);
		nextColumn();
	}

	public void newData(final long data) {
		prepareColumn();
		out.print(data);
		nextColumn();
	}

	public void newData(final double data) {
		prepareColumn();
		out.print(data);
		nextColumn();
	}

	public void newData(final float data) {
		prepareColumn();
		out.print(data);
		nextColumn();
	}

	public void newData(final boolean data) {
		if (data) {
			newData("1");
		} else {
			newData("0");
		}
	}

	public void newData(final Object data) {
		if (data instanceof Boolean) {
			newData((Boolean) data);
		} else {
			prepareColumn();
			out.print(data.toString());
			nextColumn();
		}
	}

	public void setAutowrap(final boolean autowrap) {
		this.autowrap = autowrap;
	}

	public void setAppend(final boolean append) {
		this.append = append;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setSeparator(final char separator) {
		this.separator = separator;
	}

	public char getSeparator() {
		return separator;
	}

	public void endRecord() {
		if (autowrap) {
			new Error("endRecord() should NOT be invoked when autowrap is enabled.");
		}
		newLine();
	}

	public void flush() {
		out.flush();
	}

	public void open() {
		if (out == null) {
			try {
				final File file = new File(fileName);
				if ((file.getParentFile() != null) && !file.getParentFile().exists()) {
					if (!file.getParentFile().mkdirs()) {
						CSVWriter.logger.error("Failed to create parent directories for "
								+ fileName + " !");
					}
				}

				out = new PrintStream(new BufferedOutputStream(new FileOutputStream(
						file, append)));
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				CSVWriter.logger.error(fileName + " cannot be created !");
			} catch (final SecurityException e) {
				e.printStackTrace();
				CSVWriter.logger.error(fileName + " is NOT writable !");
			}
		} else {
			CSVWriter.logger.debug("File already opened in "
					+ getClass().getSimpleName());
		}
	}

	public void close() {
		out.close();
		out = null;
	}

	public void setNumColumns(final int numColumns) {
		if (!autowrap) {
			new Error(
					"The number of columns should NOT be set when autowrap is disabled.");
		}
		this.numColumns = numColumns;
	}

	protected void prepareColumn() {
		if (!autowrap) {
			if (currentColumn > 0) {
				out.print(separator);
			}
		}
	}

	protected void nextColumn() {
		currentColumn++;
		if (autowrap) {
			if (currentColumn < numColumns) {
				out.print(separator);
			} else {
				newLine();
			}
		}
	}

	private void newLine() {
		out.println();
		currentColumn = 0;
	}

	private void writeObject(final java.io.ObjectOutputStream out)
			throws IOException {
	}

	private void readObject(final java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
	}
}

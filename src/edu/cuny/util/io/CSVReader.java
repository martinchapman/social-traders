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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import edu.cuny.util.Utils;

/**
 * A class for reading data from a CSV (comma-separated variables) text file.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.7 $
 */

public class CSVReader {

	BufferedReader in;

	char seperator;

	Class<?>[] types;

	static final char DEFAULT_SEPERATOR = '\t';

	static Logger logger = Logger.getLogger(CSVReader.class);

	public CSVReader(final InputStream in, final Class<?>[] types,
			final char seperator) {
		this.in = new BufferedReader(new InputStreamReader(in));
		this.seperator = seperator;
		this.types = types;
	}

	public CSVReader(final InputStream in, final Class<?>[] types) {
		this(in, types, CSVReader.DEFAULT_SEPERATOR);
	}

	public CSVReader(final InputStream in, final char separator) {
		this(in, null, separator);
	}

	public CSVReader(final InputStream in) {
		this(in, CSVReader.DEFAULT_SEPERATOR);
	}

	public String[] nextRecord() throws IOException {
		final String line = in.readLine();
		if (line == null) {
			return null;
		}

		String[] record = null;

		try {
			final StringTokenizer tokens = new StringTokenizer(line, seperator + "");
			record = new String[tokens.countTokens()];

			for (int i = 0; i < record.length; i++) {
				record[i] = tokens.nextToken();
			}

		} catch (final NoSuchElementException e) {
			CSVReader.logger.error(e);
		}

		return record;
	}

	public <N extends Number> N[] nextRecordTyped(final Class<N> type)
			throws IOException {
		final String[] strings = nextRecord();
		return Utils.convert(strings, type);
	}

	public Object[] nextRecordTyped() throws IOException {
		final String[] strings = nextRecord();

		final Object[] objects = new Object[strings.length];

		try {

			int offset = 0;

			if (types != null) {
				/* typed field values */
				offset = types.length;
				for (int i = 0; i < types.length; i++) {
					objects[i] = Utils.convert(strings[i], types[i]);
				}
			}

			for (int i = offset; i < objects.length; i++) {
				objects[i] = strings[i];
			}
		} catch (final NoSuchElementException e) {
			CSVReader.logger.error(e);
		}

		return objects;
	}

	public void close() {
		try {
			in.close();
		} catch (final IOException e) {
			CSVReader.logger.error(e);
		}

		in = null;
	}

}
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

package edu.cuny.cat.stat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cuny.cat.core.Shout;
import edu.cuny.cat.core.Transaction;
import edu.cuny.cat.event.AuctionEvent;
import edu.cuny.cat.event.DayClosedEvent;
import edu.cuny.cat.event.DayOpeningEvent;
import edu.cuny.cat.event.RoundOpenedEvent;
import edu.cuny.cat.event.ShoutPlacedEvent;
import edu.cuny.cat.event.ShoutPostedEvent;
import edu.cuny.cat.event.SimulationOverEvent;
import edu.cuny.cat.event.TransactionExecutedEvent;
import edu.cuny.cat.event.TransactionPostedEvent;
import edu.cuny.event.Event;
import edu.cuny.event.EventEngine;
import edu.cuny.event.EventListener;
import edu.cuny.util.Galaxy;

/**
 * A report logging the behaviors of game clients for debugging purpose.
 * Information about each game client is stored in a separate file.
 * 
 * TODO: to refactor later to remove static behavior.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */

public class ClientDynamicsReport implements GameReport, EventListener {

	static Logger logger = Logger.getLogger(ClientDynamicsReport.class);

	public static final String ID = "id";

	public static final String INFO = "info";

	public static final String PATH = "log";

	public static final String TRANSACTIONS = "transactions";

	protected Writer transactionWriter;

	protected Map<String, FileWriter> fileMap;

	static {
		final File dir = new File(ClientDynamicsReport.PATH);
		if (!dir.exists()) {
			dir.mkdir();
		}
	}

	public ClientDynamicsReport() {
		fileMap = Collections.synchronizedMap(new HashMap<String, FileWriter>());

		transactionWriter = openWriter(ClientDynamicsReport.TRANSACTIONS);

		/* TODO: need to check out sometime later ? */
		Galaxy.getInstance().getDefaultTyped(EventEngine.class).checkIn(getClass(),
				this);
	}

	public Map<ReportVariable, ?> getVariables() {
		return null;
	}

	public void produceUserOutput() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public void eventOccurred(final AuctionEvent event) {
		String info = null;
		if (event instanceof DayOpeningEvent) {
			info = "**** Day " + event.getDay() + " [[[[[[[[[[[[[[[[[";
		} else if (event instanceof RoundOpenedEvent) {
			info = "------ Round " + event.getRound() + " -----------";
		} else if (event instanceof DayClosedEvent) {
			info = "**** Day " + event.getDay() + " ]]]]]]]]]]]]]]]]]";
		} else if (event instanceof ShoutPlacedEvent) {
			final ShoutPlacedEvent spEvent = (ShoutPlacedEvent) event;
			final Shout shout = spEvent.getShout();
			info = shout.getId() + "  "
					+ GameReport.Formatter.format(shout.getPrice()) + "  "
					+ shout.getTrader().getId();
		} else if (event instanceof ShoutPostedEvent) {
			final ShoutPostedEvent spEvent = (ShoutPostedEvent) event;
			final Shout shout = spEvent.getShout();
			info = " Posted shout: " + shout.getId() + "  "
					+ GameReport.Formatter.format(shout.getPrice()) + "  "
					+ shout.getTrader().getId();
		} else if (event instanceof TransactionExecutedEvent) {
			final TransactionExecutedEvent teEvent = (TransactionExecutedEvent) event;
			final Transaction transaction = teEvent.getTransaction();
			final Shout ask = transaction.getAsk();
			final Shout bid = transaction.getBid();
			info = transaction.getId() + "  "
					+ GameReport.Formatter.format(transaction.getPrice()) + "  {"
					+ bid.getTrader().getId() + "." + bid.getId() + ": "
					+ GameReport.Formatter.format(bid.getPrice()) + ", "
					+ ask.getTrader().getId() + "." + ask.getId() + ": "
					+ GameReport.Formatter.format(ask.getPrice()) + ")";

			writeTo(transactionWriter, event.getDay() + "." + event.getRound() + "\t"
					+ info);

		} else if (event instanceof TransactionPostedEvent) {
			final TransactionPostedEvent tpEvent = (TransactionPostedEvent) event;
			final Transaction transaction = tpEvent.getTransaction();
			final Shout ask = transaction.getAsk();
			final Shout bid = transaction.getBid();
			info = " Posted transaction: " + transaction.getId() + "  "
					+ GameReport.Formatter.format(transaction.getPrice()) + "  {"
					+ bid.getTrader().getId() + "." + bid.getId() + ": "
					+ GameReport.Formatter.format(bid.getPrice()) + ", "
					+ ask.getTrader().getId() + "." + ask.getId() + ": "
					+ GameReport.Formatter.format(ask.getPrice()) + ")";

			writeTo(transactionWriter, event.getDay() + "." + event.getRound() + "\t"
					+ info);
		} else if (event instanceof SimulationOverEvent) {
			// do nothing, keep info null
		} else {
			return;
		}

		final Iterator<String> itor = fileMap.keySet().iterator();
		while (itor.hasNext()) {
			final String id = itor.next();
			final FileWriter writer = fileMap.get(id);
			if (info == null) {
				closeWriter(id, writer);
			} else {
				writeTo(writer, info);
			}
		}
	}

	protected FileWriter openWriter(final String id) {
		FileWriter writer = null;
		final File file = new File(ClientDynamicsReport.PATH, id + ".log");
		try {
			writer = new FileWriter(file, false);
		} catch (final IOException e) {
			e.printStackTrace();
			ClientDynamicsReport.logger
					.error("Failed to create a writer to record client behavior for "
							+ id + "!");
		}

		writeTo(writer,
				"**************************************************************");

		return writer;
	}

	protected void closeWriter(final String id, final FileWriter writer) {
		try {
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected void writeTo(final String id, final String info) {
		FileWriter writer = fileMap.get(id);
		if (writer == null) {
			writer = openWriter(id);
			fileMap.put(id, writer);
		}

		writeTo(writer, info);
	}

	protected void writeTo(final Writer writer, final String info) {
		try {
			writer.write(info + "\n");
			writer.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * allows clients to report their behaviors without directly refering to this
	 * board
	 */
	public void eventOccurred(final Event te) {
		final String id = (String) te.getValue(ClientDynamicsReport.ID);
		final String info = (String) te.getValue(ClientDynamicsReport.INFO);
		writeTo(id, info);
	}
}

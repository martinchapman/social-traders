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

package edu.cuny.cat;

import org.apache.log4j.Logger;

import edu.cuny.cat.comm.CatpMessage;
import edu.cuny.util.SyncTask;
import edu.cuny.util.Utils;

/**
 * This class aims to make sure that the server will not start the game until
 * all local clients check in in order.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.2 $
 */

public class ClientSynchronizer {

	protected static Logger logger = Logger.getLogger(ClientSynchronizer.class);

	protected SyncTask syncClientTask;

	public ClientSynchronizer() {
		syncClientTask = new SyncTask(GameClient.class, 60000);
		syncClientTask.setTag(CatpMessage.CHECKIN);
	}

	public synchronized void countMe() {
		syncClientTask.addCount(1);
	}

	public void waitForClients() {
		syncClientTask.sync();
	}

	public void dispose() {
		syncClientTask.terminate();
		syncClientTask = null;
	}

	@Override
	public String toString() {
		String s = getClass().getSimpleName();

		s += "\n" + Utils.indent(syncClientTask.toString());

		return s;
	}
}

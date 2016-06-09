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

import java.util.Iterator;

/**
 * Interface for logging data to a back-end store.
 * 
 * @author Steve Phelps
 * @version $Revision: 1.8 $
 */

public interface DataWriter {

	public void newData(Iterator<?> i);

	public void newData(Object[] data);

	public void newData(String data);

	public void newData(Double data);

	public void newData(Integer data);

	public void newData(Long data);

	public void newData(Object data);

	public void newData(int data);

	public void newData(long data);

	public void newData(double data);

	public void newData(float data);

	public void newData(boolean data);

	public void flush();

	public void close();

}

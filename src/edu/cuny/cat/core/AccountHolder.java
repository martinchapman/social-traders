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

package edu.cuny.cat.core;

import edu.cuny.util.Resetable;

/**
 * representing the entity that can own an {@link Account}.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.8 $
 */
public class AccountHolder implements Resetable, Comparable<AccountHolder> {

	protected Account account;

	/**
	 * The description of this account holder.
	 */
	protected String desc;

	protected String id;

	public AccountHolder() {
		this(null);
	}

	public AccountHolder(final String id) {
		this(id, null);
	}

	public AccountHolder(final String id, final String desc) {
		this.id = id;
		this.desc = desc;
		account = new Account();
	}

	public void reset() {
		account.reset();
	}

	public Account getAccount() {
		return account;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getDescription() {
		return desc;
	}

	public void setDescription(final String desc) {
		this.desc = desc;
	}

	public int compareTo(AccountHolder holder) {
		if (id != null) {
			return id.compareTo(holder.getId());
		} else {
			return holder.getId() == null ? 0 : -1;
		}
	}

}

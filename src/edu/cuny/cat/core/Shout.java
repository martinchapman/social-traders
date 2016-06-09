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

package edu.cuny.cat.core;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

/**
 * <p>
 * A class representing a shout in an auction. A shout may be either a bid
 * (offer to buy) or an ask (offer to sell).
 * </p>
 * 
 * <p>
 * Shouts are mutable within this package for performance reasons, hence care
 * should be taken not to rely on, e.g. shouts held in collections remaining
 * constant.
 * </p>
 * 
 * @author Steve Phelps
 * @version $Revision: 1.23 $
 */

public class Shout implements Comparable<Shout>, Cloneable, Serializable {

	/**
	 * a switch for debug purposes. If true, output trace of shouts
	 */
	public static final boolean TRACE = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(Shout.class);

	/**
	 * The number of items offered/wanted.
	 */
	protected int quantity;

	/**
	 * The price of this offer
	 */
	protected double price;

	/**
	 * The agent placing this offer
	 */
	protected String id;

	protected Trader trader;

	protected Specialist specialist;

	/**
	 * True if this shout is a bid. False if this shout is an ask.
	 */
	protected boolean isBid = true;

	public final static int CREATED = 0x00;

	public final static int PLACED = 0x01;

	public final static int REJECTED = 0x02;

	public final static int MATCHED = 0x03;

	/**
	 * different meaning at different locations:
	 * 
	 * (TRADER): placing shout being attempted;
	 * 
	 * (SERVER): an attempted shout placing or modification is under way
	 * 
	 * (SPECIALIST): a transaction involving this shout is proposed to server
	 */
	public final static int PENDING = 0xff;

	protected int state;

	/**
	 * The child of this shout.
	 * 
	 * In various scenarios, it is used to store the shout attempting to modify
	 * this shout.
	 */
	protected Shout child = null;

	/**
	 * the parent of this shout.
	 * 
	 * In various scenarios, it is used to store the shout that this shout is
	 * attempting to modify.
	 */
	protected Shout parent = null;

	static DecimalFormat currencyFormatter = new DecimalFormat(
			"+#########0.00;-#########.00");

	public Shout() {
		this(1, Double.NaN, true);
	}

	public Shout(final String shoutId, final double price, final boolean isBid) {
		this(shoutId, 1, price, isBid);
	}

	public Shout(final int quantity, final double price, final boolean isBid) {
		this(null, quantity, price, isBid);
	}

	public Shout(final Shout existing) {
		this(existing.getId(), existing.getQuantity(), existing.getPrice(),
				existing.isBid());
	}

	public Shout(final String shoutId, final int quantity, final double price,
			final boolean isBid, final Trader trader, final Specialist specialist) {
		this(shoutId, quantity, price, isBid);
		this.trader = trader;
		this.specialist = specialist;
	}

	public Shout(final String shoutId, final int quantity, final double price,
			final boolean isBid) {
		id = shoutId;
		this.quantity = quantity;
		this.price = price;
		this.isBid = isBid;

		state = Shout.PENDING;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPrice() {
		return price;
	}

	public String getId() {
		return id;
	}

	public Trader getTrader() {
		return trader;
	}

	public Specialist getSpecialist() {
		return specialist;
	}

	public boolean isBid() {
		return isBid;
	}

	public boolean isAsk() {
		return !isBid;
	}

	public int getState() {
		return state;
	}

	public void setState(final int state) {
		this.state = state;
	}

	public boolean isMatched() {
		return state == Shout.MATCHED;
	}

	public boolean satisfies(final Shout other) {
		if (isBid()) {
			return other.isAsk() && (getPrice() >= other.getPrice());
		} else {
			return other.isBid() && (other.getPrice() >= getPrice());
		}
	}

	public int compareTo(final Shout other) {
		if (price > other.price) {
			return 1;
		} else if (price < other.price) {
			return -1;
		} else {
			return 0;
		}
		// return new Long(this.price).compareTo(new Long(other.getPrice()));
	}

	/**
	 * TODO: currently only shout ID is compared, however this cannot rule out the
	 * parent shout and child shout.
	 * 
	 * @param shout
	 *          the shout to be compared with this shout
	 * @return true if equal; false otherwise
	 */
	public boolean equals(final Shout shout) {
		return getId().equals(shout.getId());
	}

	public boolean isValid() {
		if (price < 0) {
			return false;
		}
		if (quantity < 1) {
			return false;
		}
		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Shout shout = null;
		shout = (Shout) super.clone();
		if (shout.trader != null) {
			shout.trader = (Trader) shout.trader.clone();
		}
		if (shout.specialist != null) {
			shout.specialist = (Specialist) shout.specialist.clone();
		}

		return shout;
	}

	@Override
	public String toString() {

		String s = getClass().getSimpleName() + " quantity:" + quantity + " price:"
				+ price + " isBid:" + isBid + " Id:" + id;

		if (trader != null) {
			s += " trader:" + trader.getId();
		}

		if (specialist != null) {
			s += " specialist:" + specialist.getId();
		}

		s += " state:" + getStateDescription();

		return s;
	}

	public String toPrettyString() {
		double p = price;
		if (!isBid) {
			p = -p;
		}
		return Shout.currencyFormatter.format(p) + "/" + quantity;
	}

	public static double maxPrice(final Shout s1, final Shout s2) {
		return Math.max(Shout.price(s1, Double.NEGATIVE_INFINITY), Shout.price(s2,
				Double.NEGATIVE_INFINITY));
	}

	public static double maxPrice(final Shout s, final double price) {
		return Math.max(Shout.price(s, Double.NEGATIVE_INFINITY), price);
	}

	public static double minPrice(final Shout s1, final Shout s2) {
		return Math.min(Shout.price(s1, Double.POSITIVE_INFINITY), Shout.price(s2,
				Double.POSITIVE_INFINITY));
	}

	public static double minPrice(final Shout s, final double price) {
		return Math.min(Shout.price(s, Double.POSITIVE_INFINITY), price);
	}

	private static double price(final Shout s, final double alt) {
		if (s == null) {
			return alt;
		} else {
			return s.getPrice();
		}
	}

	/**
	 * Get the child of this shout.
	 * 
	 * TODO: it is true in jcat that ' Shouts have children when they are
	 * split().'
	 * 
	 * @return The child Shout object, or null if this Shout is childless.
	 */
	public Shout getChild() {
		return child;
	}

	public void setChild(final Shout child) {
		this.child = child;
	}

	/**
	 * Get the parent of this shout.
	 * 
	 * @return The parent Shout object, or null if this Shout is parentless.
	 */
	public Shout getParent() {
		return parent;
	}

	public void setParent(final Shout parent) {
		this.parent = parent;
	}

	// public boolean equals( Object other ) {
	// return id == ((Shout) other).id &&
	// getAgent().equals(((Shout) other).getAgent());
	// }
	//
	// The following methods allow muting of shouts, but only by classes
	// that are part of the uk.ac.liv.auction.core package.
	//
	void makeChildless() {
		if (child != null) {
			child.makeChildless();
			child = null;
		}
	}

	public void copyFrom(final Shout other) {
		price = other.getPrice();
		id = other.getId();
		quantity = other.getQuantity();
		isBid = other.isBid();
		state = other.getState();
		trader = other.getTrader();
		specialist = other.getSpecialist();
		child = null;
	}

	/**
	 * Reduce the quantity of this shout by excess and return a new child shout
	 * containing the excess quantity. After a split, parent shouts keep a
	 * reference to their children.
	 * 
	 * @param excess
	 *          The excess quantity
	 * 
	 */
	public Shout split(final int excess) {
		quantity -= excess;
		final Shout newShout = new Shout(id, excess, price, isBid);
		child = newShout;
		return newShout;
	}

	public Shout splat(final int excess) {
		final Shout newShout = new Shout(id, quantity - excess, price, isBid);
		// Shout newShout = ShoutPool.fetch(agent, excess, price, isBid);
		quantity = excess;
		child = newShout;
		return newShout;
	}

	public void setIsBid(final boolean isBid) {
		this.isBid = isBid;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setPrice(final double price) {
		this.price = price;
	}

	public void setQuantity(final int quantity) {
		this.quantity = quantity;
	}

	public void setTrader(final Trader trader) {
		this.trader = trader;
	}

	public void setSpecialist(final Specialist specialist) {
		this.specialist = specialist;
	}

	public String getStateDescription() {
		switch (state) {
		case CREATED:
			return "CREATED";

		case PLACED:
			return "PLACED";

		case REJECTED:
			return "REJECTED";

		case MATCHED:
			return "MATCHED";

		case PENDING:
			return "PENDING";
		default:
			Shout.logger.error("Invalid shout state !", new Exception());
			return null;
		}
	}

	/**
	 * A Shout that is publically mutable.
	 * 
	 * @author Steve Phelps
	 */
	public static class MutableShout extends Shout {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MutableShout() {
			super();
		}

		public MutableShout(final Shout existing) {
			super(existing);
		}

		@Override
		public void setPrice(final double price) {
			super.setPrice(price);
		}

		@Override
		public void setId(final String shoutId) {
			super.setId(shoutId);
		}

		@Override
		public void setQuantity(final int quantity) {
			super.setQuantity(quantity);
		}

		@Override
		public void setIsBid(final boolean isBid) {
			super.setIsBid(isBid);
		}

		@Override
		public void copyFrom(final Shout other) {
			super.copyFrom(other);
		}
	}

}

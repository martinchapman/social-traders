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

/**
 * Each instance of this class represents a trading agent.
 * 
 * @author Kai Cai
 * @version $Revision: 1.18 $
 */

public class Trader extends AccountHolder implements Cloneable {

	/**
	 * The private value of this trader
	 */
	protected double privateValue;

	/**
	 * The identity of the specialist which this trader currently registered to.
	 */
	protected String specialistId;

	/**
	 * ~MDC 15/2/11
	 * The identity of the trader upon whose advice this trader is currently
	 * acting.
	 */
	protected String referrerId = null;

	/**
	 * Flag indicating whether this trader is a seller or buyer.
	 */
	protected boolean isSeller = false;

	protected int entitlement;

	public Trader(final String id, final String desc, final boolean isSeller) {
		this(id, desc, Double.NaN, isSeller);
	}

	public Trader(final String id, final String desc, final double privateValue,
			final boolean isSeller) {
		super(id, desc);
		this.privateValue = privateValue;
		this.isSeller = isSeller;
	}

	public Trader(final String id, final String desc, final double privateValue,
			final String specialistId, final boolean isSeller) {
		super(id, desc);
		this.privateValue = privateValue;
		this.specialistId = specialistId;
		this.isSeller = isSeller;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {

		return super.clone();
	}

	public double getPrivateValue() {
		return privateValue;
	}

	public String getSpecialistId() {
		return specialistId;
	}

	// ~MDC 15/2/11
	public String getReferrerId() {
		return referrerId;
	}
	
	public boolean isSeller() {
		return isSeller;
	}

	public void setPrivateValue(final double privateValue) {
		this.privateValue = privateValue;
	}

	public void setSpecialistId(final String specialistId) {
		this.specialistId = specialistId;
	}

	// ~MDC 15/2/11
	public void setReferrerId(final String referrerId) {
		this.referrerId = referrerId;
	}

	public void setIsSeller(final boolean isSeller) {
		this.isSeller = isSeller;
	}

	/**
	 * @return an array of private values assigned to the entitlements of the
	 *         trader
	 */
	public double[] getPrivateValues() {
		return new double[] { privateValue };
	}

	/**
	 * @return the entitlements of the trader
	 */
	public int getEntitlement() {
		return entitlement;
	}

	/**
	 * @param entitlement
	 *          the entitlements for the trader
	 */
	public void setEntitlement(final int entitlement) {
		this.entitlement = entitlement;
	}
}

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

package edu.cuny.ai.learning.prob;

/**
 * This class presents a framework of an n-armed slot machine.
 * 
 * @author Jinzhong Niu
 * @version $Revision: 1.3 $
 */

public abstract class NArmedBanditProb {

	protected int numOfArms = 5;

	/**
	 * 
	 * @return number of arms to choose from
	 */
	public int getNumOfArms() {
		return numOfArms;
	}

	/**
	 * pull an arm.
	 * 
	 * @param arm
	 *          the arm to pull
	 * @return the reward after pulling the arm
	 * 
	 */
	public abstract double pull(int arm);

	/**
	 * 
	 * @return the expected returns of arms.
	 */
	public double[] getExpectedReturns() {
		final double returns[] = new double[numOfArms];
		for (int i = 0; i < returns.length; i++) {
			returns[i] = getExpectedReturn(i);
		}

		return returns;
	}

	/**
	 * 
	 * @return the expected return of an arm.
	 */
	public abstract double getExpectedReturn(int arm);

	/**
	 * 
	 * @return the index of the best arm or -1 when multiple arms tie.
	 */
	public abstract int getBestArm();

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
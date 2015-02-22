/*
 * <p> <b>jFin, open source derivatives trade processing</b> </p>
 *
 * <p> Copyright (C) 2005, 2006, 2007 Morgan Brown Consultancy Ltd. </p>
 *
 * <p> This file is part of jFin. </p>
 *
 * <p> jFin is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. </p>
 *
 * <p> jFin is distributed in the hope that it will be useful, but <b>WITHOUT
 * ANY WARRANTY</b>; without even the implied warranty of <b>MERCHANTABILITY</b>
 * or <b>FITNESS FOR A PARTICULAR PURPOSE</b>. See the GNU General Public
 * License for more details. </p>
 *
 * <p> You should have received a copy of the GNU General Public License along
 * with jFin; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA. </p>
 */

package org.jfin.date.accrual;

import org.jfin.common.Iso4217Currency;

/**
 * Used to describe a Notional
 */
public class Notional {
	private double amount;
	private Iso4217Currency currency;

	/**
	 * Default constructor
	 */
	public Notional() {
	}

	/**
	 * Utility constructor
	 * @param amount The in currency amount
	 * @param currency The currency of the notional
	 */
	public Notional(double amount, Iso4217Currency currency) {
		this.amount = amount;
		this.currency = currency;
	}

	/**
	 * Gets the amount (in currency) of the Notional
	 * @return The amount
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * Sets the amount (in currency) of the Notional
	 * @param amount The amount
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	/**
	 * Gets the currency of the Notional
	 * @return The currency
	 */
	public Iso4217Currency getCurrency() {
		return currency;
	}

	/**
	 * Sets the currency of the Notional
	 * @param currency The currency
	 */
	public void setCurrency(Iso4217Currency currency) {
		this.currency = currency;
	}
}

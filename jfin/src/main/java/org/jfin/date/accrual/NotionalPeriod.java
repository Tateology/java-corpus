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

import org.jfin.date.Period;
import org.jfin.common.Iso4217Currency;

import java.util.Calendar;

/**
 * Used to model a Notional Period; a period of time during which
 * the notional amount of a trade is unchanged.
 */
public class NotionalPeriod extends Period {
	Notional notional;

	/**
	 * Default constructor
	 */
	public NotionalPeriod() {
	}

	/**
	 * Copy constructor
	 * @param toCopy The notional period to copy
	 */
	public NotionalPeriod(NotionalPeriod toCopy) {
		super(toCopy);
		notional = toCopy.notional;
	}

	/**
	 * Constructor
	 * @param notional The notional to use
	 */
	public NotionalPeriod(Notional notional) {
		this.notional = notional;
	}

	/**
	 * Constructor
	 * @param amount The amount to use
	 * @param currency The currency to use
	 */
	public NotionalPeriod(double amount, Iso4217Currency currency) {
		notional = new Notional(amount,currency);
	}

	/**
	 * Constructor
	 * @param startCalendar The start date as a calendar
	 * @param endCalendar The end date as a calendar
	 * @param notional The notional
	 */
	public NotionalPeriod(Calendar startCalendar, Calendar endCalendar, Notional notional) {
		super(startCalendar, endCalendar);
		this.notional = notional;
	}

	/**
	 * Constructor
	 * @param startCalendar The start date as a calendar
	 * @param endCalendar The end date as a calendar
	 * @param amount The amount
	 * @param currency The currency
	 */
	public NotionalPeriod(Calendar startCalendar, Calendar endCalendar, double amount, Iso4217Currency currency) {
		super(startCalendar, endCalendar);
		notional = new Notional(amount,currency);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public NotionalPeriod clone() {
		return new NotionalPeriod(this);
	}

	/**
	 * Gets the object representing the Notional
	 * @return The notional
	 */
	public Notional getNotional() {
		return notional;
	}

	/**
	 * Sets the object representing the Notional
	 * @param notional The notional
	 */
	public void setNotional(Notional notional) {
		this.notional = notional;
	}

	/**
	 * Gets the amount of the notional
	 * @return The amount
	 */
	public double getAmount() {
		return notional.getAmount();
	}

	/**
	 * Sets the amount of the notional
	 * @param amount The amount
	 */
	public void setAmount(double amount) {
		notional.setAmount(amount);
	}

	/**
	 * Gets the currency of the notional
	 * @return The currency
	 */
	public Iso4217Currency getCurrency() {
		return notional.getCurrency();
	}

	/**
	 * Sets the currency of the notional
	 * @param currency The currency
	 */
	public void setCurrency(Iso4217Currency currency) {
		notional.setCurrency(currency);
	}
}

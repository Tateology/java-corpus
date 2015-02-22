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

import java.util.Calendar;

/**
 * Models a single atomic payment
 */
public class Payment {
	private double amount;
	private Iso4217Currency currency;
	private Calendar paymentCalendar;


	/**
	 * Default constructor
	 */
	public Payment() {
	}

	/**
	 * Copy constructor
	 * @param toCopy The Payment to copy
	 */
	public Payment(Payment toCopy) {
		amount = toCopy.amount;
		currency = toCopy.currency;
		paymentCalendar = toCopy.paymentCalendar;
	}

	/**
	 * Return a clone of this Payment
	 * @return The cloned Payment
	 */
	public Payment clone() {
		return new Payment(this);
	}

	/**
	 * Utility constructor
	 * @param amount The amount of the payment
	 * @param currency The currency of the payment
	 * @param paymentCalendar The date of the payment
	 */
	public Payment(double amount, Iso4217Currency currency, Calendar paymentCalendar) {
		this.amount = amount;
		this.currency = currency;
		this.paymentCalendar = paymentCalendar;
	}

	/**
	 * Gets the amount of the payment
	 * @return The amount
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * Sets the amount of the payment
	 * @param amount The amount
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}

	/**
	 * Gets the currency of the payment
	 * @return The currency
	 */
	public Iso4217Currency getCurrency() {
		return currency;
	}

	/**
	 * Sets the currency of the payment
	 * @param currency The currency
	 */
	public void setCurrency(Iso4217Currency currency) {
		this.currency = currency;
	}

	/**
	 * Gets the date of the payment
	 * @return The date
	 */
	public Calendar getPaymentCalendar() {
		return paymentCalendar;
	}

	/**
	 * Sets the date of the payment
	 * @param paymentCalendar The date
	 */
	public void setPaymentCalendar(Calendar paymentCalendar) {
		this.paymentCalendar = paymentCalendar;
	}

	/**
	 * Return true if this Payment is equal to the given payment, ignoring the amount field
	 * @param payment The payment to compare
	 * @return True if they are equal, otherwise false
	 */
	public boolean equalsIgnoringAmount(Payment payment) {
		return areCalendarsSameDate(paymentCalendar,payment.paymentCalendar) && currency==payment.currency;
	}

	/**
	 * Return true if the calendars represent the same date, irrespective of times
	 * @param c1 The first calendar to compare
	 * @param c2 The second calendar to compare
	 * @return True if they represent the same date, otherwise false
	 */
	public boolean areCalendarsSameDate(Calendar c1, Calendar c2) {
		return 	c1.get(Calendar.DAY_OF_MONTH)==c2.get(Calendar.DAY_OF_MONTH) &&
				c1.get(Calendar.MONTH)==c2.get(Calendar.MONTH) &&
				c1.get(Calendar.YEAR)==c2.get(Calendar.YEAR);

	}
}

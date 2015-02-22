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

package org.jfin.date.accrual.impl;

import org.jfin.date.accrual.NotionalSchedule;
import org.jfin.date.accrual.NotionalPeriod;
import org.jfin.date.accrual.Notional;
import org.jfin.common.Iso4217Currency;

import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;

/**
 * Concrete implementation of NotionalSchedule where there is a single
 * unchanging notional
 */
public class FlatNotionalSchedule implements NotionalSchedule {

	private Notional notional;

	public FlatNotionalSchedule() {

	}

	public FlatNotionalSchedule(Notional notional) {
		this.notional = notional;
	}

	public FlatNotionalSchedule(double amount, Iso4217Currency currency) {
		notional = new Notional(amount,currency);
	}


	public double getAmount() {
		return notional.getAmount();
	}

	public void setAmount(double amount) {
		notional.setAmount(amount);
	}

	public Iso4217Currency getCurrency() {
		return notional.getCurrency();
	}

	public void setCurrency(Iso4217Currency currency) {
		notional.setCurrency(currency);
	}

	public List<NotionalPeriod> getNotionalPeriodsBetween(Calendar startDate, Calendar endDate) {
		List<NotionalPeriod> resultantNotionalPeriods = new ArrayList<NotionalPeriod>();

		NotionalPeriod notionalPeriod = new NotionalPeriod(startDate,endDate,notional);

		resultantNotionalPeriods.add(notionalPeriod);

		return resultantNotionalPeriods;
	}
}

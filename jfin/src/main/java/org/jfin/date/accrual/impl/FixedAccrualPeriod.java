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

import org.jfin.date.Period;
import org.jfin.date.accrual.AccrualPeriod;


/**
 * Concrete implementation of AccrualPeriod that models
 * a Fixed Rate accrual period (e.g. a fixed rate Bond)
 */
public class FixedAccrualPeriod extends AccrualPeriod {
	private double fixedRate;

	/**
	 * Default constructor
	 */
	public FixedAccrualPeriod() {
	}

	/**
	 * Copy constructor
	 * @param toCopy The period to copy
	 */
	public FixedAccrualPeriod(Period toCopy) {
		super(toCopy);
		if(toCopy instanceof FixedAccrualPeriod) {
			FixedAccrualPeriod fixedAccrualPeriodToCopy = (FixedAccrualPeriod)toCopy;
			fixedRate = fixedAccrualPeriodToCopy.fixedRate;
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public FixedAccrualPeriod clone() {
		return new FixedAccrualPeriod(this);
	}

	/**
	 * Returns the fixed rate of this period
	 * @return The fixed rate
	 */
	public double getFixedRate() {
		return fixedRate;
	}

	/**
	 * Sets the fixed rate of this period
	 * @param fixedRate The fixed rate
	 */
	public void setFixedRate(double fixedRate) {
		this.fixedRate = fixedRate;
	}

	/**
	 * Gets the payment amount for this period for a given notional
	 * @param notional The notional amount
	 * @return The payment amount
	 * @throws Exception If there is a problem calculating the payment amount
	 */
	public double getPaymentAmount(double notional) throws Exception {
		return getAdjustedDaycountFraction()*fixedRate*notional;
	}

	public boolean isPaymentPossible() {
		return true;
	}
}

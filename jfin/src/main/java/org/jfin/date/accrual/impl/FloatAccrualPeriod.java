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

import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Period;
import org.jfin.date.accrual.AccrualPeriod;
import org.jfin.date.accrual.AccrualException;
import org.jfin.date.holiday.HolidayCalendar;

import java.util.Calendar;

/**
 * Concrete implementation of AccrualPeriod that
 * models a floating rate period which requires fixing
 * (e.g. the float leg of a Swap).
 */
public class FloatAccrualPeriod extends AccrualPeriod {
	private Calendar fixingCalendar;
	private BusinessDayConvention fixingBusinessDayConvention;
	private HolidayCalendar fixingHolidayCalendar;
	private int fixingCalendarOffsetDays;
	private double fixingRate = Double.NaN;
	private FixingAdjustmentType fixingAdjustmentType;


	/**
	 * Default constructor
	 */
	public FloatAccrualPeriod() {
	}

	/**
	 * Copy constructor
	 * @param toCopy The float accrual period to copy
	 */
	protected FloatAccrualPeriod(Period toCopy) {
		super(toCopy);
		if(toCopy instanceof FloatAccrualPeriod) {
			FloatAccrualPeriod floatAccrualPeriodToCopy = (FloatAccrualPeriod)toCopy;
			fixingCalendar = floatAccrualPeriodToCopy.fixingCalendar;
			fixingBusinessDayConvention = floatAccrualPeriodToCopy.fixingBusinessDayConvention;
			fixingHolidayCalendar = floatAccrualPeriodToCopy.fixingHolidayCalendar;
			fixingCalendarOffsetDays = floatAccrualPeriodToCopy.fixingCalendarOffsetDays;
			fixingRate = floatAccrualPeriodToCopy.fixingRate;
			fixingAdjustmentType = floatAccrualPeriodToCopy.fixingAdjustmentType;
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public FloatAccrualPeriod clone() {
		return new FloatAccrualPeriod(this);
	}

	/**
	 * Initialises this period setting (if possible) the fixing date
	 * based upon the start date, fixing holiday calendar and fixing
	 * offset amount.
	 */
	public void initialise() {
		super.initialise();


		if(fixingHolidayCalendar!=null && getPaymentCalendar()!=null && fixingAdjustmentType!=null) {
			if(fixingAdjustmentType==FixingAdjustmentType.BUSINESS_DAYS) {
				fixingCalendar = fixingHolidayCalendar.advanceBusinessDays(getPaymentCalendar(),fixingCalendarOffsetDays);
			} else {
				fixingCalendar = fixingHolidayCalendar.advance(getPaymentCalendar(),fixingCalendarOffsetDays,Calendar.DAY_OF_MONTH,BusinessDayConvention.UNADJUSTED);
			}
		}
	}

	/**
	 * Returns the payment amount for this period based upon the provided notional
	 * @param notional The notional amount
	 * @return The payment amount
	 * @throws Exception Throws an exception if the daycount fraction cannot be calculated or if the fixingRate has not been set (isNaN)
	 */
	public double getPaymentAmount(double notional) throws Exception {
		if(Double.isNaN(fixingRate)) {
			throw new AccrualException("Attempt to get payment amount for a FloatAccrualPeriod where the fixingRate has not been fixed.");
		}
		return getAdjustedDaycountFraction()*getFixingRate()*notional;
	}

	public boolean isPaymentPossible() {
		return !Double.isNaN(getFixingRate());
	}

	/**
	 * Gets the adjusted fixing date
	 * @return The adjusted fixing date as a calendar
	 * @throws AccrualException If there is a problem creating the adjusted fixing calendar
	 */
	public Calendar getAdjustedFixingCalendar() throws AccrualException {
		if(fixingCalendar==null) {
			throw new AccrualException("Could not get adjusted fixing calendar where the fixing calendar is null.");
		}

		if(fixingBusinessDayConvention==null) {
			throw new AccrualException("Could not get adjusted fixing calendar where the fixing business day convention is null.");
		}

		if(fixingHolidayCalendar==null) {
			throw new AccrualException("Could not get adjusted fixing calendar where the fixing holiday calendar is null.");
		}

		return fixingHolidayCalendar.adjust(fixingCalendar,fixingBusinessDayConvention);
	}

	/**
	 * Gets the unadjusted fixing date
	 * @return The fixing date as a calendar
	 */
	public Calendar getFixingCalendar() {
		return fixingCalendar;
	}

	/**
	 * Sets the unadjusted fixing date
	 * @param fixingCalendar The fixing date as a calendar
	 */
	public void setFixingCalendar(Calendar fixingCalendar) {
		this.fixingCalendar = fixingCalendar;
	}

	/**
	 * Gets the fixing adjustment type
	 * @return The fixing adjustment type
	 */
	public FixingAdjustmentType getFixingAdjustmentType() {
		return fixingAdjustmentType;
	}

	/**
	 * Sets the fixing adjustment type
	 * @param fixingAdjustmentType The fixing adjustment type
	 */
	public void setFixingAdjustmentType(FixingAdjustmentType fixingAdjustmentType) {
		this.fixingAdjustmentType = fixingAdjustmentType;
	}

	/**
	 * Gets the business day convention used for adjusting the fixing date
	 * @return The fixing business day convention
	 */
	public BusinessDayConvention getFixingBusinessDayConvention() {
		return fixingBusinessDayConvention;
	}

	/**
	 * Sets the business day convention used for adjusting the fixing date
	 * @param fixingBusinessDayConvention The fixing business day convention
	 */
	public void setFixingBusinessDayConvention(BusinessDayConvention fixingBusinessDayConvention) {
		this.fixingBusinessDayConvention = fixingBusinessDayConvention;
	}

	/**
	 * Gets the holiday calendar used for adjusting the fixing date
	 * @return The fixing holiday calendar
	 */
	public HolidayCalendar getFixingHolidayCalendar() {
		return fixingHolidayCalendar;
	}

	/**
	 * Sets the holiday calendar used for adjusting the fixing date
	 * @param fixingHolidayCalendar The fixing holiday calendar
	 */
	public void setFixingHolidayCalendar(HolidayCalendar fixingHolidayCalendar) {
		this.fixingHolidayCalendar = fixingHolidayCalendar;
	}

	/**
	 * Gets the offset from the payment date (in business days) for the
	 * fixing date, typically this will be 0 (for GBP LIBOR) or -2 (for other
	 * indexes)
	 * @return The fixing offset amount
	 */
	public int getFixingCalendarOffsetAmount() {
		return fixingCalendarOffsetDays;
	}

	/**
	 * Sets the offset from the payment date (in business days) for the
	 * fixing date, typically this will be 0 (for GBP LIBOR) or -2 (for other
	 * indexes)
	 * @param fixingCalendarOffsetAmount The fixing offset amount
	 */
	public void setFixingCalendarOffsetAmount(int fixingCalendarOffsetAmount) {
		this.fixingCalendarOffsetDays = fixingCalendarOffsetAmount;
	}

	/**
	 * Gets the fixing rate which has been applied to to this period or
	 * Double.NaN if the rate is not set.
	 * @return The fixing rate
	 */
	public double getFixingRate() {
		return fixingRate;
	}

	/**
	 * Sets the fixing rate which has been applied to to this period or
	 * Double.NaN if the rate is not set
	 * @param fixingRate The fixing rate
	 */
	public void setFixingRate(double fixingRate) {
		this.fixingRate = fixingRate;
	}
}

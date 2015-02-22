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
import org.jfin.date.Initialisable;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.daycount.DaycountCalculator;

import java.util.Calendar;

/**
 * Abstract class that provides the basis of concrete accrual periods.
 */
public abstract class AccrualPeriod extends Period implements Initialisable {

	private Calendar paymentCalendar;
	private PaymentType paymentType;
	private BusinessDayConvention paymentBusinessDayConvention;
	private BusinessDayConvention accrualBusinessDayConvention;
	private HolidayCalendar paymentHolidayCalendar;
	private HolidayCalendar accrualHolidayCalendar;
	private DaycountCalculator daycountCalculator;

	/**
	 * Default constructor
	 */
	public AccrualPeriod() {
	}

	/**
	 * Copy constructor
	 * @param toCopy the Period to copy
	 */
	public AccrualPeriod(Period toCopy) {
		super(toCopy);
		if(toCopy instanceof AccrualPeriod) {
			AccrualPeriod accrualPeriodToCopy = (AccrualPeriod)toCopy;
			if(accrualPeriodToCopy.paymentCalendar!=null) {
				paymentCalendar = (Calendar)accrualPeriodToCopy.paymentCalendar.clone();
			}

			paymentType = accrualPeriodToCopy.paymentType;

			paymentBusinessDayConvention = accrualPeriodToCopy.paymentBusinessDayConvention;
			paymentHolidayCalendar = accrualPeriodToCopy.paymentHolidayCalendar;

			daycountCalculator = accrualPeriodToCopy.daycountCalculator;

			accrualBusinessDayConvention = accrualPeriodToCopy.accrualBusinessDayConvention;
			accrualHolidayCalendar = accrualPeriodToCopy.accrualHolidayCalendar;
		}
	}

	/**
	 * Initialises the payment date after the start and end dates have been
	 * set.
	 */
	public void initialise() {
		if(paymentType!=null) {
			switch(paymentType) {
				case IN_ADVANCE:
					setPaymentCalendar(getStartCalendar());
					break;
				case IN_ARREARS:
					setPaymentCalendar(getEndCalendar());
					break;
			}
		}
	}

	/**
	 * Return the payment amount for a given notional
	 * @param notional The notional
	 * @return The payment amount for the given notional
	 * @throws Exception Throws an Exception if there are problems calculating the payment amount
	 */
	public abstract double getPaymentAmount(double notional) throws Exception;

	public abstract boolean isPaymentPossible();

	/**
	 * Gets the payment date
	 * @return Calendar representing the payment date
	 */
	public Calendar getPaymentCalendar() {
		return paymentCalendar;
	}

	public Calendar getAdjustedPaymentCalendar() throws AccrualException {
		if(paymentBusinessDayConvention==null) {
			throw new AccrualException("Could not get adjusted payment calendar where the payment business day convention is null.");
		}

		if(paymentBusinessDayConvention==BusinessDayConvention.UNADJUSTED) {
			return getPaymentCalendar();
		}

		if(getPaymentCalendar()==null) {
			throw new AccrualException("Could not get adjusted payment calendar where the payment calendar is null.");
		}

		if(paymentHolidayCalendar==null) {
			throw new AccrualException("Could not get adjusted payment calendar where the payment holiday calendar is null.");
		}

		return paymentHolidayCalendar.adjust(getPaymentCalendar(),paymentBusinessDayConvention);
	}

	/**
	 * Sets the payment date
	 * @param paymentCalendar The Calendar representing the payment date
	 */
	public void setPaymentCalendar(Calendar paymentCalendar) {
		this.paymentCalendar = paymentCalendar;
	}

	/**
	 * Gets the payment type (IN_ARREARS, IN_ADVANCE) which determines whether
	 * the payment date is the start of the period (IN_ADVANCE) or the end of the
	 * period (IN_ARREARS).
	 * @return The payment type
	 */
	public PaymentType getPaymentType() {
		return paymentType;
	}

	/**
	 * Sets the payment type  (IN_ARREARS, IN_ADVANCE) which determines whether
	 * the payment date is the start of the period (IN_ADVANCE) or the end of the
	 * period (IN_ARREARS).
	 * @param paymentType The payment type
	 */
	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	/**
	 * Gets the business day convention used to adjust the
	 * payment date.
	 * @return The  payment business day convention
	 */
	public BusinessDayConvention getPaymentBusinessDayConvention() {
		return paymentBusinessDayConvention;
	}

	/**
	 * Sets the business day convention used to adjust the
	 * payment date.
	 * @param paymentBusinessDayConvention The payment business day convention
	 */
	public void setPaymentBusinessDayConvention(BusinessDayConvention paymentBusinessDayConvention) {
		this.paymentBusinessDayConvention = paymentBusinessDayConvention;
	}

	/**
	 * Gets the accrual business day convention
	 * @return The accrual business day convention
	 */
	public BusinessDayConvention getAccrualBusinessDayConvention() {
		return accrualBusinessDayConvention;
	}

	/**
	 * Sets the accrual business day convention
	 * @param accrualBusinessDayConvention The accrual business day convention
	 */
	public void setAccrualBusinessDayConvention(BusinessDayConvention accrualBusinessDayConvention) {
		this.accrualBusinessDayConvention = accrualBusinessDayConvention;
	}

	/**
	 * Gets the holiday calendar used to adjust the
	 * payment date.
	 * @return The payment holiday calendar
	 */
	public HolidayCalendar getPaymentHolidayCalendar() {
		return paymentHolidayCalendar;
	}

	/**
	 * Sets the holiday calendar used to adjust the
	 * payment date.
	 * @param paymentHolidayCalendar The payment holiday calendar
	 */
	public void setPaymentHolidayCalendar(HolidayCalendar paymentHolidayCalendar) {
		this.paymentHolidayCalendar = paymentHolidayCalendar;
	}

	/**
	 * Gets the accrual holiday calendar
	 * @return The accrual holiday calendar
	 */
	public HolidayCalendar getAccrualHolidayCalendar() {
		return accrualHolidayCalendar;
	}

	/**
	 * Sets the accrual holiday calendar
	 * @param accrualHolidayCalendar The accrual holiday calendar
	 */
	public void setAccrualHolidayCalendar(HolidayCalendar accrualHolidayCalendar) {
		this.accrualHolidayCalendar = accrualHolidayCalendar;
	}

	/**
	 * Gets the day count calculator used to calculate the value of the
	 * accrual period.
	 * @return The day count calculator
	 */
	public DaycountCalculator getDaycountCalculator() {
		return daycountCalculator;
	}

	/**
	 * Sets the day count calculator used to calculate the value of the
	 * accrual period.
	 * @param daycountCalculator The day count calculator
	 */
	public void setDaycountCalculator(DaycountCalculator daycountCalculator) {
		this.daycountCalculator = daycountCalculator;
	}

	/**
	 * Returns the start calendar adjusted based upon the accrual holiday calendar and
	 * business day convention.
	 * @return The adjusted start calendar
	 * @throws AccrualException If the start calendar, payment business day convention or payment holiday are null
	 */
	public Calendar getAdjustedStartCalendar() throws AccrualException {


		if(accrualBusinessDayConvention==null) {
			throw new AccrualException("Could not get adjusted start calendar where the accrual business day convention is null.");
		}

		if(accrualBusinessDayConvention==BusinessDayConvention.UNADJUSTED) {
			return getStartCalendar();
		}

		if(getStartCalendar()==null) {
			throw new AccrualException("Could not get adjusted start calendar where the start calendar is null.");
		}

		if(accrualHolidayCalendar==null) {
			throw new AccrualException("Could not get adjusted start calendar where the accrual holiday calendar is null.");
		}

		return accrualHolidayCalendar.adjust(getStartCalendar(),accrualBusinessDayConvention);
	}

	/**
	 * Returns the end calendar adjusted based upon the accrual holiday calendar and
	 * business day convention.
	 * @return The adjusted end calendar
	 * @throws AccrualException If the start calendar, payment business day convention or payment holiday are null
	 */
	public Calendar getAdjustedEndCalendar() throws AccrualException {

		if(accrualBusinessDayConvention==null) {
			throw new AccrualException("Could not get adjusted end calendar where the accrual business day convention is null.");
		}

		if(accrualBusinessDayConvention==BusinessDayConvention.UNADJUSTED) {
			return getEndCalendar();
		}

		if(getEndCalendar()==null) {
			throw new AccrualException("Could not get adjusted end calendar where the end calendar is null.");
		}

		if(accrualHolidayCalendar==null) {
			throw new AccrualException("Could not get adjusted end calendar where the accrual holiday calendar is null.");
		}
		return accrualHolidayCalendar.adjust(getEndCalendar(),accrualBusinessDayConvention);
	}

	/**
	 * Gets the daycount fraction for the adjusted period.
	 * @return The day count fraction of the adjusted accrual period
	 * @throws AccrualException If there is a problem creating the adjusted day count fraction
	 */
	public double getAdjustedDaycountFraction() throws AccrualException {
		return daycountCalculator.calculateDaycountFraction(getAdjustedStartCalendar(),getAdjustedEndCalendar());
	}
}

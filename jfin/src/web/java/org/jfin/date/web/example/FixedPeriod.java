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

package org.jfin.date.web.example;

import java.util.Calendar;

import org.jfin.common.Iso4217Currency;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Period;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.holiday.HolidayCalendar;

public class FixedPeriod extends Period
{
	private double notional;
	private Iso4217Currency currency;
	private double fixedRate;
	private BusinessDayConvention businessDayConvention;
	private DaycountCalculator daycountCalculator;
	private HolidayCalendar paymentHolidayCalendar;



	/* (non-Javadoc)
	 * @see org.jfin.date.Period#clone()
	 */
	@Override
	public FixedPeriod clone()
	{
		FixedPeriod period = new FixedPeriod();

		period.setStartCalendar(getStartCalendar());
		period.setEndCalendar(getEndCalendar());
		period.setReferenceStartCalendar(getReferenceStartCalendar());
		period.setReferenceEndCalendar(getReferenceEndCalendar());
		period.notional = notional;
		period.currency = currency;
		period.fixedRate = fixedRate;
		period.businessDayConvention = businessDayConvention;
		period.daycountCalculator = daycountCalculator;
		period.paymentHolidayCalendar = paymentHolidayCalendar;
		return period;
	}
	/**
	 * @return Returns the businessDayConvention.
	 */
	public BusinessDayConvention getBusinessDayConvention()
	{
		return businessDayConvention;
	}
	/**
	 * @param businessDayConvention The businessDayConvention to set.
	 */
	public void setBusinessDayConvention(BusinessDayConvention businessDayConvention)
	{
		this.businessDayConvention = businessDayConvention;
	}
	/**
	 * @return Returns the currency.
	 */
	public Iso4217Currency getCurrency()
	{
		return currency;
	}
	/**
	 * @param currency The currency to set.
	 */
	public void setCurrency(Iso4217Currency currency)
	{
		this.currency = currency;
	}
	/**
	 * @return Returns the daycountCalculator.
	 */
	public DaycountCalculator getDaycountCalculator()
	{
		return daycountCalculator;
	}
	/**
	 * @param daycountCalculator The daycountCalculator to set.
	 */
	public void setDaycountCalculator(DaycountCalculator daycountCalculator)
	{
		this.daycountCalculator = daycountCalculator;
	}
	/**
	 * @return Returns the fixedRate.
	 */
	public double getFixedRate()
	{
		return fixedRate;
	}
	/**
	 * @param fixedRate The fixedRate to set.
	 */
	public void setFixedRate(double fixedRate)
	{
		this.fixedRate = fixedRate;
	}
	/**
	 * @return Returns the paymentHolidayCalendar.
	 */
	public HolidayCalendar getPaymentHolidayCalendar()
	{
		return paymentHolidayCalendar;
	}
	/**
	 * @param paymentHolidayCalendar The paymentHolidayCalendar to set.
	 */
	public void setPaymentHolidayCalendar(HolidayCalendar holidayCalendar)
	{
		this.paymentHolidayCalendar = holidayCalendar;
	}
	/**
	 * @return Returns the notional.
	 */
	public double getNotional()
	{
		return notional;
	}
	/**
	 * @param notional The notional to set.
	 */
	public void setNotional(double notional)
	{
		this.notional = notional;
	}

	public Calendar getAdjustedStartCalendar() {
		return paymentHolidayCalendar.adjust(getStartCalendar(),getBusinessDayConvention());
	}

	public Calendar getAdjustedEndCalendar() {
		return paymentHolidayCalendar.adjust(getEndCalendar(),getBusinessDayConvention());
	}

	public double getDaycountFraction() {
		return daycountCalculator.calculateDaycountFraction(getAdjustedStartCalendar(), getAdjustedEndCalendar());
	}

	public double getPayment() {
		return getDaycountFraction()*getNotional()*getFixedRate();
	}

	public Calendar getPaymentDate() {
		return paymentHolidayCalendar.adjust(getAdjustedStartCalendar(),BusinessDayConvention.MODIFIED_FOLLOWING);
	}

}

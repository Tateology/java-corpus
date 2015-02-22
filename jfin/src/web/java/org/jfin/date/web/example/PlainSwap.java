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
import java.util.List;

import org.jfin.common.Iso4217Currency;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Frequency;
import org.jfin.date.ScheduleException;
import org.jfin.date.ScheduleGenerator;
import org.jfin.date.StubType;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.holiday.HolidayCalendar;

public class PlainSwap
{
	private double notional;
	private Iso4217Currency currency;
	private Calendar tradeDate;
	private Calendar effectiveDate;
	private Calendar maturityDate;
	private StubType stubType;
	private DaycountCalculator fixedDaycountCalculator;
	private DaycountCalculator floatDaycountCalculator;
	private Frequency fixedFrequency;
	private Frequency floatFrequency;
	private BusinessDayConvention fixedConvention;
	private BusinessDayConvention floatConvention;


	private boolean payFixed;
	private double fixedRate;
	private String floatIndex;
	private double floatMargin;
	private int fixingOffset;
	private HolidayCalendar fixingBusinessDays;
	private HolidayCalendar paymentBusinessDays;

	private static int MAX_PERIODS = 200;

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
	 * @return Returns the effectiveDate.
	 */
	public Calendar getEffectiveDate()
	{
		return effectiveDate;
	}
	/**
	 * @param effectiveDate The effectiveDate to set.
	 */
	public void setEffectiveDate(Calendar effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}
	/**
	 * @return Returns the fixedConvention.
	 */
	public BusinessDayConvention getFixedConvention()
	{
		return fixedConvention;
	}
	/**
	 * @param fixedConvention The fixedConvention to set.
	 */
	public void setFixedConvention(BusinessDayConvention fixedConvention)
	{
		this.fixedConvention = fixedConvention;
	}
	/**
	 * @return Returns the fixedDaycountCalculator.
	 */
	public DaycountCalculator getFixedDaycountCalculator()
	{
		return fixedDaycountCalculator;
	}
	/**
	 * @param fixedDaycountCalculator The fixedDaycountCalculator to set.
	 */
	public void setFixedDaycountCalculator(
			DaycountCalculator fixedDaycountCalculator)
	{
		this.fixedDaycountCalculator = fixedDaycountCalculator;
	}
	/**
	 * @return Returns the fixedFrequency.
	 */
	public Frequency getFixedFrequency()
	{
		return fixedFrequency;
	}
	/**
	 * @param fixedFrequency The fixedFrequency to set.
	 */
	public void setFixedFrequency(Frequency fixedFrequency)
	{
		this.fixedFrequency = fixedFrequency;
	}
	/**
	 * @return Returns the floatConvention.
	 */
	public BusinessDayConvention getFloatConvention()
	{
		return floatConvention;
	}
	/**
	 * @param floatConvention The floatConvention to set.
	 */
	public void setFloatConvention(BusinessDayConvention floatConvention)
	{
		this.floatConvention = floatConvention;
	}
	/**
	 * @return Returns the floatDaycountCalculator.
	 */
	public DaycountCalculator getFloatDaycountCalculator()
	{
		return floatDaycountCalculator;
	}
	/**
	 * @param floatDaycountCalculator The floatDaycountCalculator to set.
	 */
	public void setFloatDaycountCalculator(
			DaycountCalculator floatDaycountCalculator)
	{
		this.floatDaycountCalculator = floatDaycountCalculator;
	}
	/**
	 * @return Returns the floatFrequency.
	 */
	public Frequency getFloatFrequency()
	{
		return floatFrequency;
	}
	/**
	 * @param floatFrequency The floatFrequency to set.
	 */
	public void setFloatFrequency(Frequency floatFrequency)
	{
		this.floatFrequency = floatFrequency;
	}
	/**
	 * @return Returns the maturityDate.
	 */
	public Calendar getMaturityDate()
	{
		return maturityDate;
	}
	/**
	 * @param maturityDate The maturityDate to set.
	 */
	public void setMaturityDate(Calendar maturityDate)
	{
		this.maturityDate = maturityDate;
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
	/**
	 * @return Returns the stubType.
	 */
	public StubType getStubType()
	{
		return stubType;
	}
	/**
	 * @param stubType The stubType to set.
	 */
	public void setStubType(StubType stubType)
	{
		this.stubType = stubType;
	}
	/**
	 * @return Returns the tradeDate.
	 */
	public Calendar getTradeDate()
	{
		return tradeDate;
	}
	/**
	 * @param tradeDate The tradeDate to set.
	 */
	public void setTradeDate(Calendar tradeDate)
	{
		this.tradeDate = tradeDate;
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
	 * @return Returns the fixingBusinessDays.
	 */
	public HolidayCalendar getFixingBusinessDays()
	{
		return fixingBusinessDays;
	}
	/**
	 * @param fixingBusinessDays The fixingBusinessDays to set.
	 */
	public void setFixingBusinessDays(HolidayCalendar fixingBusinessDays)
	{
		this.fixingBusinessDays = fixingBusinessDays;
	}
	/**
	 * @return Returns the fixingOffset.
	 */
	public int getFixingOffset()
	{
		return fixingOffset;
	}
	/**
	 * @param fixingOffset The fixingOffset to set.
	 */
	public void setFixingOffset(int fixingOffset)
	{
		this.fixingOffset = fixingOffset;
	}
	/**
	 * @return Returns the floatIndex.
	 */
	public String getFloatIndex()
	{
		return floatIndex;
	}
	/**
	 * @param floatIndex The floatIndex to set.
	 */
	public void setFloatIndex(String floatIndex)
	{
		this.floatIndex = floatIndex;
	}
	/**
	 * @return Returns the floatMargin.
	 */
	public double getFloatMargin()
	{
		return floatMargin;
	}
	/**
	 * @param floatMargin The floatMargin to set.
	 */
	public void setFloatMargin(double floatMargin)
	{
		this.floatMargin = floatMargin;
	}
	/**
	 * @return Returns the payFixed.
	 */
	public boolean isPayFixed()
	{
		return payFixed;
	}
	/**
	 * @param payFixed The payFixed to set.
	 */
	public void setPayFixed(boolean payFixed)
	{
		this.payFixed = payFixed;
	}
	/**
	 * @return Returns the paymentBusinessDays.
	 */
	public HolidayCalendar getPaymentBusinessDays()
	{
		return paymentBusinessDays;
	}
	/**
	 * @param paymentBusinessDays The paymentBusinessDays to set.
	 */
	public void setPaymentBusinessDays(HolidayCalendar paymentBusinessDays)
	{
		this.paymentBusinessDays = paymentBusinessDays;
	}
	public List getFixedSchedule() throws ScheduleException {
		FixedPeriod prototype = new FixedPeriod();
		prototype.setNotional(payFixed ? -1*getNotional() : getNotional());
		prototype.setCurrency(getCurrency());
		prototype.setFixedRate(getFixedRate());
		prototype.setBusinessDayConvention(getFixedConvention());
		prototype.setDaycountCalculator(getFixedDaycountCalculator());
		prototype.setPaymentHolidayCalendar(getPaymentBusinessDays());

		List periods = ScheduleGenerator.generateSchedule(getEffectiveDate(), getMaturityDate(), getFixedFrequency(), getStubType(), prototype,MAX_PERIODS);

		return periods;
	}

	public List getFloatSchedule() throws ScheduleException {
		FloatPeriod prototype = new FloatPeriod();
		prototype.setNotional(payFixed ? getNotional() : -1*getNotional());
		prototype.setCurrency(getCurrency());
		prototype.setBusinessDayConvention(getFloatConvention());
		prototype.setDaycountCalculator(getFloatDaycountCalculator());
		prototype.setPaymentHolidayCalendar(getPaymentBusinessDays());
		prototype.setFixingHolidayCalendar(getFixingBusinessDays());
		prototype.setIndex(getFloatIndex());
		prototype.setFrequency(getFloatFrequency());
		prototype.setFixingOffset(getFixingOffset());

		List periods = ScheduleGenerator.generateSchedule(getEffectiveDate(), getMaturityDate(), getFloatFrequency(), getStubType(), prototype,MAX_PERIODS);

		return periods;
	}

}

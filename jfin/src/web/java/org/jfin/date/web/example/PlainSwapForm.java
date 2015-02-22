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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jfin.common.Iso4217Currency;
import org.jfin.date.BusinessDayConvention;
import org.jfin.date.Frequency;
import org.jfin.date.StubType;
import org.jfin.date.Tenor;
import org.jfin.date.daycount.DaycountCalculator;
import org.jfin.date.daycount.DaycountCalculatorFactory;
import org.jfin.date.holiday.HolidayCalendar;
import org.jfin.date.holiday.HolidayCalendarFactory;
import org.jfin.date.util.ISDADateFormat;

public class PlainSwapForm extends ActionForm
{
	private static final long serialVersionUID = 9222285577548343784L;

	private String notional;
	private String currency;
	private String tradeDate;
	private String effectiveDate;
	private String maturityDate;
	private String stubType;
	private String fixedDaycountCalculator;
	private String floatDaycountCalculator;
	private String fixedFrequency;
	private String floatFrequency;
	private String fixedConvention;
	private String floatConvention;

	private String payRec;
	private String fixedRate;
	private String floatIndex;
	private String floatMargin;
	private String fixingOffset;
	private List<String> fixingBusinessDays;
	private List<String> paymentBusinessDays;

	private static HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory.newInstance();
	private static DaycountCalculatorFactory daycountCalculatorFactory = DaycountCalculatorFactory.newInstance();



	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request)
	{
		ActionErrors errors = new ActionErrors();
		ActionMessages messages = new ActionMessages();


		if(notional==null || notional.length()==0) {
			messages.add("Test1",new ActionMessage("Notional cannot be empty."));
		} else {
			try {
				Double.parseDouble(notional);
			} catch(Exception e) {
				messages.add("Test2",new ActionMessage("Could not parse notional, must be a number."));
			}
		}

		errors.add(messages);
		return errors;
	}


	/* (non-Javadoc)
	 * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public void reset(ActionMapping arg0, HttpServletRequest arg1)
	{
		super.reset(arg0, arg1);
		notional = "1,000,000";
		tradeDate = ISDADateFormat.format(new GregorianCalendar());
		HolidayCalendar we = holidayCalendarFactory.getHolidayCalendar("WE");
		Calendar effectiveDateCalendaar = we.advanceBusinessDays(new GregorianCalendar(),2);
		effectiveDate = ISDADateFormat.format(effectiveDateCalendaar);
		maturityDate = "10Y";

		currency = "EUR";
		stubType = "SHORT_FIRST";
		fixedConvention = "UNADJUSTED";
		floatConvention = "MODIFIED_FOLLOWING";
		fixedFrequency = "QUARTERLY";
		floatFrequency = "SEMI_ANNUALLY";
		fixedDaycountCalculator = "Actual360";
		floatDaycountCalculator = "Actual360";


		payRec="PAY";
		fixedRate="5%";
		floatIndex="LIBOR";
		floatMargin="0";
		fixingOffset="-2";
	}

	public StubType[] getStubTypes() {
		StubType[] stubTypes = StubType.values();

		return stubTypes;
	}

	public String[] getFrequencies() {
		Frequency[] frequencies = Frequency.values();

		String[] names = new String[frequencies.length];

		for(int i=0;i<frequencies.length;i++) names[i] = frequencies[i].name();

		return names;
	}

	public String[] getBusinessDayConventions() {
		BusinessDayConvention[] conventions = BusinessDayConvention.values();

		String[] names = new String[conventions.length];

		for(int i=0;i<conventions.length;i++) names[i] = conventions[i].name();

		return names;
	}

	public String[] getDaycountCalculators() {
		return daycountCalculatorFactory.getAvailableDaycountCalculators();
	}

	public String[] getCurrencies() {
		return new String[] {
				"CHF",
				"EUR",
				"GBP",
				"JPY",
				"SEK",
				"USD"
		};
	}

	public String[] getPayRecOptions() {
		return new String[] {
				"PAY",
				"REC"
		};
	}


	/**
	 * @return Returns the currency.
	 */
	public String getCurrency()
	{
		return currency;
	}



	/**
	 * @param currency The currency to set.
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}



	/**
	 * @return Returns the effectiveDate.
	 */
	public String getEffectiveDate()
	{
		return effectiveDate;
	}



	/**
	 * @param effectiveDate The effectiveDate to set.
	 */
	public void setEffectiveDate(String effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}



	/**
	 * @return Returns the fixedConvention.
	 */
	public String getFixedConvention()
	{
		return fixedConvention;
	}



	/**
	 * @param fixedConvention The fixedConvention to set.
	 */
	public void setFixedConvention(String fixedConvention)
	{
		this.fixedConvention = fixedConvention;
	}



	/**
	 * @return Returns the fixedDaycountCalculator.
	 */
	public String getFixedDaycountCalculator()
	{
		return fixedDaycountCalculator;
	}



	/**
	 * @param fixedDaycountCalculator The fixedDaycountCalculator to set.
	 */
	public void setFixedDaycountCalculator(String fixedDaycountCalculator)
	{
		this.fixedDaycountCalculator = fixedDaycountCalculator;
	}



	/**
	 * @return Returns the fixedFrequency.
	 */
	public String getFixedFrequency()
	{
		return fixedFrequency;
	}



	/**
	 * @param fixedFrequency The fixedFrequency to set.
	 */
	public void setFixedFrequency(String fixedFrequency)
	{
		this.fixedFrequency = fixedFrequency;
	}



	/**
	 * @return Returns the floatConvention.
	 */
	public String getFloatConvention()
	{
		return floatConvention;
	}



	/**
	 * @param floatConvention The floatConvention to set.
	 */
	public void setFloatConvention(String floatConvention)
	{
		this.floatConvention = floatConvention;
	}



	/**
	 * @return Returns the floatDaycountCalculator.
	 */
	public String getFloatDaycountCalculator()
	{
		return floatDaycountCalculator;
	}



	/**
	 * @param floatDaycountCalculator The floatDaycountCalculator to set.
	 */
	public void setFloatDaycountCalculator(String floatDaycountCalculator)
	{
		this.floatDaycountCalculator = floatDaycountCalculator;
	}



	/**
	 * @return Returns the floatFrequency.
	 */
	public String getFloatFrequency()
	{
		return floatFrequency;
	}



	/**
	 * @param floatFrequency The floatFrequency to set.
	 */
	public void setFloatFrequency(String floatFrequency)
	{
		this.floatFrequency = floatFrequency;
	}



	/**
	 * @return Returns the maturityDate.
	 */
	public String getMaturityDate()
	{
		return maturityDate;
	}



	/**
	 * @param maturityDate The maturityDate to set.
	 */
	public void setMaturityDate(String maturityDate)
	{
		this.maturityDate = maturityDate;
	}



	/**
	 * @return Returns the notional.
	 */
	public String getNotional()
	{
		return notional;
	}



	/**
	 * @param notional The notional to set.
	 */
	public void setNotional(String notional)
	{
		this.notional = notional;
	}



	/**
	 * @return Returns the stubType.
	 */
	public String getStubType()
	{
		return stubType;
	}



	/**
	 * @param stubType The stubType to set.
	 */
	public void setStubType(String stubType)
	{
		this.stubType = stubType;
	}



	/**
	 * @return Returns the tradeDate.
	 */
	public String getTradeDate()
	{
		return tradeDate;
	}



	/**
	 * @param tradeDate The tradeDate to set.
	 */
	public void setTradeDate(String tradeDate)
	{
		this.tradeDate = tradeDate;
	}


	/**
	 * @return Returns the fixedRate.
	 */
	public String getFixedRate()
	{
		return fixedRate;
	}


	/**
	 * @param fixedRate The fixedRate to set.
	 */
	public void setFixedRate(String fixedRate)
	{
		this.fixedRate = fixedRate;
	}


	/**
	 * @return Returns the fixingBusinessDays.
	 */
	public List<String> getFixingBusinessDays()
	{
		return fixingBusinessDays;
	}




	/**
	 * @param fixingBusinessDays The fixingBusinessDays to set.
	 */
	public void setFixingBusinessDays(List<String> fixingBusinessDays)
	{
		this.fixingBusinessDays = fixingBusinessDays;
	}


	/**
	 * @return Returns the fixingOffset.
	 */
	public String getFixingOffset()
	{
		return fixingOffset;
	}


	/**
	 * @param fixingOffset The fixingOffset to set.
	 */
	public void setFixingOffset(String fixingOffset)
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
	public String getFloatMargin()
	{
		return floatMargin;
	}


	/**
	 * @param floatMargin The floatMargin to set.
	 */
	public void setFloatMargin(String floatMargin)
	{
		this.floatMargin = floatMargin;
	}


	/**
	 * @return Returns the paymentBusinessDays.
	 */
	public List<String> getPaymentBusinessDays()
	{
		return paymentBusinessDays;
	}


	/**
	 * @param paymentBusinessDays The paymentBusinessDays to set.
	 */
	public void setPaymentBusinessDays(List<String> paymentBusinessDays)
	{
		this.paymentBusinessDays = paymentBusinessDays;
	}


	/**
	 * @return Returns the payRec.
	 */
	public String getPayRec()
	{
		return payRec;
	}


	/**
	 * @param payRec The payRec to set.
	 */
	public void setPayRec(String payRec)
	{
		this.payRec = payRec;
	}


	public PlainSwap getPlainSwap() {
		PlainSwap plainSwap = new PlainSwap();

		try {

			plainSwap.setNotional(parseDouble(this.notional));
			plainSwap.setCurrency(Iso4217Currency.valueOf(this.currency));
			plainSwap.setTradeDate(parseTradeDate(this.tradeDate));
			plainSwap.setEffectiveDate(parseEffectiveDate(this.effectiveDate));
			plainSwap.setMaturityDate(parseMaturityDate(this.maturityDate));
			plainSwap.setStubType(StubType.valueOf(this.stubType));
			plainSwap.setFixedDaycountCalculator(parseDaycountCalculator(this.fixedDaycountCalculator));
			plainSwap.setFloatDaycountCalculator(parseDaycountCalculator(this.floatDaycountCalculator));
			plainSwap.setFixedFrequency(Frequency.valueOf(this.fixedFrequency));
			plainSwap.setFloatFrequency(Frequency.valueOf(this.floatFrequency));
			plainSwap.setFixedConvention(BusinessDayConvention.valueOf(this.fixedConvention));
			plainSwap.setFloatConvention(BusinessDayConvention.valueOf(this.floatConvention));

			plainSwap.setPayFixed(this.payRec!=null && this.payRec.equals("PAY"));

			plainSwap.setFixedRate(parsePercentage(this.fixedRate));
			plainSwap.setFloatIndex(this.floatIndex);
			plainSwap.setFloatMargin(parseDouble(this.floatMargin));

			plainSwap.setFixingOffset(parseInt(fixingOffset));

			HolidayCalendar we = holidayCalendarFactory.getHolidayCalendar("WE");

			plainSwap.setFixingBusinessDays(we);
			plainSwap.setPaymentBusinessDays(we);

		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}

		return plainSwap;
	}


	public int parseInt(String string)
	{
		return Integer.parseInt(string);
	}


	public DaycountCalculator parseDaycountCalculator(String string)
	{
		return daycountCalculatorFactory.getDaycountCalculator(string);
	}


	public Calendar parseMaturityDate(String string) throws ParseException
	{
		Calendar ret = null;
		try {
			return ISDADateFormat.parse(string);
		} catch(Exception e) {

			HolidayCalendar we = holidayCalendarFactory.getHolidayCalendar("WE");
			Calendar effectiveDate = parseEffectiveDate(getEffectiveDate());
			Tenor tenor = new Tenor(string);
			return we.advance(effectiveDate,tenor.getAmount(), tenor.getCalendarUnit(), BusinessDayConvention.UNADJUSTED);

		}
	}


	public Calendar parseEffectiveDate(String string) throws ParseException
	{
		return ISDADateFormat.parse(string);
	}


	public Calendar parseTradeDate(String string) throws ParseException
	{
		return ISDADateFormat.parse(string);
	}


	public double parseDouble(String string) throws ParseException
	{
		double multiplier = 1;

		if(string.toUpperCase().endsWith("B")) {
			string = string.substring(0,string.length()-1);
			multiplier = 1000000000;
		} else if(string.toUpperCase().endsWith("M")) {
			string = string.substring(0,string.length()-1);
			multiplier = 1000000;
		} else if(string.toUpperCase().endsWith("K")) {
			string = string.substring(0,string.length()-1);
			multiplier = 1000;
		} else if(string.toUpperCase().endsWith("BP")) {
			string = string.substring(0,string.length()-2);
			multiplier = .0001;
		}

		return NumberFormat.getNumberInstance().parse(string).doubleValue() *multiplier;

	}

	public double parsePercentage(String string) throws ParseException {
		try {
			return NumberFormat.getPercentInstance().parse(string).doubleValue();
		} catch(Exception e) {
			return parseDouble(string);
		}
	}

}

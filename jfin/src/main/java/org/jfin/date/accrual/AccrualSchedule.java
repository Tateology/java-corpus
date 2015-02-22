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

import org.jfin.date.ScheduleCutter;

import java.util.*;

/**
 * Used to manage and manipulate an AccrualSchedule and a NotionalSchedule
 */
public class AccrualSchedule<T extends AccrualPeriod, U extends Payment> {
	private List<T> accrualPeriods;
	private U paymentPrototype;

	/**
	 * Base constructor
	 * @param paymentPrototype A prototype of the payment
	 */
	public AccrualSchedule(U paymentPrototype) {
		this.paymentPrototype = paymentPrototype;
		accrualPeriods = new ArrayList<T>();
	}

	/**
	 * Returns a netted list of payments for a given notional schedule
	 * @param notionalSchedule The notional schedule
	 * @return A netted list of payments
	 * @throws Exception If there is a problem calculating the netted payments
	 */
	public List<U> getNettedPayments(NotionalSchedule notionalSchedule) throws Exception {
		return netPayments(getPayments(notionalSchedule));
	}

	/**
	 * Nets a list of payments together
	 * @param payments The payments to net together
	 * @return The payments netted together
	 */
	public List<U> netPayments(List<U> payments) {
		List<U> nettedPayments = new ArrayList<U>();

		for (U payment : payments) {
			nettedPayments.add((U) payment.clone());
		}

		for(int i=0;i<nettedPayments.size()-1;i++) {
			for(int j=i+1;j<nettedPayments.size();j++) {
				Payment paymenti = nettedPayments.get(i);
				Payment paymentj = nettedPayments.get(j);

				if(	paymenti.equalsIgnoringAmount(paymentj)) {

					paymenti.setAmount(paymenti.getAmount()+paymentj.getAmount());

					nettedPayments.remove(j);
				}
			}
		}

		return nettedPayments;
	}

	/**
	 * Gets a list of payments for a given notional schedule
	 * @param notionalSchedule The notional schedule
	 * @return A list of payments
	 * @throws Exception If there is a problem calculating the payments
	 */
	public List<U> getPayments(NotionalSchedule notionalSchedule) throws Exception {

		List<U> ret = new ArrayList<U>();

		List<NotionalPeriod> notionalPeriods = notionalSchedule.getNotionalPeriodsBetween(getStartCalendar(),getEndCalendar());

		ScheduleCutter<T> accrualScheduleCutter = new ScheduleCutter<T>();
		ScheduleCutter<NotionalPeriod> notionalScheduleCutter = new ScheduleCutter<NotionalPeriod>();

		List<T> cutAccrualPeriods = accrualScheduleCutter.cutSchedules(accrualPeriods,notionalPeriods);
		List<NotionalPeriod> cutNotionalPeriods = notionalScheduleCutter.cutSchedules(notionalPeriods,accrualPeriods);

		if(cutAccrualPeriods.size()!=cutNotionalPeriods.size()) {
			throw new AccrualException("Error cutting accrual and notional periods, resultant sizes are different.");
		}

		Iterator<T> cutAccrualPeriodsIterator = cutAccrualPeriods.iterator();
		Iterator<NotionalPeriod> cutNotionalPeriodsIterator = cutNotionalPeriods.iterator();

		while(cutAccrualPeriodsIterator.hasNext()) {
			T accrualPeriod = cutAccrualPeriodsIterator.next();
			NotionalPeriod notionalPeriod = cutNotionalPeriodsIterator.next();

			if(accrualPeriod.isPaymentPossible()) {
				double paymentAmount = accrualPeriod.getPaymentAmount(notionalPeriod.getAmount());

				U payment = (U)paymentPrototype.clone();
				payment.setAmount(paymentAmount);
				payment.setCurrency(notionalPeriod.getCurrency());
				payment.setPaymentCalendar(accrualPeriod.getPaymentCalendar());

				ret.add(payment);
			}
		}

		return ret;
	}

	/**
	 * Get the start date as a calendar for the periods
	 * @return The start calendar
	 * @throws AccrualException If there is a problem calculating the start calendar
	 */
	public Calendar getStartCalendar() throws AccrualException {
		if(accrualPeriods.size()==0) {
			throw new AccrualException("Attempt to get start calendar for an AccrualSchedule with no AccrualPeriods");
		}
		return accrualPeriods.get(0).getStartCalendar();
	}


	/**
	 * Get the end date as a calendar for the periods
	 * @return The end calendar
	 * @throws AccrualException If there is a problem calculating the start calendar
	 */
	public Calendar getEndCalendar() throws AccrualException {
		if(accrualPeriods.size()==0) {
			throw new AccrualException("Attempt to get end calendar for an AccrualSchedule with no AccrualPeriods");
		}
		return accrualPeriods.get(accrualPeriods.size()-1).getEndCalendar();
	}

	/**
	 * Gets the accrual periods
	 * @return The accrual periods
	 */
	public List<T> getAccrualPeriods() {
		return accrualPeriods;
	}

	/**
	 * Sets the accrual periods
	 * @param accrualPeriods The accrual periods
	 */
	public void setAccrualPeriods(List<T> accrualPeriods) {
		this.accrualPeriods = accrualPeriods;
	}

	/**
	 * Gets the size of the schedule (i.e. the number of accrual periods it contains)
	 * @return The number of accrual periods
	 */
	public int size() {
		return accrualPeriods.size();
	}

	/**
	 * Returns true if this schedule contains no accrual periods, otherwise false
	 * @return Whether the schedule is empty
	 */
	public boolean isEmpty() {
		return accrualPeriods.isEmpty();
	}

	/**
	 * Returns true if this schedule contains the period o
	 * @param o The period to check for
	 * @return True if the period is in this schedule, otherwise false
	 */
	public boolean contains(Object o) {
		return accrualPeriods.contains(o);
	}

	/**
	 * Returns an iterator of the accrual periods contained in this schedule
	 * @return An iterator of the accrual periods
	 */
	public Iterator<T> iterator() {
		return accrualPeriods.iterator();
	}

	/**
	 * Adds the accrual period to the schedule
	 * @param accrualPeriod
	 * @return True
	 */
	public boolean add(T accrualPeriod) {
		return accrualPeriods.add(accrualPeriod);
	}

	/**
	 * Removes the accrual period provided from the schedule
	 * @param o The accrual period to remove
	 * @return True if the object was removed, otherwise false
	 */
	public boolean remove(Object o) {
		return accrualPeriods.remove(o);
	}

	/**
	 * Determines whether or not this schedule contains all of the accrual periods in a collection.
	 * @param objects The collection of accrual periods to check for
	 * @return True if this schedule contains all of the accrual periods, otherwise false
	 */
	public boolean containsAll(Collection<?> objects) {
		return accrualPeriods.containsAll(objects);
	}

	/**
	 * Adds all of the provided accrual periods to the schedule
	 * @param ts The accrual periods to add
	 * @return True
	 */
	public boolean addAll(Collection<? extends T> ts) {
		return accrualPeriods.addAll(ts);
	}

	/**
	 * Adds all of the provided accrual periods to the schedule at a specific point
	 * @param i Where to insert the accrual periods
	 * @param ts The accrual periods to add
	 * @return True
	 */
	public boolean addAll(int i, Collection<? extends T> ts) {
		return accrualPeriods.addAll(i, ts);
	}

	/**
	 * Removes a collection of accrual periods from the schedule
	 * @param objects The accrual periods to remove
	 * @return True
	 */
	public boolean removeAll(Collection<?> objects) {
		return accrualPeriods.removeAll(objects);
	}

	/**
	 * Removes all but a collection of accrual periods from the schedule
	 * @param objects The accrual periods to retain
	 * @return true
	 */
	public boolean retainAll(Collection<?> objects) {
		return accrualPeriods.retainAll(objects);
	}

	/**
	 * Clears all accrual periods from the schedule
	 */
	public void clear() {
		accrualPeriods.clear();
	}

	/**
	 * Gets the accrual period at an index
	 * @param i The index
	 * @return The accrual period at index i
	 */
	public T get(int i) {
		return accrualPeriods.get(i);
	}

	/**
	 * Sets the accrual period at an index
	 * @param i The index
	 * @param t The accrual period
	 * @return The accrual period set
	 */
	public T set(int i, T t) {
		return accrualPeriods.set(i, t);
	}

	/**
	 * Inserts an accrual period at an index
	 * @param i The index
	 * @param t The accrual period
	 */
	public void add(int i, T t) {
		accrualPeriods.add(i, t);
	}

	/**
	 * Remove an accrual period at an index
	 * @param i The index
	 * @return The accrual period removed
	 */
	public T remove(int i) {
		return accrualPeriods.remove(i);
	}

	/**
	 * Get the index of a particular accrual period
	 * @param o The accrual period
	 * @return The index of the accrual period in the schedule, or -1 if the schedule does not contain the given period
	 */
	public int indexOf(Object o) {
		return accrualPeriods.indexOf(o);
	}

	/**
	 * Get the last index of a particular accrual period
	 * @param o The accrual period
	 * @return The last index of the accrual period in the schedule, or -1 if the schedule does not contain the given period
	 */
	public int lastIndexOf(Object o) {
		return accrualPeriods.lastIndexOf(o);
	}

	/**
	 * Takes a sub list of the accrual periods in a schedule between two indices
	 * @param i The start index
	 * @param i1 The end index
	 * @return The sub list of accrual periods
	 */
	public List<T> subList(int i, int i1) {
		return accrualPeriods.subList(i, i1);
	}
}

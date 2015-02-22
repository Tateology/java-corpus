<%--
  ~ <p> <b>jFin, open source derivatives trade processing</b> </p>
  ~
  ~ <p> Copyright (C) 2005, 2006, 2007 Morgan Brown Consultancy Ltd. </p>
  ~
  ~ <p> This file is part of jFin. </p>
  ~
  ~ <p> jFin is free software; you can redistribute it and/or modify it under the
  ~ terms of the GNU General Public License as published by the Free Software
  ~ Foundation; either version 2 of the License, or (at your option) any later
  ~ version. </p>
  ~
  ~ <p> jFin is distributed in the hope that it will be useful, but <b>WITHOUT
  ~ ANY WARRANTY</b>; without even the implied warranty of <b>MERCHANTABILITY</b>
  ~ or <b>FITNESS FOR A PARTICULAR PURPOSE</b>. See the GNU General Public
  ~ License for more details. </p>
  ~
  ~ <p> You should have received a copy of the GNU General Public License along
  ~ with jFin; if not, write to the Free Software Foundation, Inc., 51 Franklin
  ~ St, Fifth Floor, Boston, MA 02110-1301 USA. </p>
  --%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.jfin.date.web.example.*" %>
<%@ page import="org.jfin.date.util.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>jFin: PlainSwap Example</title>
	<link href="css/basic.css" rel="stylesheet" type="text/css" media="all" />

	<!-- ======================================================================

	  jFin, Java Financial Libraries

	  Copyright (C) 2005-2007 Morgan Brown Consultancy Ltd.

	  This file is part of jFin.

	  jFin is free software; you can redistribute it and/or modify it under the
	  terms of the GNU General Public License as published by the Free Software
	  Foundation; either version 2 of the License, or (at your option) any later
	  version.

	  jFin is distributed in the hope that it will be useful, but WITHOUT ANY
	  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
	  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

	  You should have received a copy of the GNU General Public License along with
	  jFin; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
	  Fifth Floor, Boston, MA 02110-1301 USA.

     ====================================================================== -->
</head>
<body>

<logic:present name="plainSwapForm" property="plainSwap">
	<bean:define id="plainSwap" name="plainSwapForm" property="plainSwap"/>


<table border="0">
<tr><td valign="top">
	<logic:present name="plainSwap" property="fixedSchedule">
		<bean:define id="fixedSchedule" name="plainSwap" property="fixedSchedule"/>
		<table border="0">
			<tr>
				<td colspan="4" class="heading">
					Fixed Schedule
				</td>
			</tr>
			<tr>
				<!--td class="datasubheading">
					Start
				</td>
				<td class="datasubheading">
					End
				</td-->
				<td class="datasubheading">
					Accrual<br/>Start
				</td>
				<td class="datasubheading">
					Accrual<br/>End
				</td>
				<!--td class="datasubheading">
					Daycount
				</td-->
				<td class="datasubheading">
					Payment<br/>(<bean:write name="plainSwapForm" property="currency"/>)
				</td>
				<td class="datasubheading">
					Payment<br/>Date
				</td>
			</tr>

			<logic:iterate id="period" name="fixedSchedule">
				<bean:define id="startDate" name="period" property="startCalendar"/>
				<bean:define id="endDate" name="period" property="endCalendar"/>
				<bean:define id="adjStartDate" name="period" property="adjustedStartCalendar"/>
				<bean:define id="adjEndDate" name="period" property="adjustedEndCalendar"/>
				<bean:define id="paymentDate" name="period" property="paymentDate"/>
				<bean:define id="daycount" name="period" property="daycountFraction"/>
				<bean:define id="payment" name="period" property="payment"/>
				<tr>
					<!-- Hide the start and end dates -->
					<!-- td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)startDate) %>
					</td>
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)endDate) %>
					</td -->
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)adjStartDate) %>
					</td>
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)adjEndDate) %>
					</td>
					<!-- Hide the day count fraction -->
					<!--td class="datanum">
						<bean:write name="period" property="daycountFraction" format="#.####"/>
					</td-->
					<td class="datanum">

							<bean:write name="period" property="payment" format="###,###.##"/>&nbsp;


					</td>
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)paymentDate) %>
					</td>
				</tr>
			</logic:iterate>
		</table>
		</logic:present>
		<logic:notPresent name="plainSwap" property="fixedSchedule">
			Could not create fixed schedule.
		</logic:notPresent>
		</td>
		<td valign="top">
		<logic:present name="plainSwap" property="floatSchedule">
			<bean:define id="floatSchedule" name="plainSwap" property="floatSchedule"/>
		<table border="0">
			<tr>
				<td colspan="5" class="heading">
					Float Schedule
				</td>
			</tr>
			<tr>
				<!-- td class="datasubheading">
					Start
				</td>
				<td class="datasubheading">
					End
				</td-->
				<td class="datasubheading">
					Accrual<br/>Start
				</td>
				<td class="datasubheading">
					Accrual<br/>End
				</td>
				<td class="datasubheading">
					Fixing<br/>Date
				</td>
				<td class="datasubheading">
					Payment<br/>Calculation
				</td>
				<td class="datasubheading">
					Payment<br/>Date
				</td>
			</tr>

			<logic:iterate id="period" name="floatSchedule">
				<bean:define id="startDate" name="period" property="startCalendar"/>
				<bean:define id="endDate" name="period" property="endCalendar"/>
				<bean:define id="adjStartDate" name="period" property="adjustedStartCalendar"/>
				<bean:define id="adjEndDate" name="period" property="adjustedEndCalendar"/>
				<bean:define id="fixingDate" name="period" property="fixingDate"/>
				<bean:define id="paymentDate" name="period" property="paymentDate"/>
				<bean:define id="paymentDescription" name="period" property="paymentDescription"/>
				<tr>
					<!-- td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)startDate) %>
					</td>
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)endDate) %>
					</td-->
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)adjStartDate) %>
					</td>
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)adjEndDate) %>
					</td>
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)fixingDate) %>
					</td>
					<td class="datanum">
						<bean:write name="period" property="paymentDescription"/>
						*
						<bean:write name="period" property="daycountFraction" format="#.####"/>
						*
						<bean:write name="period" property="notional" format="###,###.##"/>
					</td>
					<td class="datadate">
						<%= ISDADateFormat.formatFixedLength((Calendar)paymentDate) %>
					</td>
				</tr>
			</logic:iterate>
		</table>
	</logic:present>

	<logic:notPresent name="plainSwap" property="floatSchedule">
		Could not create float schedule.
	</logic:notPresent>
		</td>
	</tr>

</logic:present>

<logic:notPresent name="plainSwapForm" property="plainSwap">
	Could not create Plain Swap.
</logic:notPresent>
</body>
</html>
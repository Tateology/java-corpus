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
    pageEncoding="ISO-8859-1" %>
<%@ page import="org.jfin.date.util.*" %>
<%@ page import="org.jfin.date.*" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jfin.date.daycount.*" %>
<%@ page import="org.jfin.date.holiday.*" %>

<%@ taglib uri="/WEB-INF/tlds/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tlds/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tlds/struts-logic.tld" prefix="logic" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>jFin WebApp</title>

		<style type="text/css" media="screen"><!--
		h2 { font-size: 11px; font-family: verdana; font-weight: bold; padding-top: 15px; }
		h1 { color: #636363; font-size: 14px; font-family: verdana; }
		p { font-size: 11px; font-family: verdana; }
		--></style>

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
	<h1>jFin WebApp</h1>
	<h2>jFin Status</h2>
	<%

		String javaVersion = System.getProperty("java.version");

		try {
			float javaVersionNumber = Float.parseFloat(javaVersion.substring(0,3));

			if(javaVersionNumber < 1.5f) {

				%>

				<p><b><font color="#A00000">JVM FAIL: Your container is using java version &quot;<%= javaVersion %>&quot;</font></b></p>
				<p>jFin requires java version 1.5+</p>
				<%

			} else {
				%>

				<p><b><font color="#00A000">JVM OK: Your container is using java version &quot;<%= javaVersion %>&quot;</font></b></p>

				<%

				try {
					HolidayCalendarFactory holidayCalendarFactory = HolidayCalendarFactory.newInstance();
					DaycountCalculatorFactory daycountCalculatorFactory = DaycountCalculatorFactory.newInstance();

					%>
					<p>HolidayCalendarFactory: <%= holidayCalendarFactory.getClass().getCanonicalName() %></p>
					<p>DaycountCalculatorFactory: <%= daycountCalculatorFactory.getClass().getCanonicalName() %></p>
					<%
				} catch(Exception e) {
					%>
					<p><b><font color="#A00000">JFIN FAIL: Could not instantiate HolidayCalendarFactory or DaycountCalculatorFactory, check that the jFin jar is in the WEB-INF/lib directory.</font></b></p>
					<%
				}
			}
		} catch(NumberFormatException e) {

			%>

			<p><b><font color="#F00">Cannot evaluate java version &quot;<%= javaVersion %>&quot;</font></b></p>
			<pre>
				<%= e.getMessage() %>
			</pre>
			<%
		}

	%>
	<h2>Axis Status</h2>
	<p>Click <a href="happyaxis.jsp">here</a> to check that AXIS is properly installed.</p>
	<h2>Web Services</h2>
	<p>
		<a href="services/AdjustmentService.jws">Adjustment Web Service</a> |
		<a href="services/AdjustmentService.jws?wsdl">WSDL</a>
	</p>
	<p>
		<a href="services/DayCountService.jws">Day Count Web Service</a> |
		<a href="services/DayCountService.jws?wsdl">WSDL</a>
	</p>
	<p>
		<a href="services/ScheduleService.jws">Schedule Web Service</a> |
		<a href="services/ScheduleService.jws?wsdl">WSDL</a>
	</p>
	<h2>Example jsp</h2>
	<p>
		<a href="plainswap.jsp">Plain Swap</a>
	</p>
	</body>
</html>
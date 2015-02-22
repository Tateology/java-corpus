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

<script type="text/javascript" src="js/jfin_ajax.js" name="jfin_ajax"></script>

<body onload="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml');">

<html:errors/>

<html:form action="/plainswaplegs" styleId="plainSwapForm">
<table border="0">
	<tr>
		<td>
			<table>
			<tr>
				<td class="heading" colspan="2">Trade Details</td>
			</tr>
				<tr>
					<td class="label">Notional</td>
					<td><html:text property="notional" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
				</tr>
				<tr>
					<td class="label">Currency</td>
					<td>
						<html:select property="currency" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="currencies"/>
						</html:select>
					</td>
				</tr>
				<tr>
					<td class="label">Pay/Rec</td>
					<td>
						<html:select property="payRec" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="payRecOptions"/>
						</html:select>
					</td>
				</tr>
				<tr>
					<td class="label">Trade Date</td>
					<td><html:text property="tradeDate" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
				</tr>
				<tr>
					<td class="label">Effective Date</td>
					<td><html:text property="effectiveDate" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
				</tr>
				<tr>
					<td class="label">Maturity Date</td>
					<td><html:text property="maturityDate" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
				</tr>
				<tr>
					<td class="label">Stub Type</td>
					<td>
					<html:select property="stubType" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
						<html:options property="stubTypes"/>
					</html:select>
					</td>
				</tr>
				<tr>
					<td class="label">Fixing Offset</td>
					<td><html:text property="fixingOffset" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
				</tr>
				<tr>
					<td class="label">Index</td>
					<td><html:text property="floatIndex" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
				</tr>
				<!-- tr>
					<td></td>
					<td><html:submit styleClass="inputelement"/></td>
				</tr/-->
			</table>
		</td>
		<td valign="top">
			<table border="0">
				<tr>
					<td></td>
					<td class="heading">Fixed Leg</td>
					<td class="heading">Float Leg</td>
				</tr>
				<tr>
					<td class="label">Day Count</td>
					<td>
						<html:select property="fixedDaycountCalculator" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="daycountCalculators"/>
						</html:select>
					</td>
					<td>
						<html:select property="floatDaycountCalculator" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="daycountCalculators"/>
						</html:select>
					</td>
				</tr>
				<tr>
					<td class="label">Frequency</td>
					<td>
						<html:select property="fixedFrequency" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="frequencies" />
						</html:select>
					</td>
					<td>
						<html:select property="floatFrequency" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="frequencies" />
						</html:select>
					</td>
				</tr>
				<tr>
					<td class="label">Convention</td>
					<td>
						<html:select property="fixedConvention" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="businessDayConventions" />
						</html:select>
					</td>
					<td>
						<html:select property="floatConvention" styleClass="inputelement" onchange="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')">
							<html:options property="businessDayConventions" />
						</html:select>
					</td>
				</tr>
				<tr>
					<td class="label">Rate/Margin</td>
					<td><html:text property="fixedRate" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
					<td><html:text property="floatMargin" styleClass="inputelement" onkeyup="executeForm('plainswaplegs.do','plainSwapForm','legsinnerhtml')"/></td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</html:form>

<span id="legsinnerhtml"></span>

</body>
</html>
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

// Used to hold the request object
var request;
var target;

/*
 * Retrieve the html content from a given url and write it to the
 * object with id innerhtml in the document
 */
function retrieveURL(url,innerhtml) {

	document.getElementById(innerhtml).innerHTML = "<font class='label'>Starting...</font>";

	var processFunction = processStateChange;
	target = innerhtml;

	if (window.XMLHttpRequest) {
		request = new XMLHttpRequest();
		processFunction.request = request;
		request.onreadystatechange = processFunction;
		try {
			request.open("GET", url, true);
		} catch (e) {
			document.getElementById(innerhtml).innerHTML ="Could not open a request: " + e;
		}
		request.send(null);
	}
	else if (window.ActiveXObject)
	{
		request = new ActiveXObject("Microsoft.XMLHTTP");
		processFunction.request = request;
		if (request) {
			request.onreadystatechange = processFunction;
			request.open("GET", url, true);
			request.send();
		}
	}
}

/*
 * Respond to the aynchronous state changes of an
 * AJAX request
 */
function processStateChange() {
	if (request.readyState == 1)
	{
		document.getElementById(target).innerHTML = "<font class='label'>Loading...</font>";
	}
	else if (request.readyState == 2)
	{
		document.getElementById(target).innerHTML = "<font class='label'>Loading...</font>";
	}
	else if (request.readyState == 3)
	{
		document.getElementById(target).innerHTML = "<font class='label'>Receiving...</font>";
	}
	else if (request.readyState == 4)
	{
		if (request.status == 200)
		{
			document.getElementById(target).innerHTML = this.request.responseText;
		}
		else
		{
			document.getElementById(target).innerHTML ="Could not process request: " + this.request.statusText;
		}
	}
}

/*
 * Turn a given form into a GET request String of the form:
 * "?a=b&c=d&..."
 */
function collectForm(formName) {
	var form = document.getElementById(formName);
	var results = "?";
	for(var i=0;i<form.length;i++) {
		if(i>0) {
			results+="&";
		}
		var element = form[i];
		results+=element.name;
		results+="=";
		results+=escape(element.value);
	}

	return results;
}

/*
 * Submit a form with id formName to the action on a given path
 * and write it to the response to the object with id target in the document.
 *
 * For example:
 * executeForm("/myaction.do","myFormId","myResultsSpanId");
 */
function executeForm(path,formName,target) {
	var collectedForm = path+collectForm(formName);
	retrieveURL(collectedForm,target);
}
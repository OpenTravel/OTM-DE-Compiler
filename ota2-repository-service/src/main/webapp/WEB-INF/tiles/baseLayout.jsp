<%--

    Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title><tiles:insertAttribute name="title" ignore="true" /></title>
	<link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon" />
	<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" type="image/x-icon" />
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/styles.css" media="screen" />
</head>
<body>
	<table>
	<tr id="header">
		<td id="headertitle" style="vertical-align:middle">
			<tiles:insertAttribute name="headerTitle" />
		</td>
		<td id="headermenu">
			<tiles:insertAttribute name="headerMenu" />
		</td>
	</tr>
	<tr><td colspan="2" id="pagebody">
		<c:if test="${errorMessage != null}">
			<div id="errorMessage">${errorMessage}</div>
		</c:if>
		<c:if test="${statusMessage != null}">
			<div id="statusMessage">${statusMessage}</div>
		</c:if>
		<h2><tiles:insertAttribute name="pageTitle" ignore="true" /></h2>
		<tiles:insertAttribute name="body" />
	
		<p/><br/><p/><br/><p/><br/><p/><br/><p/><br/><p/><br/>
	</td></tr>
	<tr>
		<td id="footer" colspan="2">
			<tiles:insertAttribute name="footer" />
		</td>
	</tr>
	</table>
</body>
<html>
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
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:if test="${latestVersions}"><c:set var="latestVersionsCheckState" value="checked" /></c:if>
<c:if test="${finalVersions}"><c:set var="finalVersionsCheckState" value="checked" /></c:if>
<form id="searchForm" action="${pageContext.request.contextPath}/console/search.html" method="GET">
	<input name="keywords" type="text" class="searchKeywords" value="${keywords}"/>
	<input type="submit" value="Search" class="formButton" />
	<br>
	<input name="latestVersions" type="checkbox" value="true" <%= "true".equals(request.getParameter("latestVersions")) ? "checked" : "" %> /> <span class="searchOption">Latest Versions Only</span>
	&nbsp; | &nbsp;
	<input name="finalVersions" type="checkbox" value="true" <%= "true".equals(request.getParameter("finalVersions")) ? "checked" : "" %> /> <span class="searchOption">Final Versions Only</span>
</form>

<c:if test="${searchResults != null}">

<br/>
<table id="browsetable">
	<tr>
		<th>Name</th>
		<th>Version</th>
		<th>Status</th>
		<th>Last Modified</th>
	</tr>
	<c:if test="${searchResults.isEmpty()}">
		<tr class="${rowStyle}">
			<td colspan="4">No matching results found in this repository.</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="item" items="${searchResults}">
		<c:url var="itemUrl" value="/console/libraryDictionary.html">
			<c:param name="baseNamespace" value="${item.baseNamespace}" />
			<c:param name="filename" value="${item.filename}" />
			<c:param name="version" value="${item.version}" />
		</c:url>
		<c:url var="allVersionsUrl" value="/console/browse.html">
			<c:param name="baseNamespace" value="${item.baseNamespace}" />
			<c:param name="filename" value="${item.filename}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/library.png" />
				<a href="${itemUrl}">${item.label}</a>
			</td>
			<td>
				<c:if test="${item.version != null}">${item.version}</c:if>
				<c:if test="${allVersionsUrl != null}"><small>(<a href="${allVersionsUrl}">all versions</a>)</small></c:if>
			</td>
			<td><c:if test="${item.status != null}"><spring:message code="${item.status.toString()}" /></c:if></td>
			<td><c:if test="${item.lastModified != null}"><fmt:formatDate value="${item.lastModified}" type="date" pattern="dd-MMM-yyyy" /></c:if></td>
		</tr>
		<c:choose>
			<c:when test="${rowStyle=='d0'}">
				<c:set var="rowStyle" value="d1" />
			</c:when>
			<c:otherwise>
				<c:set var="rowStyle" value="d0" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
</table>

</c:if>
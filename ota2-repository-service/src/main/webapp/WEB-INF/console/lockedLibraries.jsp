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

<table id="browsetable">
	<tr>
		<th>Name</th>
		<th>Version</th>
		<th>Status</th>
	</tr>
	<c:if test="${lockedLibraries.isEmpty()}">
		<tr class="${rowStyle}">
			<td colspan="4">You do not have any locked libraries in this repository.</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="item" items="${lockedLibraries}">
		<c:url var="itemUrl" value="/console/libraryInfo.html">
			<c:param name="baseNamespace" value="${item.repositoryItem.baseNamespace}" />
			<c:param name="filename" value="${item.repositoryItem.filename}" />
			<c:param name="version" value="${item.repositoryItem.version}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/library.png" />
				<a href="${itemUrl}">${item.itemName}</a>&nbsp;<small>(${item.itemNamespace})</small>
			</td>
			<td><c:if test="${item.repositoryItem.version != null}">${item.repositoryItem.version}</c:if></td>
			<td><c:if test="${item.repositoryItem.status != null}"><spring:message code="${item.repositoryItem.status.toString()}" /></c:if></td>
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

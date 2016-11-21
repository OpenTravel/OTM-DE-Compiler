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
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:url var="namespaceUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
</c:url>
<c:url var="allVersionsUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
	<c:param name="filename" value="${item.filename}" />
</c:url>

<c:set var="currentTab" value="HISTORY"/>
<%@include file="libraryTabs.jsp" %>

<table id="itemtable">
	<tr>
		<th width="15%">User</th>
		<th width="20%">Date/Time</th>
		<th width="65%">Remarks</th>
	</tr>
	<c:if test="${history == null}">
		<tr class="d0">
			<td colspan="3">No commit history found for this library.</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="commitItem" items="${history.commitHistory}">
		<c:set var="commitUser" value="${commitUsers.get( commitItem.user )}"/>
		<tr class="${rowStyle}">
			<td>
				<c:choose>
					<c:when test="${commitUser != null}">
						<c:choose>
							<c:when test="${commitUser.emailAddress != null}">
								<a href="mailto:${commitUser.emailAddress}">${commitUser.firstName}&nbsp;${commitUser.lastName}</a>
							</c:when>
							<c:otherwise>
								${commitUser.firstName} ${commitUser.lastName}
							</c:otherwise>
						</c:choose>
					</c:when>
					<c:otherwise>
						${commitItem.user}
					</c:otherwise>
				</c:choose>
			</td>
			<td>${pageUtils.formatDateTime( commitItem.effectiveOn )}</td>
			<td>${commitItem.remarks}&nbsp;</td>
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

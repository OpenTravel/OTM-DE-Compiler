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
<c:choose>
	<c:when test="${parentItems != null}">
		<small>
			[ <a href="${pageContext.request.contextPath}/console/browse.html">Back to Top</a> ]
			<c:if test="${canCreateNamespaceExtension}">
				<c:url var="createNamespaceUrl" value="/console/createNamespace.html">
					<c:param name="baseNamespace" value="${baseNamespace}" />
				</c:url>
				&nbsp; [ <a href="${createNamespaceUrl}">Create a Namespace Extension</a> ]
			</c:if>
			<c:if test="${canDeleteNamespace}">
				<c:url var="deleteNamespaceUrl" value="/console/deleteNamespace.html">
					<c:param name="baseNamespace" value="${baseNamespace}" />
				</c:url>
				&nbsp; [ <a href="${deleteNamespaceUrl}">Delete This Namespace</a> ]
			</c:if>
			<c:if test="${canEditSubscription}">
				<c:url var="subscriptionUrl" value="/console/namespaceSubscription.html">
					<c:param name="baseNamespace" value="${baseNamespace}" />
				</c:url>
				&nbsp; [ <a href="${subscriptionUrl}">${hasSubscription ? 'Edit Subscription' : 'Subscribe'}</a> ]
			</c:if>
		</small>
		<h3>Namespace: &nbsp;
			<c:forEach var="item" items="${parentItems}">
				<c:url var="itemUrl" value="/console/browse.html">
					<c:param name="baseNamespace" value="${item.baseNamespace}" />
				</c:url>
				<a href="${itemUrl}">${item.label}</a> /
			</c:forEach>
		</h3>
		<c:choose>
			<c:when test="${libraryName != null}">
				<h3 style="margin-top:0; padding-top:0;">All Versions Of: ${libraryName}</h3>
			</c:when>
		</c:choose>
	</c:when>
	<c:otherwise>
		<small>&nbsp;</small>
		<h3>Root Namespaces</h3>
	</c:otherwise>
</c:choose>
<table id="browsetable">
	<tr>
		<th>Name</th>
		<th>Version</th>
		<th>Status</th>
		<th>Last Modified</th>
	</tr>
	<c:if test="${browseItems.isEmpty()}">
		<tr class="${rowStyle}">
			<td colspan="4">No items to display for this namespace.</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="item" items="${browseItems}">
		<c:set var="itemPage" value="libraryDictionary.html"/>
		<c:if test="${pageUtils.isRelease( item )}">
			<c:set var="itemPage" value="releaseView.html"/>
		</c:if>
		<c:choose>
			<c:when test="${filename == null}">
				<c:choose>
					<c:when test="${item.filename == null}">
						<c:url var="itemUrl" value="/console/browse.html">
							<c:param name="baseNamespace" value="${item.baseNamespace}" />
						</c:url>
						<c:set var="itemIcon" value="namespace.gif" />
						<c:set var="allVersionsUrl" value="${null}" />
					</c:when>
					<c:otherwise>
						<c:url var="itemUrl" value="/console/${itemPage}">
							<c:param name="baseNamespace" value="${item.baseNamespace}" />
							<c:param name="filename" value="${item.filename}" />
							<c:param name="version" value="${item.version}" />
						</c:url>
						<c:set var="itemIcon" value="${imageResolver.getIconImage( item )}" />
						<c:url var="allVersionsUrl" value="/console/browse.html">
							<c:param name="baseNamespace" value="${item.baseNamespace}" />
							<c:param name="filename" value="${item.filename}" />
						</c:url>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
				<c:url var="itemUrl" value="/console/${itemPage}">
					<c:param name="baseNamespace" value="${item.baseNamespace}" />
					<c:param name="filename" value="${item.filename}" />
					<c:param name="version" value="${item.version}" />
				</c:url>
				<c:set var="itemIcon" value="${imageResolver.getIconImage( item )}" />
				<c:set var="allVersionsUrl" value="${null}" />
			</c:otherwise>
		</c:choose>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/${itemIcon}" />
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

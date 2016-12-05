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
		<th width="50%">Subscription Target</th>
		<th width="50%">Notification Events</th>
	</tr>
	<c:if test="${subscriptions.isEmpty()}">
		<tr class="${rowStyle}">
			<td colspan="4">You do not have any subscriptions in this repository.</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="subscription" items="${subscriptions}">
		<c:set var="target" value="${subscription.subscriptionTarget}" />
		
		<c:choose>
			<c:when test="${(target.libraryName == null) && (target.version == null)}">
				<c:url var="subscriptionUrl" value="/console/namespaceSubscription.html">
					<c:param name="baseNamespace" value="${target.baseNamespace}" />
					<c:param name="cts" value="true" />
				</c:url>
				<c:set var="isAllEvents" value="${subscription.eventTypes.size() == 7}" />
				<c:set var="icon" value="namespace.gif" />
				<c:set var="label" value="${target.baseNamespace}" />
				<c:set var="labelSuffix" value="" />
			</c:when>
			<c:otherwise>
				<c:choose>
					<c:when test="${target.version == null}">
						<c:url var="subscriptionUrl" value="/console/librarySubscription.html">
							<c:param name="baseNamespace" value="${target.baseNamespace}" />
							<c:param name="libraryName" value="${target.libraryName}" />
							<c:param name="allVersions" value="true" />
						</c:url>
						<c:set var="isAllEvents" value="${subscription.eventTypes.size() == 6}" />
						<c:set var="icon" value="library.png" />
						<c:set var="label" value="${target.libraryName}" />
						<c:set var="labelSuffix" value="(All Versions - ${target.baseNamespace})" />
					</c:when>
					<c:otherwise>
						<c:url var="subscriptionUrl" value="/console/librarySubscription.html">
							<c:param name="baseNamespace" value="${target.baseNamespace}" />
							<c:param name="libraryName" value="${target.libraryName}" />
							<c:param name="version" value="${target.version}" />
							<c:param name="allVersions" value="false" />
						</c:url>
						<c:set var="isAllEvents" value="${subscription.eventTypes.size() == 6}" />
						<c:set var="icon" value="library.png" />
						<c:set var="label" value="${target.libraryName}" />
						<c:set var="labelSuffix" value="(Version ${target.version} - ${target.baseNamespace})" />
					</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
		<tr class="${rowStyle}">
			<td style="white-space: nowrap;">
				<img src="${pageContext.request.contextPath}/images/${icon}" />&nbsp;<a href="${subscriptionUrl}">${label}</a>&nbsp;<small>${labelSuffix}</small>
			</td>
			<td><ul class="actionList">
				<c:if test="${isAllEvents}">
					<li style="line-height:90%"><small>All Notification Events</small></li>
				</c:if>
				<c:if test="${!isAllEvents}">
					<c:forEach var="eventType" items="${subscription.eventTypes}">
						<li style="line-height:90%"><small><spring:message code="${eventType.toString()}" /></small></li>
					</c:forEach>
				</c:if>
			</ul></td>
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
	
	<c:if test="${!subscriptions.isEmpty() && isLocalUserManagement && (user.emailAddress == null)}">
		<tr class="d0">
			<td colspan="2">
				<br/>
				<div id="errorMessage">
					The system is unable send you notifications because your profile does not include an email address.
					<br/>Click <a href="${pageContext.request.contextPath}/console/editUserProfile.html">here</a> to update your profile.
				</div>
			</td>
		</tr>
	</c:if>
	
</table>

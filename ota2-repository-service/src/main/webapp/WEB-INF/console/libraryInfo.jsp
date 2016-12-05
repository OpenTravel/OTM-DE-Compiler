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

<c:set var="currentTab" value="INFO"/>
<%@include file="libraryTabs.jsp" %>

<table id="itemtable">
	<tr class="d0">
		<td width="1%">Library Name:</td>
		<td>${item.libraryName}</td>
	</tr>
	<tr class="d1">
		<td>Namespace:</td>
		<td><a href="${namespaceUrl}">${item.namespace}</a></td>
	</tr>
	<tr class="d0">
		<td>Version:</td>
		<td>${item.version} <small>(<a href="${allVersionsUrl}">all versions</a>)</small></td>
	</tr>
	<tr class="d1">
		<td>Description:</td>
		<td><c:if test="${indexItem != null}">${indexItem.itemDescription}</c:if></td>
	</tr>
	<tr class="d0">
		<td>Filename:</td>
		<td>${item.filename}</td>
	</tr>
	<tr class="d1">
		<td>Status:</td>
		<td><spring:message code="${item.status.toString()}" />
			<c:if test="${sessionScope.isAdminAuthorized}">
			<c:if test="${otm16Enabled}">
				<c:set var="nextStatus" value="${item.status.nextStatus()}"/>
				<c:set var="prevStatus" value="${item.status.previousStatus()}"/>
				
				<c:if test="${nextStatus != null}">
					<c:url var="promoteItemUrl" value="/console/adminPromoteItem.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${promoteItemUrl}">Promote to <spring:message code="${nextStatus.toString()}" /></a> ]
				</c:if>
				<c:if test="${prevStatus != null}">
					<c:url var="demoteItemUrl" value="/console/adminDemoteItem.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${demoteItemUrl}">Demote to <spring:message code="${prevStatus.toString()}" /></a> ]
				</c:if>
			</c:if>
			<c:if test="${!otm16Enabled}">
				<c:if test="${item.status.toString()=='DRAFT'}">
					<c:url var="promoteItemUrl" value="/console/adminPromoteItem.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${promoteItemUrl}">Finalize this item</a> ]
				</c:if>
				<c:if test="${item.status.toString()=='FINAL'}">
					<c:url var="demoteItemUrl" value="/console/adminDemoteItem.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${demoteItemUrl}">Demote to Draft</a> ]
				</c:if>
			</c:if>
			
				<c:if test="${item.status.toString()!='DRAFT'}">
					<c:url var="recalculateItemCrcUrl" value="/console/adminRecalculateItemCrc.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${recalculateItemCrcUrl}">Recalculate CRC</a> ]
				</c:if>
			</c:if>
		</td>
	</tr>
	<tr class="d0">
		<td>Repository&nbsp;State:</td>
		<td><spring:message code="${item.state.toString()}" />
			<c:if test="${item.state.toString()=='MANAGED_LOCKED'}">
				<c:if test="${item.lockedByUser != null}">
					<small>(Locked by
					<c:choose>
						<c:when test="${lockedByUser != null}">
							<c:choose>
								<c:when test="${lockedByUser.emailAddress != null}">
									<a href="mailto:${lockedByUser.emailAddress}">${lockedByUser.firstName} ${lockedByUser.lastName}</a>
								</c:when>
								<c:otherwise>
									${lockedByUser.firstName} ${lockedByUser.lastName}
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							${item.lockedByUser}
						</c:otherwise>
					</c:choose>
					)</small>
				</c:if>
				<c:if test="${sessionScope.isAdminAuthorized}">
					<c:url var="unlockItemUrl" value="/console/adminUnlockItem.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${unlockItemUrl}">Unlock this item</a> ]
				</c:if>
			</c:if>
		</td>
	</tr>
	
	<c:set var="showActions" value="${false}"/>
	<c:if test="${sessionScope.isAdminAuthorized}">
		<c:url var="deleteItemUrl" value="/console/adminDeleteItem.html">
			<c:param name="baseNamespace" value="${item.baseNamespace}" />
			<c:param name="filename" value="${item.filename}" />
			<c:param name="version" value="${item.version}" />
		</c:url>
		<c:set var="showActions" value="${true}"/>
	</c:if>
	<c:if test="${canEditSubscription}">
		<c:url var="subscribeAllVersionsUrl" value="/console/librarySubscription.html">
			<c:param name="baseNamespace" value="${item.baseNamespace}" />
			<c:param name="libraryName" value="${item.libraryName}" />
			<c:param name="filename" value="${item.filename}" />
			<c:param name="version" value="${item.version}" />
			<c:param name="allVersions" value="true" />
		</c:url>
		<c:url var="subscribeSingleVersionUrl" value="/console/librarySubscription.html">
			<c:param name="baseNamespace" value="${item.baseNamespace}" />
			<c:param name="libraryName" value="${item.libraryName}" />
			<c:param name="filename" value="${item.filename}" />
			<c:param name="version" value="${item.version}" />
			<c:param name="allVersions" value="false" />
		</c:url>
		<c:choose>
			<c:when test="${hasAllVersionsSubscription}">
				<c:set var="subscribeAllVersionsLabel" value="Edit Subscriptions"/>
			</c:when>
			<c:otherwise>
				<c:set var="subscribeAllVersionsLabel" value="Subscribe"/>
			</c:otherwise>
		</c:choose>
		<c:choose>
			<c:when test="${hasSingleVersionSubscription}">
				<c:set var="subscribeSingleVersionLabel" value="Edit Subscriptions"/>
			</c:when>
			<c:otherwise>
				<c:set var="subscribeSingleVersionLabel" value="Subscribe"/>
			</c:otherwise>
		</c:choose>
		<c:set var="showActions" value="${true}"/>
	</c:if>
	
	<c:if test="${showActions}">
	<tr class="d1">
		<td>Action(s):</td>
		<td><ul class="actionList">
			<c:if test="${deleteItemUrl != null}">
				<li>[ <a href="${deleteItemUrl}">Delete this Item</a> ]</li>
			</c:if>
			<c:if test="${subscribeAllVersionsUrl != null}">
				<li>[ <a href="${subscribeAllVersionsUrl}">${subscribeAllVersionsLabel}</a> ] <small>(all versions)</small></li>
			</c:if>
			<c:if test="${subscribeSingleVersionUrl != null}">
				<li>[ <a href="${subscribeSingleVersionUrl}">${subscribeSingleVersionLabel}</a> ] <small>(version ${item.version} only)</small></li>
			</c:if>
		</ul></td>
	</tr>
	</c:if>
</table>
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

<c:set var="currentTab" value="VIEW"/>
<%@include file="assemblyTabs.jsp" %>

<c:if test="${assembly.itemContent.description != null}">
<div class="contentContainer" style="width:100%">
	<ul class="blockList">
	<li class="blockList">
	<ul class="blockList">
	<li class="blockList">
	
	<h3>Description</h3>
	<div class="description">
		<pre>${assembly.itemContent.description}</pre>
	</div>
	</li></ul>
	</li></ul>
</div>
</c:if>

<div class="contentContainer" style="width:100%">
<table id="itemtable" style="width:100%;float:left;">
	<tr>
		<th width="65%">Provider APIs</th>
		<th width="35%">Status</th>
	</tr>
	<c:if test="${providerReleases.isEmpty() && externalProviders.isEmpty()}">
		<tr class="d0">
			<td colspan="2">No provider-side APIs defined for this assembly</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="release" items="${providerReleases}">
		<c:url var="releaseUrl" value="/console/releaseView.html">
			<c:param name="baseNamespace" value="${release.baseNamespace}" />
			<c:param name="filename" value="${release.filename}" />
			<c:param name="version" value="${release.version}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/release.gif" />&nbsp;<a href="${releaseUrl}">${release.releaseName}</a>
				<br><small>${release.namespace}</small>
			</td>
			<td>
				<spring:message code="${release.itemContent.status}" />
			</td>
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
	<c:forEach var="externalRelease" items="${externalProviders}">
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/release.gif" />&nbsp;${externalRelease.libraryName}
				<br><small>${externalRelease.namespace} [${externalRelease.repositoryID}]</small>
			</td>
			<td>&nbsp;</td>
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
</div>

<div class="contentContainer" style="width:100%"><br/>
<table id="itemtable" style="width:100%;float:left;">
	<tr>
		<th width="65%">Consumer APIs</th>
		<th width="35%">Status</th>
	</tr>
	<c:if test="${consumerReleases.isEmpty() && externalConsumers.isEmpty()}">
		<tr class="d0">
			<td colspan="2">No consumer-side APIs defined for this assembly</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="release" items="${consumerReleases}">
		<c:url var="releaseUrl" value="/console/releaseView.html">
			<c:param name="baseNamespace" value="${release.baseNamespace}" />
			<c:param name="filename" value="${release.filename}" />
			<c:param name="version" value="${release.version}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/release.gif" />&nbsp;<a href="${releaseUrl}">${release.releaseName}</a>
				<br><small>${release.namespace}</small>
			</td>
			<td>
				<spring:message code="${release.itemContent.status}" />
			</td>
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
	<c:forEach var="externalRelease" items="${externalConsumers}">
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/release.gif" />&nbsp;${externalRelease.libraryName}
				<br><small>${externalRelease.namespace} [${externalRelease.repositoryID}]</small>
			</td>
			<td>&nbsp;</td>
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
</div>

<c:set var="itemTypeTag" value="assembly" />
<%@include file="clipboardDependencyJS.jsp" %>
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
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="namespaceUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
</c:url>
<c:url var="allVersionsUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
	<c:param name="filename" value="${item.filename}" />
</c:url>
<c:url var="viewUrl" value="/console/releaseView.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
	<c:param name="filename" value="${item.filename}" />
	<c:param name="version" value="${item.version}" />
</c:url>
<c:url var="assembliesUrl" value="/console/releaseAssemblies.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
	<c:param name="filename" value="${item.filename}" />
	<c:param name="version" value="${item.version}" />
</c:url>

<table style="border-collapse:collapse;width:100%;float:left;">
	<tr>
		<td>
			<h2 style="padding-bottom: 0;">Release: ${item.libraryName} <small>(${item.version})</small></h2>
			<h3 style="padding-top: 0;">Namespace: <a href="${namespaceUrl}">${item.namespace}</a></h3>
		</td>
		<td style="white-space:nowrap;width:10%;text-align:left;">
			<c:if test="${sessionScope.isAdminAuthorized}">
				<c:url var="deleteItemUrl" value="/console/adminDeleteItem.html">
					<c:param name="baseNamespace" value="${item.baseNamespace}" />
					<c:param name="filename" value="${item.filename}" />
					<c:param name="version" value="${item.version}" />
				</c:url>
				[ <a href="${deleteItemUrl}">Delete this Release</a> ]<br/>
			</c:if>
			Maven Plugin Dependency:
			<img src="${pageContext.request.contextPath}/images/clipboard.png"
					onclick="copyDependencyToClipboard();" style="cursor:pointer;" />
		</td>
	</tr>
	<tr>
		<td colspan="2" style="padding-top:10px;">
			Release Type: <spring:message code="${release.itemContent.status}" />
			<br/><br/>
		</td>
	</tr>
</table>

<ul class="tab">
	<c:set var="tabStyle" value="tablinks" />
	<c:if test="${currentTab == 'VIEW'}"><c:set var="tabStyle" value="tablinks activeTab" /></c:if>
	<li><a href="${viewUrl}" class="${tabStyle}">Member Libraries</a></li>
	
	<c:set var="tabStyle" value="tablinks" />
	<c:if test="${currentTab == 'ASSEMBLIES'}"><c:set var="tabStyle" value="tablinks activeTab" /></c:if>
	<li><a href="${assembliesUrl}" class="${tabStyle}">Assemblies</a></li>
</ul>
<p/><br/>


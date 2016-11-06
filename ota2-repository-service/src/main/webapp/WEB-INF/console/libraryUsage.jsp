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

<c:set var="currentTab" value="USAGE"/>
<%@include file="libraryTabs.jsp" %>

<table id="itemtable">
	<tr>
		<th>Direct References <small>(Libraries that are directly referenced by ${item.libraryName})</small></th>
	</tr>
	<c:if test="${usesLibraries.isEmpty()}">
		<tr class="d0">
			<td>No direct references for this library.</td>
		</tr>
	</c:if>
	<c:forEach var="library" items="${usesLibraries}">
		<tr class="d0">
			<td>
				<c:url var="usageUrl" value="/console/libraryUsage.html">
					<c:param name="baseNamespace" value="${library.repositoryItem.baseNamespace}" />
					<c:param name="filename" value="${library.repositoryItem.filename}" />
					<c:param name="version" value="${library.repositoryItem.version}" />
				</c:url>
				<img src="${pageContext.request.contextPath}/images/library.png" />&nbsp;<a href="${usageUrl}">${library.itemName}</a>
				<small>(${library.itemNamespace})</small>
			</td>
		</tr>
	</c:forEach>
</table>

<br/>
<table id="itemtable">
	<tr>
		<th>Direct Where-Used <small>(Libraries that directly reference ${item.libraryName})</small></th>
	</tr>
	<c:if test="${directWhereUsed.isEmpty()}">
		<tr class="d0">
			<td>No direct where-used references for this library.</td>
		</tr>
	</c:if>
	<c:forEach var="library" items="${directWhereUsed}">
		<tr class="d0">
			<td>
				<c:url var="usageUrl" value="/console/libraryUsage.html">
					<c:param name="baseNamespace" value="${library.repositoryItem.baseNamespace}" />
					<c:param name="filename" value="${library.repositoryItem.filename}" />
					<c:param name="version" value="${library.repositoryItem.version}" />
				</c:url>
				<img src="${pageContext.request.contextPath}/images/library.png" />&nbsp;<a href="${usageUrl}">${library.itemName}</a>
				<small>(${library.itemNamespace})</small>
			</td>
		</tr>
	</c:forEach>
</table>

<br/>
<table id="itemtable">
	<tr>
		<th>Indirect Where-Used <small>(Libraries that indirectly reference ${item.libraryName})</small></th>
	</tr>
	<c:if test="${indirectWhereUsed.isEmpty()}">
		<tr class="d0">
			<td>No indirect where-used references for this library.</td>
		</tr>
	</c:if>
	<c:forEach var="library" items="${indirectWhereUsed}">
		<tr class="d0">
			<td>
				<c:url var="usageUrl" value="/console/libraryUsage.html">
					<c:param name="baseNamespace" value="${library.repositoryItem.baseNamespace}" />
					<c:param name="filename" value="${library.repositoryItem.filename}" />
					<c:param name="version" value="${library.repositoryItem.version}" />
				</c:url>
				<img src="${pageContext.request.contextPath}/images/library.png" />&nbsp;<a href="${usageUrl}">${library.itemName}</a>
				<small>(${library.itemNamespace})</small>
			</td>
		</tr>
	</c:forEach>
</table>

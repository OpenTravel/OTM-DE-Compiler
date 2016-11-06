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

<c:set var="currentTab" value="USAGE"/>
<%@include file="entityTabs.jsp" %>

<table id="itemtable">
	<tr>
		<th colspan="2">Direct Where-Used <small>(Entities that directly reference ${entity.itemName})</small></th>
	</tr>
	<c:if test="${directWhereUsed.isEmpty()}">
		<tr class="d0">
			<td colspan="2">No direct where-used references for this library.</td>
		</tr>
	</c:if>
	<c:forEach var="entity" items="${directWhereUsed}">
		<tr class="d0">
			<td>
				<c:url var="usageUrl" value="/console/entityUsage.html">
					<c:param name="namespace" value="${entity.itemNamespace}" />
					<c:param name="localName" value="${entity.itemName}" />
				</c:url>
				<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( entity )}" />&nbsp;<a href="${usageUrl}">${entity.itemName}</a>
				<small>(${entity.itemNamespace})</small>
			</td>
			<td><spring:message code="${entity.entityType.simpleName}" /></td>
		</tr>
	</c:forEach>
</table>

<br/>
<table id="itemtable">
	<tr>
		<th colspan="2">Indirect Where-Used <small>(Entities that indirectly reference ${entity.itemName})</small></th>
	</tr>
	<c:if test="${indirectWhereUsed.isEmpty()}">
		<tr class="d0">
			<td colspan="2">No indirect where-used references for this library.</td>
		</tr>
	</c:if>
	<c:forEach var="entity" items="${indirectWhereUsed}">
		<tr class="d0">
			<td>
				<c:url var="usageUrl" value="/console/entityUsage.html">
					<c:param name="namespace" value="${entity.itemNamespace}" />
					<c:param name="localName" value="${entity.itemName}" />
				</c:url>
				<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( entity )}" />&nbsp;<a href="${usageUrl}">${entity.itemName}</a>
				<small>(${entity.itemNamespace})</small>
			</td>
			<td><spring:message code="${entity.entityType.simpleName}" /></td>
		</tr>
	</c:forEach>
</table>

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

<c:set var="facetMembers" value="${entity.itemContent.memberFields}" />

<table id="itemtable">
	<tr>
		<th width="20%">Property</th>
		<th width="80%">Value</th>
	</tr>
	<tr class="d0">
		<td>Facet Owner</td>
		<td>
			<c:set var="refEntity" value="${entitiesByReference.get( entity.extendsEntityId )}" />
			<c:choose>
				<c:when test="${refEntity != null}">
					<c:url var="entityUrl" value="/console/entityDictionary.html">
						<c:param name="namespace" value="${refEntity.itemNamespace}" />
						<c:param name="localName" value="${refEntity.itemLocalName}" />
					</c:url>
					<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( refEntity )}" />&nbsp;<a href="${entityUrl}">${entity.itemContent.owningEntityName}</a>
				</c:when>
				<c:otherwise>
					<c:if test="${entity.itemContent.owningEntityName != null}">
						<img src="${pageContext.request.contextPath}/images/simple.gif" />&nbsp;${entity.itemContent.owningEntityName}
					</c:if>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
	<tr class="d1">
		<td>Facet Name</td>
		<td>${entity.itemName}</td>
	</tr>
	<c:set var="rowStyle" value="d0" />
	<%@include file="entityDetails_facetList.jsp" %>
</table>

<br/><p/>
<h4>Declared Fields</h4>
<%@include file="entityDetails_facetMembers.jsp" %>

<%@include file="entityDetails_facetTables.jsp" %>

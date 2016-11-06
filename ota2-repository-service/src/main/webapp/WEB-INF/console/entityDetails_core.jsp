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

<c:set var="extensionRef" value="${pageUtils.getExtensionRef( entity )}" />
<c:set var="aliasList" value="${entity.itemContent.aliases}" />
<c:set var="simpleFacetRef" value="${pageUtils.getSimpleFacetRef( entity )}" />

<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Property</th>
		<th width="80%">Value</th>
	</tr>
	<%@include file="entityDetails_extensionRef.jsp" %>
	<%@include file="entityDetails_aliasList.jsp" %>
	
	<c:if test="${simpleFacetRef != null}">
		<tr class="${rowStyle}">
			<td>Simple Facet:</td>
			<td>
				<c:set var="refEntity" value="${entitiesByReference.get( simpleFacetRef )}" />
				<c:choose>
					<c:when test="${refEntity != null}">
						<c:url var="entityUrl" value="/console/entityDictionary.html">
							<c:param name="namespace" value="${refEntity.itemNamespace}" />
							<c:param name="localName" value="${refEntity.itemName}" />
						</c:url>
						<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( refEntity )}" />&nbsp;<a href="${entityUrl}">${simpleFacetRef}</a>
					</c:when>
					<c:otherwise>
						<c:if test="${simpleFacetRef != null}">
							<img src="${pageContext.request.contextPath}/images/simple.gif" />&nbsp;${simpleFacetRef}
						</c:if>&nbsp;
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	</c:if>
	
	<%@include file="entityDetails_facetList.jsp" %>
</table>

<%@include file="entityDetails_facetTables.jsp" %>

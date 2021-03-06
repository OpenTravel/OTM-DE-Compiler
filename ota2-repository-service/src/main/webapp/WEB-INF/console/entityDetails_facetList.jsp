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
	
<tr class="${rowStyle}">
	<td>Facets</td>
	<td>
	<c:set var="firstRow" value="true" />
	<c:forEach var="facetItem" items="${entityFacets}">
		<c:if test="${!firstRow}"><br/></c:if>
		<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( facetItem.facet )}" />&nbsp;<a href="#facet_${facetItem.identity}">${facetItem.identity}</a>
		<c:if test="${facetItem.contributedFrom != null}">
			<small>(contributed from ${facetItem.contributedFrom})</small>
		</c:if>
		<c:set var="firstRow" value="false" />
	</c:forEach>
	</td>
</tr>
<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />

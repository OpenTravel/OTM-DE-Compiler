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

<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Field Name</th>
		<th width="20%">Member Type</th>
		<th width="20%">Field Type</th>
		<th width="60%">Description</th>
	</tr>
	<c:if test="${facetMembers.isEmpty()}">
		<tr>
			<td colspan="4">No member fields defined for this facet.</td>
		</tr>
	</c:if>
	<c:forEach var="field" items="${facetMembers}">
		<tr class="${rowStyle}">
			<td>${field.name}</td>
			<td><spring:message code="${field.getClass().simpleName}" /></td>
			<td>
				<c:if test="${field.getClass().simpleName != 'TLIndicator'}">
					<c:set var="refEntity" value="${entitiesByReference.get( field.typeName )}" />
					<c:choose>
						<c:when test="${refEntity != null}">
							<c:url var="entityUrl" value="/console/entityDictionary.html">
								<c:param name="namespace" value="${refEntity.itemNamespace}" />
								<c:param name="localName" value="${refEntity.itemName}" />
							</c:url>
							<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( refEntity )}" />&nbsp;<a href="${entityUrl}">${field.typeName}</a>
						</c:when>
						<c:otherwise>
							<c:if test="${field.typeName != null}">
								<img src="${pageContext.request.contextPath}/images/simple.gif" />&nbsp;${field.typeName}
							</c:if>
						</c:otherwise>
					</c:choose>
				</c:if>
				&nbsp;
			</td>
			<td>${docHelper.getDescription( field )}</td>
		</tr>
		<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	</c:forEach>
</table>

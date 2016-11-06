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

<c:set var="attributes" value="${entity.itemContent.attributes}" />

<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Property</th>
		<th width="80%">Value</th>
	</tr>
	<tr class="${rowStyle}">
		<td>Parent Type:</td>
		<td>
			<c:set var="refEntity" value="${entitiesByReference.get( entity.itemContent.parentTypeName )}" />
			<c:choose>
				<c:when test="${refEntity != null}">
					<c:url var="entityUrl" value="/console/entityDictionary.html">
						<c:param name="namespace" value="${refEntity.itemNamespace}" />
						<c:param name="localName" value="${refEntity.itemName}" />
					</c:url>
					<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( refEntity )}" />&nbsp;<a href="${entityUrl}">${entity.itemContent.parentTypeName}</a>
				</c:when>
				<c:otherwise>
					<c:if test="${entity.itemContent.parentTypeName != null}">
						<img src="${pageContext.request.contextPath}/images/simple.gif" />&nbsp;${entity.itemContent.parentTypeName}
					</c:if>&nbsp;
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
</table>

<c:if test="${!attributes.isEmpty()}">
<br/>
<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Attribute Name</th>
		<th width="20%">Type</th>
		<th width="80%">Description</th>
	</tr>
	<c:forEach var="attribute" items="${attributes}">
		<tr class="${rowStyle}">
			<td>${attribute.name}</td>
			<td>
			<c:set var="refEntity" value="${entitiesByReference.get( attribute.typeName )}" />
			<c:choose>
				<c:when test="${refEntity != null}">
					<c:url var="entityUrl" value="/console/entityDictionary.html">
						<c:param name="namespace" value="${refEntity.itemNamespace}" />
						<c:param name="localName" value="${refEntity.itemName}" />
					</c:url>
					<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( refEntity )}" />&nbsp;<a href="${entityUrl}">${attribute.typeName}</a>
				</c:when>
				<c:otherwise>
					<c:if test="${attribute.typeName != null}">
						<img src="${pageContext.request.contextPath}/images/simple.gif" />&nbsp;${attribute.typeName}
					</c:if>&nbsp;
				</c:otherwise>
			</c:choose>
			</td>
			<td>${docHelper.getDescription( attribute )}</td>
		</tr>
		<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	</c:forEach>
</table>
</c:if>

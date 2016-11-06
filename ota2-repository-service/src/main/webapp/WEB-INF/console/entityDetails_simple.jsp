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
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>List Type:</td>
		<td>${entity.itemContent.listTypeInd}</td>
	</tr>
	<c:if test="${!pageUtils.isBlank( entity.itemContent.pattern )}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Pattern:</td>
		<td>${entity.itemContent.pattern}</td>
	</tr>
	</c:if>
	<c:if test="${entity.itemContent.minLength >= 0}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Min-Length:</td>
		<td>${entity.itemContent.minLength}</td>
	</tr>
	</c:if>
	<c:if test="${entity.itemContent.maxLength >= 0}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Max-Length:</td>
		<td>${entity.itemContent.maxLength}</td>
	</tr>
	</c:if>
	<c:if test="${entity.itemContent.fractionDigits >= 0}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Fraction Digits:</td>
		<td>${entity.itemContent.fractionDigits}</td>
	</tr>
	</c:if>
	<c:if test="${entity.itemContent.totalDigits >= 0}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Total Digits:</td>
		<td>${entity.itemContent.totalDigits}</td>
	</tr>
	</c:if>
	<c:if test="${!pageUtils.isBlank( entity.itemContent.minInclusive )}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Min-Inclusive:</td>
		<td>${entity.itemContent.minInclusive}</td>
	</tr>
	</c:if>
	<c:if test="${!pageUtils.isBlank( entity.itemContent.maxInclusive )}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Max-Inclusive:</td>
		<td>${entity.itemContent.maxInclusive}</td>
	</tr>
	</c:if>
	<c:if test="${!pageUtils.isBlank( entity.itemContent.minExclusive )}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Min-Exclusive:</td>
		<td>${entity.itemContent.minExclusive}</td>
	</tr>
	</c:if>
	<c:if test="${!pageUtils.isBlank( entity.itemContent.maxExclusive )}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Max-Exclusive:</td>
		<td>${entity.itemContent.maxExclusive}</td>
	</tr>
	</c:if>
</table>

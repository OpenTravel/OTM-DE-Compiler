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

<c:set var="resource" value="${entity.itemContent}"/>
<c:set var="extensionRef" value="${pageUtils.getExtensionRef( entity )}" />

<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Property</th>
		<th width="80%">Value</th>
	</tr>
	<tr class="${rowStyle}">
		<td>Name</td>
		<td>${resource.name}</td>
	</tr>
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Base Path</td>
		<td>${resource.basePath}</td>
	</tr>
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<%@include file="entityDetails_extensionRef.jsp" %>
	<tr class="${rowStyle}">
		<td>Business Object</td>
		<td>
			<c:set var="refEntity" value="${entitiesByReference.get( resource.businessObjectRefName )}" />
			<c:choose>
				<c:when test="${refEntity != null}">
					<c:url var="entityUrl" value="/console/entityDictionary.html">
						<c:param name="namespace" value="${refEntity.itemNamespace}" />
						<c:param name="localName" value="${refEntity.itemName}" />
					</c:url>
					<img src="${pageContext.request.contextPath}/images/business_object.png" />&nbsp;<a href="${entityUrl}">${resource.businessObjectRefName}</a>
				</c:when>
				<c:otherwise>
					<c:if test="${resource.businessObjectRefName != null}">
						<img src="${pageContext.request.contextPath}/images/business_object.png" />&nbsp;${resource.businessObjectRefName}
					</c:if>&nbsp;
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>First Class</td>
		<td>
		<c:choose>
			<c:when test="${resource.isFirstClass()}">Yes</c:when>
			<c:otherwise>No</c:otherwise>
		</c:choose>
		</td>
	</tr>
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Abstract</td>
		<td>
		<c:choose>
			<c:when test="${resource.isAbstract()}">Yes</c:when>
			<c:otherwise>No</c:otherwise>
		</c:choose>
		</td>
	</tr>
</table>

<c:if test="${!resource.parentRefs.isEmpty()}">
<br/><h4>Parent Resources</h4>
<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Parent</th>
		<th width="15%">Parameter Group</th>
		<th width="25%">Path Template</th>
		<th width="40%">Description</th>
	</tr>
	<c:forEach var="parentRef" items="${resource.parentRefs}">
		<tr class="${rowStyle}">
			<td>${parentRef.parentResourceName}</td>
			<td><a href="#PG_${parentRef.parentParamGroupName}">${parentRef.parentParamGroupName}</a></td>
			<td><pre>${parentRef.pathTemplate}</pre></td>
			<td>${parentRef.documentation.description}</td>
		</tr>
		<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	</c:forEach>
</table>
</c:if>

<c:if test="${!resource.paramGroups.isEmpty()}">
<br/><h4>Parameter Groups</h4>
<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="15%">Name</th>
		<th width="10%">ID Group</th>
		<th width="15%">Facet Refernece</th>
		<th width="60%">Parameters</th>
	</tr>
	<c:forEach var="paramGroup" items="${resource.paramGroups}">
		<tr class="${rowStyle}">
			<td><a id="PG_${paramGroup.name}">${paramGroup.name}</a></td>
			<td>
			<c:choose>
				<c:when test="${paramGroup.idGroup}">Yes</c:when>
				<c:otherwise>No</c:otherwise>
			</c:choose>
			</td>
			<td>${paramGroup.facetRefName}</td>
			<td>
			<c:choose>
				<c:when test="${!paramGroup.parameters.isEmpty()}">
				<ul>
				<c:forEach var="parameter" items="${paramGroup.getParameters()}">
					<li>${parameter.fieldRefName} (${parameter.location})</li>
				</c:forEach>
				</ul>
				</c:when>
				<c:otherwise>&nbsp;</c:otherwise>
			</c:choose>
			</td>
		</tr>
		<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	</c:forEach>
</table>
</c:if>

<c:if test="${!resource.actionFacets.isEmpty()}">
<br/><h4>Action Facets</h4>
<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Name</th>
		<th width="20%">Base Payload</th>
		<th width="20%">Reference Facet</th>
		<th width="20%">Reference Type</th>
		<th width="20%">Reference Repeat</th>
	</tr>
	<c:forEach var="facet" items="${resource.actionFacets}">
		<tr class="${rowStyle}">
			<td><a id="AF_${facet.localName}">${facet.name}</a></td>
			<td>
				<c:set var="refEntity" value="${entitiesByReference.get( facet.basePayloadName )}" />
				<c:choose>
					<c:when test="${refEntity != null}">
						<c:url var="entityUrl" value="/console/entityDictionary.html">
							<c:param name="namespace" value="${refEntity.itemNamespace}" />
							<c:param name="localName" value="${refEntity.itemName}" />
						</c:url>
						<img src="${pageContext.request.contextPath}/images/${imageResolver.getIconImage( refEntity )}" />&nbsp;<a href="${entityUrl}">${facet.basePayloadName}</a>
					</c:when>
					<c:otherwise>
						<c:if test="${facet.basePayloadName != null}">
							<img src="${pageContext.request.contextPath}/images/core_object.gif" />&nbsp;${facet.basePayloadName}
						</c:if>&nbsp;
					</c:otherwise>
				</c:choose>
			</td>
			<td>${facet.referenceFacetName}</td>
			<td>${facet.referenceType}</td>
			<td>${facet.referenceRepeat}</td>
		</tr>
		<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	</c:forEach>
</table>
</c:if>

<c:forEach var="action" items="${resource.actions}">
<c:set var="request" value="${action.request}" />
<br/><h4>Action: ${action.actionId}</h4>
<table id="itemtable">
	<c:set var="rowStyle" value="d0" />
	<tr>
		<th width="20%">Property</th>
		<th width="80%">Value</th>
	</tr>
	<tr class="${rowStyle}">
		<td>Path Template</td>
		<td><pre>${request.pathTemplate}</pre></td>
	</tr>
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Parameter Group</td>
		<td><c:choose>
				<c:when test="${request.paramGroupName != null}">
				<a href="#PG_${request.paramGroupName}">${request.paramGroupName}</a>
				</c:when>
				<c:otherwise>None</c:otherwise>
		</c:choose></td>
	</tr>
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Request Payload</td>
		<td><c:choose>
				<c:when test="${request.payloadTypeName != null}">
				<a href="#AF_${request.payloadTypeName}">${request.payloadTypeName}</a>
				</c:when>
				<c:otherwise>None</c:otherwise>
		</c:choose></td>
	</tr>
	<c:forEach var="response" items="${action.responses}">
	<c:set var="rowStyle" value="${pageUtils.swapValue( rowStyle, 'd0', 'd1')}" />
	<tr class="${rowStyle}">
		<td>Response (${pageUtils.getDisplayStatusCodes( response )})</td>
		<td><c:choose>
				<c:when test="${response.payloadTypeName != null}">
				<a href="#AF_${response.payloadTypeName}">${response.payloadTypeName}</a>
				</c:when>
				<c:otherwise>None</c:otherwise>
		</c:choose></td>
	</tr>
	</c:forEach>
</table>
</c:forEach>

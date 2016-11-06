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

<c:if test="${docHelper.hasDocumentation()}">
<div class="contentContainer" style="width:100%">
	<ul class="blockList">
	<li class="blockList">
	<ul class="blockList">
	<li class="blockList">
	
	<c:if test="${docHelper.hasDescription()}">
	<h3>Description</h3>
	<div class="description">
		<pre>${docHelper.description}</pre>
	</div>
	</c:if>
	
	<c:if test="${docHelper.hasAdditionalDocs()}">
	<table class="overviewSummary">
		<caption><span>Additional Documentation</span></caption>
		<tr>
			<th class="colFirst" scope="col">Type</th>
			<th class="colLast" scope="col">Value</th>
		</tr>
		<c:if test="${!docHelper.implementerDocs.isEmpty()}">
		<tr class="altColor">
			<td class="colFirst"><strong><code>Implementers</code></strong></td>
			<td class="colLast">
				<c:set var="firstValue" value="true" />
				<c:forEach var="docValue" items="${docHelper.implementerDocs}">
					<c:if test="${!firstValue}"><br/></c:if>
					<c:choose>
						<c:when test="${docHelper.isUrl( docValue )}"><a href="${docValue}">${docValue}</a></c:when>
						<c:otherwise><code>${docValue}</code></c:otherwise>
					</c:choose>
					<c:set var="firstValue" value="false" />
				</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${!docHelper.deprecationDocs.isEmpty()}">
		<tr class="rowColor">
			<td class="colFirst"><strong><code>Deprecations</code></strong></td>
			<td class="colLast">
				<c:set var="firstValue" value="true" />
				<c:forEach var="docValue" items="${docHelper.deprecationDocs}">
					<c:if test="${!firstValue}"><br/></c:if>
					<c:choose>
						<c:when test="${docHelper.isUrl( docValue )}"><a href="${docValue}">${docValue}</a></c:when>
						<c:otherwise><code>${docValue}</code></c:otherwise>
					</c:choose>
					<c:set var="firstValue" value="false" />
				</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${!docHelper.referenceDocs.isEmpty()}">
		<tr class="altColor">
			<td class="colFirst"><strong><code>Reference Links</code></strong></td>
			<td class="colLast">
				<c:set var="firstValue" value="true" />
				<c:forEach var="docValue" items="${docHelper.referenceDocs}">
					<c:if test="${!firstValue}"><br/></c:if>
					<c:choose>
						<c:when test="${docHelper.isUrl( docValue )}"><a href="${docValue}">${docValue}</a></c:when>
						<c:otherwise><code>${docValue}</code></c:otherwise>
					</c:choose>
					<c:set var="firstValue" value="false" />
				</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${!docHelper.moreInfoDocs.isEmpty()}">
		<tr class="rowColor">
			<td class="colFirst"><strong><code>More Info</code></strong></td>
			<td class="colLast">
				<c:set var="firstValue" value="true" />
				<c:forEach var="docValue" items="${docHelper.moreInfoDocs}">
					<c:if test="${!firstValue}"><br/></c:if>
					<c:choose>
						<c:when test="${docHelper.isUrl( docValue )}"><a href="${docValue}">${docValue}</a></c:when>
						<c:otherwise><code>${docValue}</code></c:otherwise>
					</c:choose>
					<c:set var="firstValue" value="false" />
				</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${!docHelper.otherDocs.isEmpty()}">
		<tr class="altColor">
			<td class="colFirst"><strong><code>Other Doc</code></strong></td>
			<td class="colLast">
				<c:set var="firstValue" value="true" />
				<c:forEach var="docValue" items="${docHelper.otherDocs}">
					<c:if test="${!firstValue}"><br/></c:if>
					<c:choose>
						<c:when test="${docHelper.isUrl( docValue )}"><a href="${docValue}">${docValue}</a></c:when>
						<c:otherwise><code>${docValue}</code></c:otherwise>
					</c:choose>
					<c:set var="firstValue" value="false" />
				</c:forEach>
			</td>
		</tr>
		</c:if>
	</table>
	</c:if>
	</li></ul>
	</li></ul>
</div>
</c:if>

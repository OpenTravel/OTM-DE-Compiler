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

<c:set var="currentTab" value="VIEW"/>
<%@include file="releaseTabs.jsp" %>

<c:if test="${release.itemContent.description != null}">
<div class="contentContainer" style="width:100%">
	<ul class="blockList">
	<li class="blockList">
	<ul class="blockList">
	<li class="blockList">
	
	<h3>Description</h3>
	<div class="description">
		<pre>${release.itemContent.description}</pre>
	</div>
	</li></ul>
	</li></ul>
</div>
</c:if>

<table id="itemtable" style="width:100%;float:left;">
	<tr>
		<th width="40%">Principal Libraries</th>
		<th width="30%">Status</th>
		<th width="30%">Effective Date</th>
	</tr>
	<c:if test="${principalLibraries.isEmpty() && externalPrincipals.isEmpty()}">
		<tr class="d0">
			<td colspan="2">No principal libraries defined for this release</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="library" items="${principalLibraries}">
		<c:url var="libraryUrl" value="/console/libraryDictionary.html">
			<c:param name="baseNamespace" value="${library.library.repositoryItem.baseNamespace}" />
			<c:param name="filename" value="${library.library.repositoryItem.filename}" />
			<c:param name="version" value="${library.library.repositoryItem.version}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/library.png" />&nbsp;<a href="${libraryUrl}">${library.library.itemName}</a>
				<br><small>${library.library.itemNamespace}</small>
			</td>
			<td>
				<spring:message code="${library.library.status}" />
			</td>
			<td>
				<c:choose>
					<c:when test="${library.effectiveDate != null}">
						${pageUtils.formatDateTime( library.effectiveDate )}
					</c:when>
					<c:otherwise>
						Latest Commit
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<c:choose>
			<c:when test="${rowStyle=='d0'}">
				<c:set var="rowStyle" value="d1" />
			</c:when>
			<c:otherwise>
				<c:set var="rowStyle" value="d0" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
	<c:forEach var="externalLib" items="${externalPrincipals}">
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/library.png" />&nbsp;${externalLib.libraryName}
				<br><small>${externalLib.namespace} [${externalLib.repositoryID}]</small>
			</td>
			<td>&nbsp;</td>
			<td>
				<c:choose>
					<c:when test="${externalLib.effectiveDate != null}">
						${pageUtils.formatXmlDateTime( externalLib.effectiveDate )}
					</c:when>
					<c:otherwise>
						Latest Commit
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<c:choose>
			<c:when test="${rowStyle=='d0'}">
				<c:set var="rowStyle" value="d1" />
			</c:when>
			<c:otherwise>
				<c:set var="rowStyle" value="d0" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
</table>


<table id="itemtable" style="width:100%; float:left; margin-top:25px;">
	<tr>
		<th width="40%">Referenced Libraries</th>
		<th width="30%">Status</th>
		<th width="30%">Effective Date</th>
	</tr>
	<c:if test="${referencedLibraries.isEmpty() && externalReferences.isEmpty()}">
		<tr class="d0">
			<td colspan="2">No referenced libraries defined for this release</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="library" items="${referencedLibraries}">
		<c:url var="libraryUrl" value="/console/libraryDictionary.html">
			<c:param name="baseNamespace" value="${library.library.repositoryItem.baseNamespace}" />
			<c:param name="filename" value="${library.library.repositoryItem.filename}" />
			<c:param name="version" value="${library.library.repositoryItem.version}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/library.png" />&nbsp;<a href="${libraryUrl}">${library.library.itemName}</a>
				<br><small>${library.library.itemNamespace}</small>
			</td>
			<td>
				<spring:message code="${library.library.status}" />
			</td>
			<td>
				<c:choose>
					<c:when test="${library.effectiveDate != null}">
						${pageUtils.formatDateTime( library.effectiveDate )}
					</c:when>
					<c:otherwise>
						Latest Commit
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<c:choose>
			<c:when test="${rowStyle=='d0'}">
				<c:set var="rowStyle" value="d1" />
			</c:when>
			<c:otherwise>
				<c:set var="rowStyle" value="d0" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
	<c:forEach var="externalLib" items="${externalReferences}">
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/library.png" />&nbsp;${externalLib.libraryName}
				<br><small>${externalLib.namespace} [${externalLib.repositoryID}]</small>
			</td>
			<td>&nbsp;</td>
			<td>
				<c:choose>
					<c:when test="${externalLib.effectiveDate != null}">
						${pageUtils.formatXmlDateTime( externalLib.effectiveDate )}
					</c:when>
					<c:otherwise>
						Latest Commit
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<c:choose>
			<c:when test="${rowStyle=='d0'}">
				<c:set var="rowStyle" value="d1" />
			</c:when>
			<c:otherwise>
				<c:set var="rowStyle" value="d0" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
</table>

<c:set var="itemTypeTag" value="release" />
<%@include file="clipboardDependencyJS.jsp" %>
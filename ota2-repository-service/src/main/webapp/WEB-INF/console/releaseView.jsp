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
<c:url var="namespaceUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
</c:url>
<c:url var="allVersionsUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
	<c:param name="filename" value="${item.filename}" />
</c:url>

<table style="border-collapse:collapse;width:75%;float:left;">
	<tr>
		<td>
			<h2 style="padding-bottom: 0;">Release: ${item.libraryName} <small>(${item.version})</small></h2>
			<h3 style="padding-top: 0;">Namespace: <a href="${namespaceUrl}">${item.namespace}</a></h3>
		</td>
		<td style="white-space:nowrap;width:10%;text-align:right;">
			Maven Plugin Dependency:
			<img src="${pageContext.request.contextPath}/images/clipboard.png"
					onclick="copyDependencyToClipboard();" style="cursor:pointer;" />
			<c:if test="${sessionScope.isAdminAuthorized}">
				<c:url var="deleteItemUrl" value="/console/adminDeleteItem.html">
					<c:param name="baseNamespace" value="${item.baseNamespace}" />
					<c:param name="filename" value="${item.filename}" />
					<c:param name="version" value="${item.version}" />
				</c:url>
				<br/>[ <a href="${deleteItemUrl}">Delete this Release</a> ]
</c:if>
		</td>
	</tr>
</table>

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

<br/>
<table id="itemtable" style="width:75%;float:left;margin-top:25px;">
	<tr>
		<th width="70%">Referenced Libraries</th>
		<th width="30%">Effective Date</th>
	</tr>
	<c:if test="${principalLibraries.isEmpty()}">
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
</table>


<table id="itemtable" style="width:75%; float:left; margin-top:25px;">
	<tr>
		<th width="70%">Referenced Libraries</th>
		<th width="30%">Effective Date</th>
	</tr>
	<c:if test="${referencedLibraries.isEmpty()}">
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
</table>

<script lang="javascript">
function copyDependencyToClipboard() {
	var dependencyText = "<release>\n"
		+ "\t<baseNamespace>${item.baseNamespace}</baseNamespace>\n"
		+ "\t<filename>${item.filename}</filename>\n"
		+ "\t<version>${item.version}</version>\n"
		+ "</release>"
	
    if (window.clipboardData && window.clipboardData.setData) {
        // IE specific code path to prevent textarea being shown while dialog is visible.
        alert("Maven plugin dependency copied to clipboard.");
        return clipboardData.setData("Text", dependencyText); 

    } else if (document.queryCommandSupported && document.queryCommandSupported("copy")) {
        var textarea = document.createElement("textarea");
        textarea.textContent = dependencyText;
        textarea.style.position = "fixed";  // Prevent scrolling to bottom of page in MS Edge.
        document.body.appendChild(textarea);
        textarea.select();
        try {
            alert("Maven plugin dependency copied to clipboard.");
            return document.execCommand("copy");  // Security exception may be thrown by some browsers.
        } catch (ex) {
            console.warn("Copy to clipboard failed.", ex);
            return false;
        } finally {
            document.body.removeChild(textarea);
        }
    }
}
</script>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:choose>
	<c:when test="${parentItems != null}">
		<small>
			[ <a href="${pageContext.request.contextPath}/console/browse.html">Back to Top</a> ]
			<c:if test="${canCreateNamespaceExtension}">
				<c:url var="createNamespaceUrl" value="/console/createNamespace.html">
					<c:param name="baseNamespace" value="${baseNamespace}" />
				</c:url>
				&nbsp; [ <a href="${createNamespaceUrl}">Create a Namespace Extension</a> ]
			</c:if>
			<c:if test="${canDeleteNamespace}">
				<c:url var="deleteNamespaceUrl" value="/console/deleteNamespace.html">
					<c:param name="baseNamespace" value="${baseNamespace}" />
				</c:url>
				&nbsp; [ <a href="${deleteNamespaceUrl}">Delete This Namespace</a> ]
			</c:if>
		</small>
		<h3>
			Namespace: &nbsp;
			<c:forEach var="item" items="${parentItems}">
				<c:url var="itemUrl" value="/console/browse.html">
					<c:param name="baseNamespace" value="${item.baseNamespace}" />
				</c:url>
				<a href="${itemUrl}">${item.label}</a> /
			</c:forEach>
			<c:choose>
				<c:when test="${libraryName != null}">
					<br/>All Versions Of: ${libraryName}<br/>
				</c:when>
			</c:choose>
		</h3>
	</c:when>
	<c:otherwise>
		<small>&nbsp;</small>
		<h3>Root Namespaces</h3>
	</c:otherwise>
</c:choose>
<table id="browsetable">
	<tr>
		<th>Name</th>
		<th>Version</th>
		<th>Status</th>
		<th>Last Modified</th>
	</tr>
	<c:if test="${browseItems.isEmpty()}">
		<tr class="${rowStyle}">
			<td colspan="4">No items to display for this namespace.</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="item" items="${browseItems}">
		<c:choose>
			<c:when test="${filename == null}">
				<c:choose>
					<c:when test="${item.filename == null}">
						<c:url var="itemUrl" value="/console/browse.html">
							<c:param name="baseNamespace" value="${item.baseNamespace}" />
						</c:url>
						<c:set var="versionUrl" value="${null}" />
					</c:when>
					<c:otherwise>
						<c:url var="itemUrl" value="/console/browse.html">
							<c:param name="baseNamespace" value="${item.baseNamespace}" />
							<c:param name="filename" value="${item.filename}" />
						</c:url>
						<c:url var="versionUrl" value="/console/itemDetails.html">
							<c:param name="baseNamespace" value="${item.baseNamespace}" />
							<c:param name="filename" value="${item.filename}" />
							<c:param name="version" value="${item.version}" />
						</c:url>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
				<c:url var="itemUrl" value="/console/itemDetails.html">
					<c:param name="baseNamespace" value="${item.baseNamespace}" />
					<c:param name="filename" value="${item.filename}" />
					<c:param name="version" value="${item.version}" />
				</c:url>
				<c:set var="versionUrl" value="${itemUrl}" />
			</c:otherwise>
		</c:choose>
		<tr class="${rowStyle}">
			<td><a href="${itemUrl}">${item.label}</a></td>
			<td><c:if test="${item.version != null}"><a href="${versionUrl}">${item.version}</a></c:if></td>
			<td><c:if test="${item.status != null}">${item.status}</c:if></td>
			<td><c:if test="${item.lastModified != null}"><fmt:formatDate value="${item.lastModified}" type="date" pattern="dd-MMM-yyyy" /></c:if></td>
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

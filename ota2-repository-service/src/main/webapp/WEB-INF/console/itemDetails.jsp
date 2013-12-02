<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:url var="namespaceUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
</c:url>
<c:url var="allVersionsUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${item.baseNamespace}" />
	<c:param name="filename" value="${item.filename}" />
</c:url>
<table id="browsetable">
	<tr class="d0">
		<td>Library Name:</td>
		<td><a href="${allVersionsUrl}">${item.libraryName}</a></td>
	</tr>
	<tr class="d1">
		<td>Namespace:</td>
		<td><a href="${namespaceUrl}">${item.namespace}</a></td>
	</tr>
	<tr class="d0">
		<td>Version:</td>
		<td>${item.version}</td>
	</tr>
	<tr class="d1">
		<td>Filename:</td>
		<td>${item.filename}</td>
	</tr>
	<tr class="d0">
		<td>Status:</td>
		<td>${item.status}
			<c:if test="${sessionScope.isAdminAuthorized}">
				<c:if test="${item.status.toString()=='DRAFT'}">
					<c:url var="promoteItemUrl" value="/console/adminPromoteItem.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${promoteItemUrl}">Finalize this item</a> ]
				</c:if>
				<c:if test="${item.status.toString()=='FINAL'}">
					<c:url var="demoteItemUrl" value="/console/adminDemoteItem.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					<c:url var="recalculateItemCrcUrl" value="/console/adminRecalculateItemCrc.html">
						<c:param name="baseNamespace" value="${item.baseNamespace}" />
						<c:param name="filename" value="${item.filename}" />
						<c:param name="version" value="${item.version}" />
					</c:url>
					&nbsp; &nbsp; [ <a href="${demoteItemUrl}">Demote to DRAFT</a> ]
					&nbsp; &nbsp; [ <a href="${recalculateItemCrcUrl}">Recalculate CRC</a> ]
				</c:if>
			</c:if>
		</td>
	</tr>
	<tr class="d1">
		<td>Repository State:</td>
		<td>${item.state}</td>
	</tr>
	<c:if test="${item.lockedByUser != null}">
		<tr class="d0">
			<td>Locked By:</td>
			<td>${item.lockedByUser}</td>
		</tr>
	</c:if>
	<c:if test="${sessionScope.isAdminAuthorized}">
		<c:url var="deleteItemUrl" value="/console/adminDeleteItem.html">
			<c:param name="baseNamespace" value="${item.baseNamespace}" />
			<c:param name="filename" value="${item.filename}" />
			<c:param name="version" value="${item.version}" />
		</c:url>
		<tr class="d0">
			<td colspan="2"><br/>[ <a href="${deleteItemUrl}">Delete this Item</a> ]</td>
		</tr>
	</c:if>
</table>
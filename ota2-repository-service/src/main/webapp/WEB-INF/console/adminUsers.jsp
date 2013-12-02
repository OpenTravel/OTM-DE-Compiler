<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<table id="userTable">
	<tr>
	<c:set var="count" value="0" />
	<c:forEach var="userId" items="${userAccounts}">
		<c:url var="editUrl" value="/console/adminUsersChangePassword.html">
			<c:param name="userId" value="${userId}" />
		</c:url>
		<c:url var="deleteUrl" value="/console/adminUsersDelete.html">
			<c:param name="userId" value="${userId}" />
		</c:url>
		<td>${userId} &nbsp;
		<a href="${editUrl}" title="Change Password"><img src="${pageContext.request.contextPath}/images/edit.png" class="imageLink"/></a>
		 <a href="${deleteUrl}" title="Delete User"><img src="${pageContext.request.contextPath}/images/delete.png" class="imageLink"/></a></td>
		<c:choose>
			<c:when test="${count == 4}">
				<%= "</tr><tr>" %>
				<c:set var="count" value="0" />
			</c:when>
			<c:otherwise>
				<c:set var="count" value="${count + 1}" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
	<c:forEach var="i" begin="${count}" end="4">
		<td></td>
	</c:forEach>
	</tr>
</table>
<div style="clear:both;">
	<br/><a href="${pageContext.request.contextPath}/console/adminUsersAdd.html">Add a New User</a>
</div>

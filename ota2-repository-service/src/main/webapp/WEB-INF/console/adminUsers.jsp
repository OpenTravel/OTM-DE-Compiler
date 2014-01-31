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

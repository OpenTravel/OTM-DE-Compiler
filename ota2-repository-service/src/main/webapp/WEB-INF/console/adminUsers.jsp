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
		<th>Action</th>
		<th>User ID</th>
		<th>Full Name</th>
		<th>Email</th>
	</tr>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="user" items="${userAccounts}">
		<c:url var="editUrl" value="/console/adminUsersEditLocal.html">
			<c:param name="userId" value="${user.userId}" />
		</c:url>
		<c:url var="passwordUrl" value="/console/adminUsersChangePassword.html">
			<c:param name="userId" value="${user.userId}" />
		</c:url>
		<c:url var="deleteUrl" value="/console/adminUsersDelete.html">
			<c:param name="userId" value="${user.userId}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<c:if test="${isLocalUserManagement}">
					<a href="${editUrl}" title="Edit User"><img src="${pageContext.request.contextPath}/images/edit.png" class="imageLink"/></a>
					<a href="${passwordUrl}" title="Change Password"><img src="${pageContext.request.contextPath}/images/passwd.png" class="imageLink"/></a>
				</c:if>
				<a href="${deleteUrl}" title="Delete User"><img src="${pageContext.request.contextPath}/images/delete.png" class="imageLink"/></a>
			</td>
			<td>${user.userId}</td>
			<td>${user.lastName}<c:if test="${(user.firstName!=null)&&(user.firstName!='')}">, ${user.firstName}</c:if></td>
			<td>${user.emailAddress}</td>
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
<div style="clear:both;">
	<br/>
	<c:choose>
		<c:when test="${isLocalUserManagement}">
			<a href="${pageContext.request.contextPath}/console/adminUsersAddLocal.html">Add a New User</a>
		</c:when>
		<c:otherwise>
			<a href="${pageContext.request.contextPath}/console/adminUsersAddDirectory.html">Add a New User</a>
		</c:otherwise>
	</c:choose>
</div>

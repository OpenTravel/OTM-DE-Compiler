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
<form id="addUserForm" action="${pageContext.request.contextPath}/console/adminUsersAddDirectory.html" method="post">
<input id="createUserInput" name="createUser" type="hidden" value="false" />
<table style="border: 0px solid black; width: 100%;"><tr><td>

<table id="editUserTable">
	<tr>
		<td>Search Criteria: </td>
		<td><input name="searchFilter" type="text" value="${searchFilter}" /></td>
		<td> &nbsp; <input type="submit" value="Search User Directory" class="formButton" /></td>
	</tr>
	<tr>
		<td>Max Results: </td>
		<td colspan="2">
			<select name="maxResults">
				<option value="10" <c:if test="${maxResults==10}">selected</c:if>>10</option>
				<option value="20" <c:if test="${maxResults==20}">selected</c:if>>20</option>
				<option value="50" <c:if test="${maxResults==50}">selected</c:if>>50</option>
			</select>
		</td>
	</tr>
</table>

</td></tr>

<c:if test="${(candidateUsers != null) && !candidateUsers.isEmpty()}">
	<tr><td>
	<table id="userTable">
		<tr>
			<th>&nbsp;</th>
			<th>User ID</th>
			<th>Full Name</th>
			<th>Email</th>
		</tr>
		<c:set var="rowStyle" value="d0" />
		<c:forEach var="user" items="${candidateUsers}">
			<tr class="${rowStyle}">
				<td><input name="userId" type="radio" value="${user.userId}" onchange="handleUserSelect();" /></td>
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
		<tr>
			<td colspan="4">
				<input id="createUserButton" type="button" value="Create User" class="formButton" disabled onclick="submitCreateUser();" />
			</td>
		</tr>
	</table>
	</td></tr>
</c:if>

</table>

</form>
<script type="text/javascript">

function handleUserSelect() {
	document.getElementById("createUserButton").disabled = false;
}

function submitCreateUser() {
	document.getElementById("createUserInput").value = "true";
	document.getElementById("addUserForm").submit();
}

</script>
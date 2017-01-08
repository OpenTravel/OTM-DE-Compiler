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
<c:choose>
	<c:when test="${sessionScope.user == null}">
		<form action="${pageContext.request.contextPath}/console/login.html" method="post" id="headerlogin">
			<c:if test="${loginError}">
				<span class="loginError">Invalid user ID or password credentials.</span><br>
			</c:if>
			User ID <input name="userid" type="text" value="${userId}" width="8" />
			&nbsp;
			Password <input name="password" type="password" width="8" />
			&nbsp;
			<input value="Login" type="submit" class="loginButton" />
		</form>
	</c:when>
	<c:otherwise>
		<div id="headerlogin">
			<c:set var="user" value="${sessionScope.user}" />
			User: <span id="userid"><c:if test="${(user.firstName!=null)&&(user.firstName!='')}">${user.firstName} </c:if>${user.lastName}</span>
			[ <a href="${pageContext.request.contextPath}/console/logout.html">Logout</a> ]
			<c:if test="${isLocalUserManagement}">
				[ <a href="${pageContext.request.contextPath}/console/editUserProfile.html">Edit Profile</a> ]
				[ <a href="${pageContext.request.contextPath}/console/changePassword.html">Change Password</a> ]
			</c:if>
		</div>
	</c:otherwise>
</c:choose>
<br>
<a href="${pageContext.request.contextPath}/console/browse.html">Browse</a>
&nbsp;|&nbsp;
<a href="${pageContext.request.contextPath}/console/search.html">Search</a>
<c:if test="${sessionScope.user != null}">
&nbsp;|&nbsp;
<a href="${pageContext.request.contextPath}/console/lockedLibraries.html">Locked Libraries</a>
</c:if>
<c:if test="${sessionScope.user != null}">
&nbsp;|&nbsp;
<a href="${pageContext.request.contextPath}/console/subscriptions.html">Subscriptions</a>
</c:if>
<c:if test="${(sessionScope.user != null) && sessionScope.isAdminAuthorized}">
	&nbsp;|&nbsp;
	<a href="${pageContext.request.contextPath}/console/adminHome.html">Administration</a>
</c:if>

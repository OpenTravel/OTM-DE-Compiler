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
			<input name="currentPage" type="hidden" value="${currentPage}" />
			<input value="Login" type="submit" class="loginButton" />
		</form>
	</c:when>
	<c:otherwise>
		<div id="headerlogin">
			User: <span id="userid">${sessionScope.user.userId}</span>
			[ <a href="${pageContext.request.contextPath}/console/logout.html">Logout</a> ]
			<c:if test="${isLocalUserManagement}">
				[ <a href="${pageContext.request.contextPath}/console/changePassword.html?currentPage=${currentPage}">Change Password</a> ]
			</c:if>
		</div>
	</c:otherwise>
</c:choose>
<br>
<a href="${pageContext.request.contextPath}/console/browse.html">BROWSE</a>
&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
<a href="${pageContext.request.contextPath}/console/search.html">SEARCH</a>
<c:if test="${sessionScope.isAdminAuthorized}">
	&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
	<a href="${pageContext.request.contextPath}/console/adminHome.html">ADMINISTRATION</a>
</c:if>

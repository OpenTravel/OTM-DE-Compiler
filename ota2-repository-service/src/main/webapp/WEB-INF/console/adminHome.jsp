<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<ul>
	<li><h3><a href="${pageContext.request.contextPath}/console/adminChangeRepositoryName.html">Change Repository Name</a></h3></li>
</ul>
<ul>
	<li><h3><a href="${pageContext.request.contextPath}/console/adminManageRootNamespaces.html">Manage Root Namespaces</a></h3></li>
</ul>
<ul>
	<li><h3><a href="${pageContext.request.contextPath}/console/adminPermissions.html">Manage Namespace Permissions</a></h3></li>
</ul>
<c:if test="${isLocalUserManagement}">
<ul>
	<li><h3><a href="${pageContext.request.contextPath}/console/adminUsers.html">Manage User Accounts</a></h3></li>
</ul>
</c:if>
<ul>
	<li><h3><a href="${pageContext.request.contextPath}/console/adminGroups.html">Manage Group Assignments</a></h3></li>
</ul>
<ul>
	<li><h3><a href="${pageContext.request.contextPath}/console/adminSearchIndex.html">Refresh Free-Text Search Index</a></h3></li>
</ul>
<c:if test="${isDevelopmentRepository}">
<ul>
	<li><h3><a href="${pageContext.request.contextPath}/console/adminDeleteRepository.html">Delete Repository Contents (Development Only)</a></h3></li>
</ul>
</c:if>

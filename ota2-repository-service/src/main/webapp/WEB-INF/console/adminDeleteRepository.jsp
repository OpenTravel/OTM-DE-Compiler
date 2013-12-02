<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
	<c:when test="${confirmDeletion}">
		<p><br>
		<span class="confirmMessage">The contents of the repository have been deleted.</span>
		<p><br>
		<a href="${pageContext.request.contextPath}/console/adminHome.html">Back to Administration Home</a>
	</c:when>
	<c:otherwise>
		<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminDeleteRepository.html" method="POST">
			<span class="confirmMessage">Are you sure you want to delete the contents of this repository?<br/>This action cannot be undone.</span>
			<p><br>
			<input name="confirmDeletion" type="hidden" value="true" />
			<input type="submit" value="Continue with Repository Deletion" class="formButton" />
		</form>
	</c:otherwise>
</c:choose>
<p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br>
<p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br>

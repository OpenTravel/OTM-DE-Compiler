<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
	<c:when test="${confirmReindexing}">
		<p><br>
		<span class="confirmMessage">The search indexing request has been submitted.  It may take several minutes for the reindexing job to complete.</span>
		<p><br>
		<a href="${pageContext.request.contextPath}/console/adminHome.html">Back to Administration Home</a>
	</c:when>
	<c:otherwise>
		<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminSearchIndex.html" method="POST">
			<span class="confirmMessage">Refresh the search index for the entire repository?<br/>All existing indexes will be deleted - this operation may take several minutes.</span>
			<p><br>
			<input name="confirmReindexing" type="hidden" value="true" />
			<input type="submit" value="Continue with Search Indexing" class="formButton" />
		</form>
	</c:otherwise>
</c:choose>
<p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br>
<p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br>

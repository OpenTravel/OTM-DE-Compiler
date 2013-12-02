<form id="confirmForm" action="${pageContext.request.contextPath}/console/deleteNamespace.html" method="POST">
	<span class="confirmMessage">Delete namespace "${baseNamespace}".  Are you sure?</span>
	<p><br>
	<input name="baseNamespace" type="hidden" value="${baseNamespace}" />
	<input name="confirmDelete" type="hidden" value="true" />
	<input type="submit" value="Delete" class="formButton" />
</form>

<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminUsersDelete.html" method="POST">
	<span class="confirmMessage">Delete user account "${userId}".  Are you sure?</span>
	<p><br>
	<input name="userId" type="hidden" value="${userId}" />
	<input name="confirmDelete" type="hidden" value="true" />
	<input type="submit" value="Delete User Account" class="formButton" />
</form>

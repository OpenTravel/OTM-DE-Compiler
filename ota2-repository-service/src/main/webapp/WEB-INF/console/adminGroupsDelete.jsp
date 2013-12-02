<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminGroupsDelete.html" method="POST">
	<span class="confirmMessage">Delete group "${groupName}".  Are you sure?</span>
	<p><br>
	<input name="groupName" type="hidden" value="${groupName}" />
	<input name="confirmDelete" type="hidden" value="true" />
	<input type="submit" value="Delete Group" class="formButton" />
</form>

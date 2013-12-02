<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminDemoteItem.html" method="POST">
	<span class="confirmMessage">Demote repository item "${item.filename}" to DRAFT status.  The library will be editable by authorized users.<br/>Are you sure?</span>
	<p><br>
	<input name="baseNamespace" type="hidden" value="${item.baseNamespace}" />
	<input name="filename" type="hidden" value="${item.filename}" />
	<input name="version" type="hidden" value="${item.version}" />
	<input name="confirmDemote" type="hidden" value="true" />
	<input type="submit" value="Demote Item" class="formButton" />
</form>

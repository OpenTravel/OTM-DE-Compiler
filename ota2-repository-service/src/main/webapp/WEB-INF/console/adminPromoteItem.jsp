<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminPromoteItem.html" method="POST">
	<span class="confirmMessage">Promote repository item "${item.filename}" to FINAL status.  The library will no longer be editable.<br/>Are you sure?</span>
	<p><br>
	<input name="baseNamespace" type="hidden" value="${item.baseNamespace}" />
	<input name="filename" type="hidden" value="${item.filename}" />
	<input name="version" type="hidden" value="${item.version}" />
	<input name="confirmPromote" type="hidden" value="true" />
	<input type="submit" value="Promote/Finalize Item" class="formButton" />
</form>

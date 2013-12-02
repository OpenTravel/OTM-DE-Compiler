<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminDeleteItem.html" method="POST">
	<span class="confirmMessage">Delete repository item "${item.filename}".  Are you sure?</span>
	<p><br>
	<input name="baseNamespace" type="hidden" value="${item.baseNamespace}" />
	<input name="filename" type="hidden" value="${item.filename}" />
	<input name="version" type="hidden" value="${item.version}" />
	<input name="confirmDelete" type="hidden" value="true" />
	<input type="submit" value="Delete Item" class="formButton" />
</form>

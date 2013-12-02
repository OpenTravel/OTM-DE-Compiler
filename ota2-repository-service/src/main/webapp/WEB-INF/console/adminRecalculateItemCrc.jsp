<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminRecalculateItemCrc.html" method="POST">
	<span class="confirmMessage">Recalculate the CRC for repository item "${item.filename}".  Are you sure?</span>
	<p><br>
	<input name="baseNamespace" type="hidden" value="${item.baseNamespace}" />
	<input name="filename" type="hidden" value="${item.filename}" />
	<input name="version" type="hidden" value="${item.version}" />
	<input name="confirmRecalculate" type="hidden" value="true" />
	<input type="submit" value="Recalculate Item CRC" class="formButton" />
</form>

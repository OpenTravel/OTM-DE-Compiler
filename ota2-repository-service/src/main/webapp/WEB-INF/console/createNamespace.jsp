<small>Enter the namespace extension to be created.<br/></small>
<form id="confirmForm" action="${pageContext.request.contextPath}/console/createNamespace.html" method="POST">
	<br/>${baseNamespace}/ <input name="nsExtension" type="text" value="${nsExtension}" size="20"/>
	<input name="baseNamespace" type="hidden" value="${baseNamespace}" />
	<input type="submit" value="Create" class="formButton" />
</form>

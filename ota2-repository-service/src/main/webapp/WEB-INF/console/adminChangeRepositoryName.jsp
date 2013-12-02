<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form action="${pageContext.request.contextPath}/console/adminChangeRepositoryName.html" method="post">
<br/>
<table id="changeNameTable">
	<tr>
		<td>Display Name:</td>
		<td><input name="displayName" type="text" value="${displayName}" size="60" /></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>
			<input type="submit" value="Update" class="formButton" />
		</td>
	</tr>
</table>
</form>

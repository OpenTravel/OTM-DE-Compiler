<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form id="changePasswordForm" action="${pageContext.request.contextPath}/console/adminUsersAdd.html" method="post">
<table id="passwordTable">
	<tr>
		<td>User ID:</td>
		<td><input name="userId" type="text" value="${userId}" /></td>
	</tr>
	<tr>
		<td>Password:</td>
		<td><input name="password" type="password" /></td>
	</tr>
	<tr>
		<td>Confirm Password:</td>
		<td><input name="passwordConfirm" type="password" /></td>
	</tr>
	<tr>
		<td colspan="2" style="text-align:right;">
			<br/><input type="submit" value="Create User Account" class="formButton" />
		</td>
	</tr>
</table>
</form>

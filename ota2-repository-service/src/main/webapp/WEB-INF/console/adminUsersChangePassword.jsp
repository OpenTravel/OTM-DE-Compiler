<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form id="changePasswordForm" action="${pageContext.request.contextPath}/console/adminUsersChangePassword.html" method="post">
<table id="passwordTable">
	<tr>
		<td>User ID:</td>
		<td>${userId} <input name="userId" type="hidden" value="${userId}" /></td>
	</tr>
	<tr>
		<td>New Password:</td>
		<td><input name="newPassword" type="password" /></td>
	</tr>
	<tr>
		<td>Confirm New Password:</td>
		<td><input name="newPasswordConfirm" type="password" /></td>
	</tr>
	<tr>
		<td colspan="2" style="text-align:right;">
			<br/><input type="submit" value="Change Password" class="formButton" />
		</td>
	</tr>
</table>
</form>

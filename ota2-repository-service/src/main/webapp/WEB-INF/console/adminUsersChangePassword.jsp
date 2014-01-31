<%--

    Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
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

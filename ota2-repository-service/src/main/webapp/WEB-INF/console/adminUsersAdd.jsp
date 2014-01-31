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

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
<form id="renameForm" action="${pageContext.request.contextPath}/console/adminChangeRepositoryName.html" method="post">
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

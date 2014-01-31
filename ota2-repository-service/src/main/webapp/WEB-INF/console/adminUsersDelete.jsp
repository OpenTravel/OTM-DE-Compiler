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
<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminUsersDelete.html" method="POST">
	<span class="confirmMessage">Delete user account "${userId}".  Are you sure?</span>
	<p><br>
	<input name="userId" type="hidden" value="${userId}" />
	<input name="confirmDelete" type="hidden" value="true" />
	<input type="submit" value="Delete User Account" class="formButton" />
</form>

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
<c:choose>
	<c:when test="${confirmDeletion}">
		<p><br>
		<span class="confirmMessage">The contents of the repository have been deleted.</span>
		<p><br>
		<a href="${pageContext.request.contextPath}/console/adminHome.html">Back to Administration Home</a>
	</c:when>
	<c:otherwise>
		<form id="confirmForm" action="${pageContext.request.contextPath}/console/adminDeleteRepository.html" method="POST">
			<span class="confirmMessage">Are you sure you want to delete the contents of this repository?<br/>This action cannot be undone.</span>
			<p><br>
			<input name="confirmDeletion" type="hidden" value="true" />
			<input type="submit" value="Continue with Repository Deletion" class="formButton" />
		</form>
	</c:otherwise>
</c:choose>
<p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br>
<p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br><p><br>

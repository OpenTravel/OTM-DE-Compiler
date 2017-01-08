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
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<form id="editUserForm" action="${pageContext.request.contextPath}/console/librarySubscription.html" method="post">
<input name="updateSubscription" type="hidden" value="true" />
<input name="baseNamespace" type="hidden" value="${baseNamespace}" />
<input name="libraryName" type="hidden" value="${libraryName}" />
<input name="filename" type="hidden" value="${filename}" />
<input name="version" type="hidden" value="${version}" />
<input name="allVersions" type="hidden" value="${allVersions}" />
<b>Library: </b>${item.libraryName} <small>(<c:if test="${allVersions}">All Versions</c:if><c:if test="${!allVersions}">${item.version}</c:if>)</small>
<br/><b>Namespace: </b>${item.namespace}
<p><br/>
<span>From the table below, select the events for which you wish to receive email notifications for
	<c:choose>
		<c:when test="${allVersions}">all versions of this library.</c:when>
		<c:otherwise>version ${item.version} of this library.</c:otherwise>
	</c:choose>
</span>
<p><br/>
<table id="userTable" style="margin-left:25px">
	<tr class="d0">
		<th><small><a href="#" onclick="selectAll();"><span style="color:#FFFFFF">Select All</span></a></small></th>
		<th>Notification Event</th>
	</tr>
	<tr class="d0">
		<td style="text-align:center;"><input name="etLibraryPublish" type="checkbox" value="true" ${etLibraryPublish ? 'checked' : ''} /></td>
		<td><spring:message code="LIBRARY_PUBLISH" /></td>
	</tr>
	<tr class="d1">
		<td style="text-align:center;"><input name="etLibraryNewVersion" type="checkbox" value="true" ${etLibraryNewVersion ? 'checked' : ''} /></td>
		<td><spring:message code="LIBRARY_NEW_VERSION" /></td>
	</tr>
	<tr class="d0">
		<td style="text-align:center;"><input name="etLibraryStatusChange" type="checkbox" value="true" ${etLibraryStatusChange ? 'checked' : ''} /></td>
		<td><spring:message code="LIBRARY_STATUS_CHANGE" /></td>
	</tr>
	<tr class="d1">
		<td style="text-align:center;"><input name="etLibraryStateChange" type="checkbox" value="true" ${etLibraryStateChange ? 'checked' : ''} /></td>
		<td><spring:message code="LIBRARY_STATE_CHANGE" /></td>
	</tr>
	<tr class="d0">
		<td style="text-align:center;"><input name="etLibraryCommit" type="checkbox" value="true" ${etLibraryCommit ? 'checked' : ''} /></td>
		<td><spring:message code="LIBRARY_COMMIT" /></td>
	</tr>
	<tr class="d1">
		<td style="text-align:center;"><input name="etLibraryMoveOrRename" type="checkbox" value="true" ${etLibraryMoveOrRename ? 'checked' : ''} /></td>
		<td><spring:message code="LIBRARY_MOVE_OR_RENAME" /></td>
	</tr>
	<tr>
		<td colspan="2" style="text-align:right;">
			<c:choose>
				<c:when test="${filename == null}">
					<c:url var="cancelUrl" value="/console/subscriptions.html" />
				</c:when>
				<c:otherwise>
					<c:url var="cancelUrl" value="/console/libraryInfo.html">
						<c:param name="baseNamespace" value="${baseNamespace}" />
						<c:param name="filename" value="${filename}" />
						<c:param name="version" value="${version}" />
					</c:url>
				</c:otherwise>
			</c:choose>
			<br/><small><a href="${cancelUrl}">Cancel</a></small>
			<input type="submit" value="Update Subscriptions" class="formButton" />
		</td>
	</tr>
</table>
</form>

<script type="text/javascript">
function selectAll() {
    var tableElement = document.getElementById('userTable');
    var inputElements = tableElement.getElementsByTagName('input');
    var allSelected = true;
    
    for (i = 0; i < inputElements.length; i++) {
        if (inputElements[i].type != 'checkbox') continue;
        allSelected &= inputElements[i].checked;
    }
    for (i = 0; i < inputElements.length; i++) {
        if (inputElements[i].type != 'checkbox') continue;
        inputElements[i].checked = !allSelected;
    }
}
</script>
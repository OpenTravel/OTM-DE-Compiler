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
<small>Select a root namespace to delete or use the text field below to create a new one.<br/></small>
<form  id="rootNamespaceForm" action="${pageContext.request.contextPath}/console/adminManageRootNamespaces.html" method="post" onsubmit="return prepareForm();">
<input name="action" type="hidden" value="" />
<input name="rootNamespace" type="hidden" value="" />
<table>
	<tr><td>
		<br/>Root Namespace: <input name="newRootNamespace" type="text" value="${newRootNamespace}" size="30"/>
		&nbsp; <input type="submit" value="Create" class="formButton" onclick="createRootNamespace();"/>
	</td></tr>
	<tr><td>
		<br/>
		<table id="rootNamespaceTable">
			<c:if test="${rootNamespaceItems.isEmpty()}">
				<tr><td style="text-align:center;">No root namespaces have been defined for this repository.</td></tr>
			</c:if>
			<c:forEach var="rootNS" items="${rootNamespaceItems}">
				<tr>
					<td style="text-align:center;">
						<c:if test="${rootNS.canDelete}">
							<a href="#" onclick="deleteRootNamespace('${rootNS.baseNamespace}');" title="Delete Root Namespace"><img src="${pageContext.request.contextPath}/images/delete.png" class="imageLink"/></a>
						</c:if>
						<c:if test="${!rootNS.canDelete}">&nbsp;</c:if>
					</td>
					<td>${rootNS.label}</td>
				</tr>
			</c:forEach>
		</table>
	</td></tr>
	<tr><td>
		<div><small>** Only empty namespaces can be deleted.<br/><br/></small></div>
	</td></tr>
</table>
</form>

<script type="text/javascript">

function createRootNamespace() {
	var form = document.forms["rootNamespaceForm"];
	var newRootNamespace = form.elements["newRootNamespace"];
	var rootNamespace = form.elements["rootNamespace"];
	var formAction = form.elements["action"];
	
	if (newRootNamespace.value == "") {
		alert("Please enter a root namespace URI.");
	} else {
		rootNamespace.value = newRootNamespace.value;
		formAction.value = "create";
		form.submit();
	}
}

function deleteRootNamespace( rootNS ) {
	var confirmDelete = confirm("Delete root namespace: " + rootNS + "\n\nAre you sure?");
	
	if (confirmDelete) {
		var form = document.forms["rootNamespaceForm"];
		var rootNamespace = form.elements["rootNamespace"];
		var formAction = form.elements["action"];
		
		rootNamespace.value = rootNS;
		formAction.value = "delete";
		form.submit();
	}
}

</script>

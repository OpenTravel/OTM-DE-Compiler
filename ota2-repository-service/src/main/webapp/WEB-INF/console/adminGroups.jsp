<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<small>Click on a radio button in the list below to view members of the selected group.<br/><br/></small>
<form>
<table id="groupTable">
	<c:forEach var="group" items="${allGroups}">
		<c:url var="editUrl" value="/console/adminGroupsEdit.html">
			<c:param name="groupName" value="${group.groupName}" />
		</c:url>
		<c:url var="deleteUrl" value="/console/adminGroupsDelete.html">
			<c:param name="groupName" value="${group.groupName}" />
		</c:url>
		<tr>
			<td><input name="selectedGroup" type="radio" value="${group.groupName}" onclick="displayGroupMembers('${group.groupName}');" /></td>
			<td>${group.groupName}</td>
			<td>
				<a href="${editUrl}" title="Edit Group Members"><img src="${pageContext.request.contextPath}/images/edit.png" class="imageLink"/></a>
				<a href="${deleteUrl}" title="Delete Group"><img src="${pageContext.request.contextPath}/images/delete.png" class="imageLink"/></a>
			</td>
		</tr>
	</c:forEach>
</table>
<div>
<table id="groupMembersTable">
	<tr>
		<td>
			<select size="10" id="groupMembersSelect">
			</select>
		</td>
	</tr>
</table>
</div>
</form>
<br>
<div style="clear:both;">
	<br/><a href="${pageContext.request.contextPath}/console/adminGroupsAdd.html">Add a New Group</a>
</div>
<script type="text/javascript">
var groupAssignments = [
<c:forEach var="group" items="${allGroups}">
	{ groupName: "${group.groupName}", members: new Array( <c:set var="firstId" value="true"/><c:forEach var="userId" items="${group.memberIds}"><c:if test="${firstId!=true}">,</c:if><c:set var="firstId" value="false"/>"${userId}"</c:forEach> ) },
</c:forEach>
];
function displayGroupMembers( groupName ) {
	var groupMembers = null;
	
	for (var i = 0; i < groupAssignments.length; i++) {
		if (groupAssignments[i].groupName == groupName) {
			groupMembers = groupAssignments[i].members;
			break;
		}
	}
	if (groupMembers != null) {
		var selectList = document.getElementById("groupMembersSelect");
		selectList.options.length = 0;
		
		for (var i = 0; i < groupMembers.length; i++) {
			var option = document.createElement('option');
			
			option.setAttribute('value', groupMembers[i]);
			option.appendChild(document.createTextNode(groupMembers[i]));
			selectList.appendChild(option);
		}
	}
}

// Select the first radio button and display group members when the page is rendered
var groupRadios = document.getElementsByName("selectedGroup");

if ((groupRadios != null) && (groupRadios.length > 0)) {
	groupRadios[0].checked = true;
	displayGroupMembers( groupRadios[0].value );
}
</script>

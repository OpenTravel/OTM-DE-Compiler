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
<h3>Group: ${groupName}</h3>
<small>Use the controls below to add or remove members for this group.<br/></small>
<form id="editForm" action="${pageContext.request.contextPath}/console/adminGroupsEdit.html" method="POST">
<table id="groupEditTable">
	<tr>
		<td>
			<small>Select an existing user:</small><br/>
			<select id="groupEditKnownUsers" onchange="clearNewUser();" style="width:20em;">
				<option value="" selected></option>
				<c:forEach var="user" items="${allUsers}">
					<option value="${user.userId}">${user.lastName}<c:if test="${(user.firstName!=null)&&(user.firstName!='')}">, ${user.firstName}</c:if> (${user.userId})</option>
				</c:forEach>
			</select>
		</td>
		<td style="text-align:center;">
			<input id="addButton" type="button" value="Add Member ->" disabled class="formButtonSmall" onclick="addSelectedUser();" style="width:100%;" />
			<br/><br/>
			<input id="removeButton" type="button" value="<- Remove Member" disabled class="formButtonSmall" onclick="removeSelectedUser();" />
		</td>
		<td>
			<select size="10" id="groupMembersSelect" onclick="groupMemberSelectionChanged();" style="width:20em;">
			</select>
			<input id="groupMembers" name="groupMembers" type="hidden" value="${groupMembers}" />
			<input name="groupName" type="hidden" value="${groupName}" />
		</td>
	</tr>
	<tr>
		<td colspan="3" style="text-align:center;">
			<input type="submit" value="Save Group Membership" class="formButton" />
			&nbsp;
			<input type="button" value="Cancel" class="formButton" onclick="location.href='${pageContext.request.contextPath}/console/adminGroups.html';" />
		</td>
	</tr>
</table>
</form>
<br>
<script type="text/javascript">
var userAccounts = new Object();
<c:forEach var="user" items="${allUsers}">
	userAccounts["${user.userId}"] = "${user.lastName}<c:if test="${(user.firstName!=null)&&(user.firstName!='')}">, ${user.firstName}</c:if>";
</c:forEach>

// Populate the select list of group members when the page is loaded
refreshGroupMembersSelect();

function addSelectedUser() {
	var newUserInput = document.getElementById("groupEditNewUser");
	var knownUsersSelect = document.getElementById("groupEditKnownUsers");
	var groupMembersSelect = document.getElementById("groupMembersSelect");
	var groupMembers = document.getElementById("groupMembers");
	var selectedUserId = null;
	
	if ((newUserInput != null) && (newUserInput.value != null)
			&& (newUserInput.value != "") && !isExistingUser(newUserInput.value)) { // unknown user selected
		
		if (newUserInput.value.indexOf(' ') >= 0) {
			alert("White space characters are not allowed in user ID's.");
		} else {
			selectedUserId = newUserInput.value;
		}
	} else { /* known user selected */
		selectedUserId = knownUsersSelect.options[knownUsersSelect.selectedIndex].value;
	}
	if ((selectedUserId != null) && (selectedUserId.length > 0) && !isExistingUser( selectedUserId )) {
		groupMembers.value = groupMembers.value + selectedUserId + ",";
		refreshGroupMembersSelect();
	}
}

function removeSelectedUser() {
	var groupMembersSelect = document.getElementById("groupMembersSelect");
	var selectedGroupMember = groupMembersSelect.options[groupMembersSelect.selectedIndex].value;
	
	if ((selectedGroupMember != null) && (selectedGroupMember.length > 0)) {
		var groupMembers = document.getElementById("groupMembers");
		var oldGroupMembers = groupMembers.value.split(",");
		var newGroupMembers = "";
		
		for (var i = 0; i < oldGroupMembers.length; i++) {
			if (oldGroupMembers[i] != selectedGroupMember) {
				newGroupMembers = newGroupMembers + oldGroupMembers[i] + ",";
			}
		}
		groupMembers.value = newGroupMembers;
		refreshGroupMembersSelect();
		removeButton.disabled = true;
	}
}

function refreshGroupMembersSelect() {
	var groupMembersSelect = document.getElementById("groupMembersSelect");
	var groupMembers = document.getElementById("groupMembers");
	var memberIds = groupMembers.value.split(",");
	
	groupMembersSelect.options.length = 0;
	
	for (var i = 0; i < memberIds.length; i++) {
		if ((memberIds[i] != null) && (memberIds[i].length > 0)) {
			var option = document.createElement('option');
			var userName = userAccounts[memberIds[i]];
			
			option.setAttribute('value', memberIds[i]);
			option.appendChild(document.createTextNode(userName + " (" + memberIds[i] + ")"));
			groupMembersSelect.appendChild(option);
		}
	}
}

function isExistingUser( userId ) {
	var groupMembers = document.getElementById("groupMembers");
	var memberIds = groupMembers.value.split(",");
	
	for (var i = 0; i < memberIds.length; i++) {
		if (memberIds[i] == userId) {
			return true;
		}
	}
	return false;
}

var processingUserChange = false;

function clearKnownUser() {
	if (!processingUserChange) {
		try {
			var knownUsersSelect = document.getElementById("groupEditKnownUsers");
			var newUserInput = document.getElementById("groupEditNewUser");
			var addButton = document.getElementById("addButton");
			
			processingUserChange = true;
			knownUsersSelect.options[0].selected = true;
			addButton.disabled = (newUserInput.value == "");
			
		} finally {
			processingUserChange = false;
		}
	}
}

function clearNewUser() {
	if (!processingUserChange) {
		try {
			var knownUsersSelect = document.getElementById("groupEditKnownUsers");
			var newUserInput = document.getElementById("groupEditNewUser");
			var addButton = document.getElementById("addButton");
			var selectedKnownUser = knownUsersSelect.options[knownUsersSelect.selectedIndex].value;
			
			processingUserChange = true;
			addButton.disabled = ((selectedKnownUser == null) || (selectedKnownUser == ""));
			
			if (newUserInput != null) {
				newUserInput.value = "";
			}
		} finally {
			processingUserChange = false;
		}
	}
}

function groupMemberSelectionChanged() {
	removeButton.disabled = (document.getElementById("groupMembersSelect").selectedIndex < 0);
}

</script>

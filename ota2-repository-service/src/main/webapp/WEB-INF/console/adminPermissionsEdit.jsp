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
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h3>
	Namespace:
	<span style="text-decoration:underline;">
		<c:if test="${baseNamespace == null}">Global Permissions</c:if>
		<c:if test="${baseNamespace != null}">${baseNamespace}</c:if>
	</span>
</h3>

<form action="${pageContext.request.contextPath}/console/adminPermissionsEdit.html" method="post">
<input name="namespace" type="hidden" value="${baseNamespace}" />
<input name="processForm" type="hidden" value="true" />

<table id="permissionsTable">
	<tr>
		<th>Group</th>
		<th>Grant Permission</th>
		<th>Deny Permission</th>
	</tr>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="nsPermission" varStatus="status" items="${permissions.permissions}">
		<tr class="${rowStyle}">
			<td>
				<c:choose>
					<c:when test="${nsPermission.principal=='anonymous'}">
						Anonymous Users
					</c:when>
					<c:otherwise>
						${nsPermission.principal}
					</c:otherwise>
				</c:choose>
				<input name="permissions[${status.index}].principal" type="hidden" value="${nsPermission.principal}" />
			</td>
			<td>
				<select name="permissions[${status.index}].grantPermission" size="1">
					<option value=""></option>
					<c:forEach var="permissionOption" items="${permissionOptions}">
						<c:choose>
							<c:when test="${nsPermission.grantPermission.toString() == permissionOption}">
								<option value="${permissionOption}" selected><spring:message code="${permissionOption}" /></option>
							</c:when>
							<c:otherwise>
								<option value="${permissionOption}"><spring:message code="${permissionOption}" /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</td>
			<td>
				<select name="permissions[${status.index}].denyPermission" size="1">
					<option value=""></option>
					<c:forEach var="permissionOption" items="${permissionOptions}">
						<c:choose>
							<c:when test="${nsPermission.denyPermission.toString() == permissionOption}">
								<option value="${permissionOption}" selected><spring:message code="${permissionOption}" /></option>
							</c:when>
							<c:otherwise>
								<option value="${permissionOption}"><spring:message code="${permissionOption}" /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</td>
		</tr>
		<c:choose>
			<c:when test="${rowStyle=='d0'}">
				<c:set var="rowStyle" value="d1" />
			</c:when>
			<c:otherwise>
				<c:set var="rowStyle" value="d0" />
			</c:otherwise>
		</c:choose>
	</c:forEach>
	<tr>
		<td colspan="3">
			<c:url var="cancelUrl" value="/console/adminPermissions.html">
				<c:param name="namespace" value="${baseNamespace}" />
			</c:url>
			<br/><input type="submit" value="Save Permissions" class="formButton" />
			&nbsp; <input type="button" value="Cancel" class="formButton" onclick="location.href='${cancelUrl}';" />
		</td>
	</tr>
</table>

</form>
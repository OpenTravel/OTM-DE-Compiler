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
<form action="${pageContext.request.contextPath}/console/adminPermissions.html" method="GET">
	Select a namespace from the list below to view the access control settings.<br/>
	<select name="namespace" size="1">
		<option value="">Global Permissions</option>
		<c:forEach var="ns" items="${baseNamespaces}">
			<c:if test="${ns == baseNamespace}">
				<option value="${ns}" selected>${ns}</option>
			</c:if>
			<c:if test="${ns != baseNamespace}">
				<option value="${ns}">${ns}</option>
			</c:if>
		</c:forEach>
	</select>
	&nbsp;
	<input type="submit" value="Show Permissions" class="formButtonSmall"/>
</form>
<div class="divider"></div>

<form>
	<h3>
		Namespace:
		<span style="text-decoration:underline;">
			<c:if test="${baseNamespace == null}">Global Permissions</c:if>
			<c:if test="${baseNamespace != null}">${baseNamespace}</c:if>
		</span>
		<c:url var="editUrl" value="/console/adminPermissionsEdit.html">
			<c:param name="namespace" value="${baseNamespace}" />
		</c:url>
		<c:url var="testUrl" value="/console/adminPermissionsTest.html">
			<c:param name="namespace" value="${baseNamespace}" />
		</c:url>
		&nbsp; <input type="button" value="Edit" class="formButtonSmall" onclick="location.href='${editUrl}';" />
		&nbsp; <input type="button" value="Test" class="formButtonSmall" onclick="location.href='${testUrl}';" />
	</h3>
	
	<table id="permissionsTable">
		<tr>
			<th>Group</th>
			<th>Grant Permission</th>
			<th>Deny Permission</th>
		</tr>
		<c:if test="${permissions.permissions.isEmpty()}">
			<tr class="d1"><td colspan="3">
				No permissions defined for this namespace.
			</td></tr>
		</c:if>
		<c:set var="rowStyle" value="d0" />
		<c:forEach var="nsPermission" items="${permissions.permissions}">
			<tr class="${rowStyle}">
				<td><c:choose>
					<c:when test="${nsPermission.principal=='anonymous'}">
						Anonymous Users
					</c:when>
					<c:otherwise>
						${nsPermission.principal}
					</c:otherwise>
				</c:choose></td>
				<td>
					<c:if test="${nsPermission.grantPermission != null}">
						<spring:message code="${nsPermission.grantPermission.toString()}" />
					</c:if>
				</td>
				<td>
					<c:if test="${nsPermission.denyPermission != null}">
						<spring:message code="${nsPermission.denyPermission.toString()}" />
					</c:if>
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
	</table>
</form>

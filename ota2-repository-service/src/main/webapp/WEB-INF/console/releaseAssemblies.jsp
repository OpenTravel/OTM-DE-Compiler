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
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="currentTab" value="ASSEMBLIES"/>
<%@include file="releaseTabs.jsp" %>

<div class="contentContainer" style="width:100%">
<table id="itemtable" style="width:100%;float:left;">
	<tr>
		<th width="65%">Provider APIs</th>
		<th width="35%">Version</th>
	</tr>
	<c:if test="${releaseAssemblies.isEmpty()}">
		<tr class="d0">
			<td colspan="2">This release is not associated with any service assemblies</td>
		</tr>
	</c:if>
	<c:set var="rowStyle" value="d0" />
	<c:forEach var="assembly" items="${releaseAssemblies}">
		<c:url var="assemblyUrl" value="/console/assemblyView.html">
			<c:param name="baseNamespace" value="${assembly.baseNamespace}" />
			<c:param name="filename" value="${assembly.filename}" />
			<c:param name="version" value="${assembly.version}" />
		</c:url>
		<tr class="${rowStyle}">
			<td>
				<img src="${pageContext.request.contextPath}/images/assembly.gif" />&nbsp;<a href="${assemblyUrl}">${assembly.assemblyName}</a>
				<br><small>${assembly.baseNamespace}</small>
			</td>
			<td>
				${assembly.version}
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
</div>

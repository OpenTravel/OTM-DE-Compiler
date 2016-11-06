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

<c:url var="libraryUrl" value="/console/libraryDictionary.html">
	<c:param name="baseNamespace" value="${library.repositoryItem.baseNamespace}" />
	<c:param name="filename" value="${library.repositoryItem.filename}" />
	<c:param name="version" value="${library.repositoryItem.version}" />
</c:url>
<c:url var="namespaceUrl" value="/console/browse.html">
	<c:param name="baseNamespace" value="${library.repositoryItem.baseNamespace}" />
</c:url>
<c:url var="dictionaryUrl" value="/console/entityDictionary.html">
	<c:param name="namespace" value="${entity.itemNamespace}" />
	<c:param name="localName" value="${entity.itemName}" />
</c:url>
<c:url var="usageUrl" value="/console/entityUsage.html">
	<c:param name="namespace" value="${entity.itemNamespace}" />
	<c:param name="localName" value="${entity.itemName}" />
</c:url>
<c:url var="validationUrl" value="/console/entityValidation.html">
	<c:param name="namespace" value="${entity.itemNamespace}" />
	<c:param name="localName" value="${entity.itemName}" />
</c:url>

<h2 style="padding-bottom: 0;"><spring:message code="${entity.entityType.simpleName}" />: ${entity.itemName} <small>(${library.repositoryItem.version})</small></h2>
<h3 style="padding-top: 0;">Library: <a href="${libraryUrl}">${library.itemName}</a> <small>(<a href="${namespaceUrl}">${entity.itemNamespace}</a>)</small></h3>

<ul class="tab">
	<c:set var="tabStyle" value="tablinks" />
	<c:if test="${currentTab == 'DICTIONARY'}"><c:set var="tabStyle" value="tablinks activeTab" /></c:if>
	<li><a href="${dictionaryUrl}" class="${tabStyle}">Dictionary</a></li>
	
	<c:set var="tabStyle" value="tablinks" />
	<c:if test="${currentTab == 'USAGE'}"><c:set var="tabStyle" value="tablinks activeTab" /></c:if>
	<li><a href="${usageUrl}" class="${tabStyle}">Where-Used</a></li>
	
	<c:set var="tabStyle" value="tablinks" />
	<c:if test="${currentTab == 'VALIDATION'}"><c:set var="tabStyle" value="tablinks activeTab" /></c:if>
	<li><a href="${validationUrl}" class="${tabStyle}">Errors &amp; Warnings</a></li>
</ul>
<p/><br/>


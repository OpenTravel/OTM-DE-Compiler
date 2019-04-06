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

<c:set var="currentTab" value="DICTIONARY"/>
<%@include file="entityTabs.jsp" %>

<%@include file="entityDetails_documentation.jsp" %>

<c:if test="${entity.entityType.simpleName == 'TLSimple'}">
	<%@include file="entityDetails_simple.jsp" %>
</c:if>

<c:if test="${(entity.entityType.simpleName == 'TLClosedEnumeration') || entity.entityType.simpleName == 'TLOpenEnumeration'}">
	<%@include file="entityDetails_enum.jsp" %>
</c:if>

<c:if test="${entity.entityType.simpleName == 'TLValueWithAttributes'}">
	<%@include file="entityDetails_vwa.jsp" %>
</c:if>

<c:if test="${entity.entityType.simpleName == 'TLCoreObject'}">
	<%@include file="entityDetails_core.jsp" %>
</c:if>

<c:if test="${entity.entityType.simpleName == 'TLChoiceObject'}">
	<%@include file="entityDetails_choice.jsp" %>
</c:if>

<c:if test="${entity.entityType.simpleName == 'TLBusinessObject'}">
	<%@include file="entityDetails_bo.jsp" %>
</c:if>

<c:if test="${entity.entityType.simpleName == 'TLOperation'}">
	<%@include file="entityDetails_operation.jsp" %>
</c:if>

<c:if test="${entity.entityType.simpleName == 'TLResource'}">
	<%@include file="entityDetails_resource.jsp" %>
</c:if>

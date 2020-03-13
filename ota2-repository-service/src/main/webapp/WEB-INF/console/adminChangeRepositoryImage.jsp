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
<form id="changeImageForm" action="${pageContext.request.contextPath}/console/adminChangeRepositoryImage.html" method="post" enctype="multipart/form-data">
<input id="saveChanges" name="saveChanges" type="hidden" value="false" />
<input id="cancelChanges" name="cancelChanges" type="hidden" value="false" />
<input name="tempLogoFile" type="hidden" value="${tempLogoFile}" />
<br/>
<table id="changeImageTable">
	<tr>
		<td width="1" style="white-space: nowrap;">
			<input id="defaultBannerRadio" type="radio" name="bannerType" value="DEFAULT" ${defaultBanner} onclick="updateSaveButtonStatus();" />
			Use Default Image Banner
		</td>
		<td> &nbsp; &nbsp; &nbsp; <img src="${pageContext.request.contextPath}/images/ota_logo.png" style="vertical-align:middle" width="256" height="64" /></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td width="1" style="white-space: nowrap;">
			<input id="customBannerRadio" type="radio" name="bannerType" value="CUSTOM" ${customBanner} onclick="updateSaveButtonStatus();" /> Use Custom Image Banner
			<br/>&nbsp;
			<br/>&nbsp;
		</td>
		<td> &nbsp; &nbsp; &nbsp;
			<c:choose>
				<c:when test="${useTempLogo}">
					<img src="${pageContext.request.contextPath}/service/tempLogo?file=${tempLogoFile}" alt="No custom banner defined" style="vertical-align:middle" width="256" height="64" />
				</c:when>
				<c:otherwise>
					<img src="${pageContext.request.contextPath}/service/customLogo" alt="No custom banner defined" style="vertical-align:middle" width="256" height="64" />
				</c:otherwise>
			</c:choose>
			<br/> &nbsp; &nbsp; &nbsp; <input type="file" name="bannerImageFile" onchange="selectCustomImage();" />
			<br/> &nbsp; &nbsp; &nbsp; <a href="#" onclick="clearCustomImage();"><small>Clear Custom Image</small></a>
		</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td>
			<input id="saveButton" type="button" value="Save Changes" class="formButton" onclick="submitForm('saveChanges');" />
			<input type="button" value="Cancel" class="formButton" onclick="submitForm('cancelChanges');" />
		</td>
		<td>&nbsp;</td>
	</tr>
</table>
</form>

<script type="text/javascript">

function updateSaveButtonStatus() {
	var form = document.forms["changeImageForm"];
	var customRadioField = form.elements["customBannerRadio"];
	var hiddenField = form.elements["tempLogoFile"];
	var saveButton = form.elements["saveButton"];
	var saveEnabled = true;
	
	if (customRadioField.checked && (hiddenField.value == "")) {
		saveEnabled = false;
	}
	saveButton.disabled = !saveEnabled;
}

function submitForm(hiddenField) {
	var form = document.forms["changeImageForm"];
	var hiddenField = form.elements[hiddenField];
	
	hiddenField.value = "true";
	form.submit();
}

function selectCustomImage() {
	var form = document.forms["changeImageForm"];
	var radioField = form.elements["customBannerRadio"];
	
	radioField.checked = true;
	form.submit();
}

function clearCustomImage() {
	var form = document.forms["changeImageForm"];
	var radioField = form.elements["defaultBannerRadio"];
	var hiddenField = form.elements["tempLogoFile"];
	
	radioField.checked = true;
	hiddenField.value = "";
	form.submit();
}

</script>
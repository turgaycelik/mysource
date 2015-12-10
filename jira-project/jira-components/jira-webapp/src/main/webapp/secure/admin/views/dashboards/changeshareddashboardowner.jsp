<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="decorator" content="navigator" />
    <title><ww:text name="'shareddashboards.admin.changeowner.title'"/></title>
</head>
<body>

    <page:applyDecorator id="change-owner" name="auiform">
        <ww:if test="hasErrorMessages == 'true'">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:iterator value="flushedErrorMessages">
                        <p><ww:property value="." /></p>
                    </ww:iterator>
                </aui:param>
            </aui:component>
        </ww:if>
        <page:param name="action"><ww:property value="./actionName"/>.jspa</page:param>
        <page:param name="id">change-owner-form-<ww:property value="dashboardId" /></page:param>
        <page:param name="submitButtonName">ChangeOwner</page:param>
        <page:param name="submitButtonText"><ww:text name="'shareddashboards.admin.cog.changeowner'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="'/secure/ViewSharedDashboards.jspa'" atltoken="false"/></page:param>
        <page:param name="returnUrl"><ww:property value="./returnUrl"/></page:param>
        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'shareddashboards.admin.changeowner.heading'"><ww:param name="'value0'"><ww:property value="dashboardName" /></ww:param></ww:text></aui:param>
            <aui:param name="'escape'" value="'false'" />
        </aui:component>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="id">owner-container</page:param>
            <aui:component label="text('shareddashboards.admin.dashboard.owner')" name="'owner'" id="'owner'" template="singleSelectUserPicker.jsp" theme="'aui'">
                <aui:param name="'userName'" value="/owner"/>
                <aui:param name="'userFullName'" value="ownerUserObj/displayName"/>
                <aui:param name="'userAvatar'" value="ownerUserAvatarUrl"/>
                <aui:param name="'inputText'" value="/ownerError" />
                <aui:param name="'mandatory'" value="'true'" />
                <aui:param name="'disabled'" value="userPickerDisabled" />
                <aui:param name="'description'"><ww:text name="'user.picker.ajax.desc'"/></aui:param>
            </aui:component>
        </page:applyDecorator>
        <aui:component name="'searchName'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'searchOwnerUserName'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'sortColumn'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'sortAscending'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'pagingOffset'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'totalResultCount'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'dashboardId'" template="hidden.jsp" theme="'aui'" />

    </page:applyDecorator>

</body>
</html>

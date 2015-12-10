<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.editprofile.edit.profile'"/>: <ww:property value="editedUser/displayName" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <page:applyDecorator id="user-edit" name="auiform">
        <page:param name="action">EditUser.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>
        <page:param name="cancelLinkURI"><ww:url value="'ViewUser.jspa'"><ww:param name="'name'" value="editedUser/name" /></ww:url></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.editprofile.edit.profile'"/>: <ww:property value="editedUser/displayName" escape="false" /></aui:param>
        </aui:component>

        <ww:if test="/showProjectsUserLeadsError == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'" value="/projectsUserLeadsErrorMessage()" />
            </aui:component>
        </ww:if>

        <ww:if test="/showComponentsUserLeadsError == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'" value="/componentsUserLeadsErrorMessage()" />
            </aui:component>
        </ww:if>

        <ww:if test="/showRenameUser == true">
            <ww:if test="/inMultipleDirectories == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'"><ww:text name="'admin.warnings.rename.user.exists.in.multiple'"/></aui:param>
                </aui:component>
            </ww:if>

            <page:applyDecorator name="auifieldgroup">
                <aui:textfield label="text('common.words.username')" mandatory="true" maxlength="255" id="'username'" name="'username'" theme="'aui'" />
            </page:applyDecorator>
        </ww:if>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.fullname')" mandatory="true" maxlength="255" id="'fullName'" name="'fullName'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.email')" mandatory="true" maxlength="255" id="'email'" name="'email'" theme="'aui'" />
        </page:applyDecorator>

        <ww:if test="/showActiveCheckbox == true">
            <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">checkbox</page:param>
                    <aui:checkbox label="text('admin.common.words.active')"  id="'active'" name="'active'" fieldValue="'true'" theme="'aui'" />
                </page:applyDecorator>
            </page:applyDecorator>
        </ww:if>

        <aui:component name="'editName'" template="hidden.jsp"  theme="'aui'" />

    </page:applyDecorator>
</body>
</html>

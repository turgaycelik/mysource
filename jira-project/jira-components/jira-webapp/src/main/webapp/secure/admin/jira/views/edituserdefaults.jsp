<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.userdefaults.edit.user.defaults'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="user_defaults"/>
</head>
<body>
    <page:applyDecorator id="edit_user_defaults" name="auiform">
        <page:param name="action">EditUserDefaultSettings.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>
        <page:param name="cancelLinkURI"><ww:url value="'ViewUserDefaultSettings.jspa'" /></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.userdefaults.user.default.settings'"/></aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('admin.userdefaults.labels.email.format')" name="'preference'" size="'medium'" list="emailFormats" listKey="'id'" listValue="'name'" value="selectedEmailFormat" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.userdefaults.number.of.issues'" /></page:param>
            <aui:textfield label="text('admin.userdefaults.labels.issues.per.page')" name="'numIssues'" size="'medium'" value="applicationProperties/defaultBackedString('user.issues.per.page')" theme="'aui'">
            </aui:textfield>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.userdefaults.labels.share.filter.dashboards'" /></page:param>
            <ui:select label="'Default acccess'" name="'sharePublic'" size="'medium'" list="shareList" listKey="'id'" listValue="'name'" value="shareValue" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">checkbox</page:param>
                <aui:checkbox label="text('admin.userdefaults.notify.users.of.own.changes')" id="'emailUser'" name="'emailUser'" fieldValue="'true'" theme="'aui'" />
            </page:applyDecorator>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">checkbox</page:param>
                <aui:checkbox label="text('admin.userdefaults.labels.autowatch')" id="'autoWatch'" name="'autoWatch'" fieldValue="'true'" theme="'aui'" />
            </page:applyDecorator>
        </page:applyDecorator>

    </page:applyDecorator>
</body>
</html>

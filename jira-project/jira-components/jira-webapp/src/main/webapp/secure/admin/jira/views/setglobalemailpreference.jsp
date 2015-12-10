<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.userdefaults.user.default.email.pref'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="user_defaults"/>
</head>
<body>
<page:applyDecorator name="jiraform">
	<page:param name="action">SetGlobalEmailPreference.jspa</page:param>
	<page:param name="submitId">set_email</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI">ViewUserDefaultSettings.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.userdefaults.user.default.email.pref'"/></page:param>
	<page:param name="width">100%</page:param>
    <page:param name="description">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.userdefaults.email.change.confirmation'">
                        <ww:param name="'value0'">'<ww:property value="/otherMimeType"/>'</ww:param>
                        <ww:param name="'value1'">'<ww:property value="applicationProperties/defaultBackedString('user.notifications.mimetype')"/>'</ww:param>
                        <ww:param name="'value2'"><ww:property value="/effectedUsers"/></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </page:param>
</page:applyDecorator>
</body>
</html>

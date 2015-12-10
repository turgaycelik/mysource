<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.resetfailedlogin.title'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="title"><ww:text name="'admin.resetfailedlogin.title'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="action">ResetFailedLoginCount.jspa</page:param>
        <page:param name="submitId">reset_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.common.words.reset'"/></page:param>
        <page:param name="cancelURI">UserBrowser.jspa</page:param>

        <ui:textfield label="text('common.words.username')" name="'name'" size="40"/>
    </page:applyDecorator>
</body>
</html>

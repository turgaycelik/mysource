
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.roles.edit.role'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="project_role_browser"/>
</head>

<body>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditProjectRole.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI">ViewProjectRoles.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.roles.edit.role'"/>: <ww:property value="name" /></page:param>
        <page:param name="helpURL">project_roles</page:param>
        <page:param name="helpURLFragment">#Editing+a+project+role</page:param>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />

        <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

    	<ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
    </page:applyDecorator>
</body>
</html>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.schemes.permissions.add.permission.schemes'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="permission_schemes"/>
</head>
<body>
    <p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddPermissionScheme.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    	<page:param name="cancelURI">ViewPermissionSchemes.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.schemes.permissions.add.permission.schemes'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">permissionsHelp</page:param>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />
        <ui:textarea label="text('common.words.description')" name="'description'" cols="'30'" rows="'3'" />
    </page:applyDecorator>
    </p>
</body>
</html>


<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.issuesettings.resolutions.edit.resolution'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/issue_attributes"/>
    <meta name="admin.active.tab" content="resolutions"/>
</head>

<body>
    <p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">EditResolution.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI">ViewResolutions.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.issuesettings.resolutions.edit.resolution'"/>: <ww:property value="constant/string('name')" /></page:param>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />

        <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

    	<ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
    </page:applyDecorator>
    </p>

</body>
</html>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigurations.copy.field.configuration'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="field_configuration"/>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">CopyFieldLayout.jspa</page:param>
    <page:param name="cancelURI"><ww:url page="ViewFieldLayouts.jspa" /></page:param>
    <page:param name="submitId">copy_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.copy'"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigurations.copy.field.configuration'"/>: <ww:property value="/fieldLayout/name" /></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
    <p>
        <ww:text name="'admin.issuefields.fieldconfigurations.copy.field.instructions'"/>
    </p>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'fieldLayoutName'" size="'30'">
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'fieldLayoutDescription'" size="'60'" />

    <ui:component name="'id'" template="hidden.jsp" theme="'single'" />

</page:applyDecorator>
</body>
</html>

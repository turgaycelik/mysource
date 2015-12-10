<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigurations.edit.field.configuration'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="field_configuration"/>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditFieldLayout.jspa</page:param>
    <page:param name="cancelURI"><ww:url page="ViewFieldLayouts.jspa" /></page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigurations.edit.field.configuration'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
    <p>
        <ww:text name="'admin.issuefields.fieldconfigurations.edit.page.description'">
            <ww:param name="'value0'"><ww:property value="/fieldLayout/name" /></ww:param>
        </ww:text>
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

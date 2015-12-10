<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigurations.delete.field.layout.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="field_configuration"/>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">DeleteFieldLayout.jspa</page:param>
    <page:param name="submitId">delete_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="cancelURI"><ww:url page="ViewFieldLayouts.jspa" /></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigurations.delete.field.layout.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <p>
            <ww:text name="'admin.issuefields.fieldconfigurations.delete.confirmation'">
                <ww:param name="'value0'"><b><ww:property value="/fieldLayout/name" /></b></ww:param>
            </ww:text>
        </p>
    </page:param>

    <ui:component name="'id'" template="hidden.jsp" theme="'single'" />
    <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'" />

</page:applyDecorator>
</body>
</html>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screen_scheme"/>
	<title><ww:text name="'admin.issuefields.screenschemes.edit.screen.scheme'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditFieldScreenScheme.jspa</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.update'"/></page:param>
    <page:param name="cancelURI"><ww:url page="ViewFieldScreenSchemes.jspa" /></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.screenschemes.edit.screen.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <p>
            <ww:text name="'admin.issuefields.screenschemes.use.the.form.below'">
                <ww:param name="'value0'"><b><ww:property value="/fieldScreenScheme/name" /></b></ww:param>
            </ww:text>
        </p>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'fieldScreenSchemeName'" size="'30'">
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'fieldScreenSchemeDescription'" size="'60'" />

    <ui:component name="'id'" template="hidden.jsp" theme="'single'" />
</page:applyDecorator>
</body>
</html>

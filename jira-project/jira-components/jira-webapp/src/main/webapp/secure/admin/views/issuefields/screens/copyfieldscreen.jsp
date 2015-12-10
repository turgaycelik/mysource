<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screens"/>
	<title><ww:text name="'admin.issuefields.screens.copy.screen'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">CopyFieldScreen.jspa</page:param>
    <page:param name="submitId">copy_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.copy'"/></page:param>
    <page:param name="cancelURI"><ww:url page="ViewFieldScreens.jspa" /></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.screens.copy.screen'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <p>
            <ww:text name="'admin.issuefields.screens.copy.instructions'">
                <ww:param name="'value0'"><b><ww:property value="/fieldScreen/name" /></b></ww:param>
            </ww:text>
        </p>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'fieldScreenName'" size="'30'">
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'fieldScreenDescription'" size="'60'" />

    <ui:component name="'id'" template="hidden.jsp" theme="'single'" />

    <ui:component name="'edited'" value="'true'" template="hidden.jsp" theme="'single'" />
</page:applyDecorator>
</body>
</html>

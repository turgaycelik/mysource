<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.screenschemes.add.screen.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screen_scheme"/>
</head>
<body>
<page:applyDecorator id="field-screen-scheme-add" name="auiform">
    <page:param name="action">AddFieldScreenScheme.jspa</page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="cancelLinkURI">ViewFieldScreenSchemes.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'">
            <ww:text name="'admin.issuefields.screenschemes.add.screen.scheme'"/>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.name')" name="'fieldScreenSchemeName'" mandatory="true" theme="'aui'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.description')" name="'fieldScreenSchemeDescription'" theme="'aui'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('admin.issuefields.screenschemes.default.screen')" name="'fieldScreenId'" list="/fieldScreens" listKey="'./id'" listValue="'./name'" theme="'aui'" />
        <page:param name="description"><ww:text name="'admin.issuefields.screenschemes.default.screen.description'"/></page:param>
    </page:applyDecorator>

</page:applyDecorator>
</body>
</html>

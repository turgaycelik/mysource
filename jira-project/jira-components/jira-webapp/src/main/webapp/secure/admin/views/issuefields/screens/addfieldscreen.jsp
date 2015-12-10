<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.screens.add.screen'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screens"/>
</head>
<body>
<page:applyDecorator id="field-screen-add" name="auiform">
    <page:param name="action">AddFieldScreen.jspa</page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="cancelLinkURI">ViewFieldScreens.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'">
            <ww:text name="'admin.issuefields.screens.add.screen'"/>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.name')" name="'fieldScreenName'" mandatory="true" theme="'aui'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.description')" name="'fieldScreenDescription'" theme="'aui'" />
    </page:applyDecorator>

</page:applyDecorator>
</body>
</html>

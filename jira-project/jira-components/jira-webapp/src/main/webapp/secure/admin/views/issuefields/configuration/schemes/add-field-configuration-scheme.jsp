<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.issuefields.fieldconfigschemes.add.field.configuration.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>

<page:applyDecorator id="add-field-configuration-scheme" name="auiform">
    <page:param name="action">AddFieldConfigurationScheme.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="cancelLinkURI">ViewFieldLayoutSchemes.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.issuefields.fieldconfigschemes.add.field.configuration.scheme'"/></aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.name')" id="'field-layout-scheme-name'" mandatory="true"
                       name="'fieldLayoutSchemeName'" theme="'aui'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.description')" id="'field-layout-scheme-description'"
                       name="'fieldLayoutSchemeDescription'" theme="'aui'" />
    </page:applyDecorator>
</page:applyDecorator>
</body>
</html>
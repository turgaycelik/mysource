<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.issuefields.fieldconfigschemes.add.issue.type'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>
<page:applyDecorator id="add-issue-type-field-configuration-association-form" name="auiform">
    <page:param name="action">AddIssueTypeToFieldConfigurationAssociation.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="cancelLinkURI">ConfigureFieldLayoutScheme!default.jspa?id=<ww:property value="./id"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.issuefields.fieldconfigschemes.add.issue.type'"/></aui:param>
    </aui:component>

    <aui:component name="'id'" template="hidden.jsp" theme="'aui'"/>
    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('common.concepts.issuetype')" id="'issue-type-id'" name="'issueTypeId'"
                    list="/addableIssueTypes" listKey="'./genericValue/string('id')'" listValue="'./nameTranslation'"
                    theme="'aui'">
        </aui:select>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('admin.issuefields.fieldconfigschemes.field.configuration')" id="'field-configuration-id'"
                    name="'fieldConfigurationId'" list="/fieldLayouts" listKey="'/fieldLayoutId(.)'" listValue="'./name'"
                    theme="'aui'">
        </aui:select>
    </page:applyDecorator>
</page:applyDecorator>
</body>
</html>

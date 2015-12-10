<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.itss.configure.add.issue.type.to.screen.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="issue_type_screen_scheme"/>
</head>
<body>

<page:applyDecorator id="add-issue-type-screen-scheme-association-form" name="auiform">
    <page:param name="action">AddIssueTypeScreenScreenSchemeAssociation.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="cancelLinkURI">ConfigureIssueTypeScreenScheme.jspa?id=<ww:property value="./id"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.itss.configure.add.issue.type.to.screen.scheme'"/></aui:param>
    </aui:component>

    <aui:component name="'id'" template="hidden.jsp" theme="'aui'"/>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('common.concepts.issuetype')" id="'issue-type-id'" name="'issueTypeId'"
                    list="/addableIssueTypes" listKey="'./genericValue/string('id')'" listValue="'./nameTranslation'"
                    theme="'aui'">
        </aui:select>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('admin.menu.issuefields.screen.scheme')" id="'field-screen-scheme-id'"
                    name="'fieldScreenSchemeId'" list="/fieldScreenSchemes" listKey="'./id'" listValue="'./name'"
                    theme="'aui'">
        </aui:select>
    </page:applyDecorator>
</page:applyDecorator>
</body>
</html>
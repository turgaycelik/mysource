<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="aui" uri="webwork" %>
<html>
<head>
    <title><ww:text name="'admin.issuefields.issuetypescreenschemes.add'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="issue_type_screen_scheme"/>
</head>
<body>

<page:applyDecorator id="add-issue-type-screen-scheme-form" name="auiform">
    <page:param name="action">AddIssueTypeScreenScheme.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="cancelLinkURI">ViewIssueTypeScreenSchemes.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.issuefields.issuetypescreenschemes.add'"/></aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.name')" id="'issue-type-screen-scheme-name'" mandatory="true"
                       name="'schemeName'" theme="'aui'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.description')" id="'issue-type-screen-scheme-description'"
                       name="'schemeDescription'" theme="'aui'"/>

    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('admin.projects.screen.scheme')" id="'field-screen-scheme-id'"
                    name="'fieldScreenSchemeId'" list="/fieldScreenSchemes" listKey="'./id'" listValue="'./name'"
                    theme="'aui'">
        </aui:select>
        <page:param name="description"><ww:text name="'admin.issuefields.issuetypescreenschemes.screen.scheme.description'"/></page:param>
    </page:applyDecorator>
</page:applyDecorator>
</body>
</html>
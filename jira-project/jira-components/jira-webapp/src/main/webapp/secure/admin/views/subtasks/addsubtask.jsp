<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.subtasks.add.new.issue.type'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="subtasks"/>
</head>
<body>

<page:applyDecorator id="add-subtask-issue-type-form" name="auiform">
    <page:param name="action">AddSubTaskIssueType.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
    <page:param name="submitButtonName">Add</page:param>
    <page:param name="cancelLinkURI">ManageSubTasks.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.subtasks.add.new.issue.type'"/></aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.name')" name="'name'" mandatory="true" theme="'aui'"/>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textarea label="text('common.words.description')" name="'description'" rows="'4'" theme="'aui'"/>
    </page:applyDecorator>
</page:applyDecorator>
</body>
</html>
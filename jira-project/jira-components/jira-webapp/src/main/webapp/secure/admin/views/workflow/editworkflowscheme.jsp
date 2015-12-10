<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
    <jira:web-resource-require modules="com.atlassian.jira.jira-project-config-plugin:edit-workflow-scheme"/>
    <title><ww:text name="'admin.schemes.workflow.edit.title'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
</head>
<body>
    <div id="workflowscheme-editor" class="workflowscheme-editor-loading"></div>
</body>
</html>

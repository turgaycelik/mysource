<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.subtasks.edit.subtask.issue.type'"/></title>
</head>
<body>
    <p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">ManageSubTasks.jspa</page:param>
        <page:param name="submitId">ok_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.common.words.ok'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.subtasks.are.disabled'"/></page:param>
        <page:param name="description"><ww:text name="'admin.subtasks.disabled.notice'"/></page:param>
    </page:applyDecorator>
    </p>
</body>
</html>

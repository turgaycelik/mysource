<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'movesubtask.title'"/>: <ww:property value="issue/string('key')" /></title>
</head>
<body>
    <page:applyDecorator name="bulkops-general">
        <page:param name="pageTitle"><ww:text name="'moveissue.title'"/></page:param>
        <page:param name="navContentJsp">/secure/views/issue/movetaskpane.jsp</page:param>

            <page:applyDecorator name="jiraform">
                <page:param name="title"><ww:text name="'movesubtask.title'"/>: <ww:property value="issue/string('key')" /></page:param>
                <page:param name="description">
                    <ww:text name="'move.subtask.step4.desc'"/>
                </page:param>
                <page:param name="width">100%</page:param>
                <page:param name="action"><ww:url page="MoveSubTaskParent.jspa"><ww:param name="'id'" value="id" /></ww:url></page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <page:param name="cancelURI"><ww:url value="/issuePath" /></page:param>
                <page:param name="submitId">reparent_submit</page:param>
                <page:param name="submitName"><ww:text name="'move.subtask.parent.submit'"/></page:param>

                <ui:component label="'Parent Issue'" name="'parentIssue'" template="issuepicker.jsp">
                    <ui:param name="'size'" value="18"/>
                    <ui:param name="'singleSelectOnly'" value="'true'"/>
                    <ui:param name="'showSubTasks'" value="'false'"/>
                    <ui:param name="'showSubTasksParent'" value="'false'"/>
                    <ui:param name="'selectedProjectId'" value="currentPid"/>
                    <ui:param name="'currentIssue'" value="issue/string('key')" />
                    <ui:param name="'currentJQL'" value="/currentJQL" />
                    <ui:param name="'mandatory'" value="'true'"/>
                </ui:component>

                <ui:component name="'id'" template="hidden.jsp" />
            </page:applyDecorator>

    </page:applyDecorator>
</body>
</html>

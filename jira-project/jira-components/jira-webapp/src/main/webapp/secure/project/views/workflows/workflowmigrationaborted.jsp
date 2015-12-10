<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
    <title><ww:text name="'admin.workflowmigration.aborted.title'" /></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="method">get</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="title"><ww:text name="'admin.workflowmigration.aborted.title'" /></page:param>
    <page:param name="instructions">
        <page:param name="action">AcknowledgeTask.jspa</page:param>
        <ww:if test="/currentTask/userWhoStartedTask == true">
            <page:param name="submitId">acknowledge_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.words.acknowledge'"/></page:param>
            <ui:component name="'taskId'" template="hidden.jsp"/>
        </ww:if>
        <ww:else>
            <page:param name="submitId">done_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.words.done'"/></page:param>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'common.tasks.cant.acknowledge.task.you.didnt.start'">
                            <ww:param name="'value0'"><jira:formatuser userName="/currentTask/user/name" type="'profileLink'" id="'user_profile'"/></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:else>
        <ui:component name="'destinationURL'" value="destinationURL" template="hidden.jsp"/>
    </page:param>

    <tr>
        <td>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:if test="/scheme != null">
                        <ww:text name="'admin.workflowmigration.aborted.scheme'">
                            <ww:param name="'value0'"><ww:property value="/scheme/string('name')" /></ww:param>
                            <ww:param name="'value1'"><ww:property value="/project/string('name')" /></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="/abortedMigrationMessageKey">
                                <ww:param name="'value1'"><ww:property value="/project/string('name')" /></ww:param>
                        </ww:text>
                    </ww:else>

                    <ol>
                    <ww:iterator value="/failedIssueIds()">
                        <li>
                            <ww:text name="'admin.workflowmigration.aborted.issues'">
                                <ww:param name="'value0'"><ww:property value="./key()" /></ww:param>
                                <ww:param name="'value1'"><a href="<%= request.getContextPath() %>/browse/<ww:property value="./value()" />"><ww:property value="./value()" /></a></ww:param>
                            </ww:text>
                        </li>
                    </ww:iterator>
                    </ol>

                    <ww:text name="'admin.workflowmigration.aborted.stopped'" />

                    <p>
                        <ww:text name="'admin.workflowmigration.aborted.restorebackup'" />
                    </p>
                </aui:param>
            </aui:component>
        </td>
    </tr>
</page:applyDecorator>
</body>
</html>

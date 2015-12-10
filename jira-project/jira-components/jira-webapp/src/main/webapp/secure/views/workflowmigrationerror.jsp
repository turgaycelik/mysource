<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
    <title><ww:text name="'admin.workflowmigration.error.title'" /></title>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="method">get</page:param>
    <page:param name="title"><ww:text name="'admin.workflowmigration.error.title'" /></page:param>
    <page:param name="description">
        <p>
            <ww:text name="'admin.workflowmigration.error.issue.errors'" /><br>
            <ww:text name="'admin.workflowmigration.error.correct'" /><br>
            <ww:text name="'admin.workflowmigration.error.no.db.changes'" />
        </p>
        <p>
            <ww:text name="'admin.workflowmigration.errors.contact.admin'">
                <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
            </ww:text>
            <ww:text name="'admin.workflowmigration.error.integrity.checker'" />
        </p>
    </page:param>
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
</page:applyDecorator>

</body>
</html>

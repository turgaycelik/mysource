<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean name="'com.atlassian.jira.util.JiraDateUtils'" id="dateUtils" />
<html>
<head>
    <title><ww:text name="'admin.projects.reindex.project'"/>: <ww:property value="project/string('name')" /></title>
    <ww:if test="/ourTask/finished == false">
        <meta http-equiv="refresh" content="5">
    </ww:if>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>
<body>
<div id="project-reindex-form">
<page:applyDecorator name="jiraform">
    <page:param name="columns">1</page:param>
    <page:param name="action">IndexProject.jspa</page:param>
    <page:param name="method">get</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="submitId">refresh_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.common.words.refresh'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.indexing.reindexing'"/></page:param>
    <page:param name="instructions">
        <ww:if test="/ourTask/finished == true && /ourTask/userWhoStartedTask == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'common.tasks.cant.acknowledge.task.you.didnt.start'">
                            <ww:param name="'value0'"><a href="<ww:property value="/ourTask/userURL"/>"><ww:property value="/ourTask/user/name"/></a></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
    </page:param>
    <tr bgcolor="#ffffff"><td>
        <ui:component template="taskdescriptor.jsp" name="'/ourTask'"/>
        <ww:if test="/ourTask/finished == true">
            <page:param name="action">AcknowledgeTask.jspa</page:param>
            <ww:if test="/ourTask/userWhoStartedTask == true">
                <page:param name="submitId">acknowledge_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.words.acknowledge'"/></page:param>
                <ui:component name="'taskId'" template="hidden.jsp"/>
            </ww:if>
            <ww:else>
                <page:param name="submitId">done_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.words.done'"/></page:param>
            </ww:else>
            <ui:component name="'destinationURL'" template="hidden.jsp"/>
        </ww:if>
        <ww:else>
            <page:param name="action">IndexProjectProgress.jspa</page:param>
            <page:param name="submitId">refresh_submit</page:param>
            <page:param name="submitName"><ww:text name="'admin.common.words.refresh'"/></page:param>
            <ui:component name="'taskId'" template="hidden.jsp"/>
            <ui:component name="'pid'" template="hidden.jsp"/>
            <ww:if test="/ourTask/cancellable == true">
                <page:param name="buttons">
                    <input class="aui-button" type="button" id="cancel_reindex_submit" name ="<ww:text name="'admin.indexing.cancel'"/>" value="<ww:text name="'admin.indexing.cancel'"/>"
                           onclick="location.href='IndexProjectCancel.jspa?taskId=<ww:property value="taskId" />&pid=<ww:property value="pid"/>'"/>
                </page:param>
            </ww:if>
        </ww:else>
        <ww:if test="/ourTask/cancelled == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p id="project-index-message">
                        <ww:if test="/ourTask/finished == true">
                            <ww:text name="'admin.indexing.cancelled'"/><br/>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'admin.indexing.cancelling'"/><br/>
                        </ww:else>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>

    </td></tr>
</page:applyDecorator>
</div>
</body>
</html>

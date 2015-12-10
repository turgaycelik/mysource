<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
    <ww:if test="/currentTask/finished == false">
        <meta http-equiv="refresh" content="5">
    </ww:if>
    <title><ww:if test="/draftMigration == true"><ww:text name="'admin.selectworkflowscheme.migration.publish.draft'"/></ww:if><ww:else><ww:text name="'admin.selectworkflowscheme.select.workflow.scheme'"/></ww:else></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="method">get</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="title">
        <ww:if test="/draftMigration == true">
            <ww:text name="'admin.selectworkflowscheme.migration.publish.draft'"/>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.selectworkflowscheme.associate.scheme.to.project'"/>: <ww:property value="project/string('name')" />
        </ww:else>
    </page:param>
    <page:param name="instructions">
        <ww:if test="/currentTask/finished == true && /currentTask/userWhoStartedTask == false">
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
        </ww:if>
    </page:param>

    <tr>
        <td>
            <ui:component template="taskdescriptor.jsp" name="'/currentTask'"/>
            <ww:if test="/done == true">

                <page:param name="description">
                    <p>
                        <ww:if test="/draftMigration == true">
                            <ww:text name="'admin.selectworkflowscheme.step2.draft.done'">
                                <ww:param name="'value0'"><b></ww:param>
                                <ww:param name="'value1'"></b></ww:param>
                            </ww:text>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'admin.selectworkflowscheme.step3.done'">
                                <ww:param name="'value0'"><b></ww:param>
                                <ww:param name="'value1'"></b></ww:param>
                            </ww:text>
                        </ww:else>
                    </p>
                </page:param>
                <page:param name="action">AcknowledgeTask.jspa</page:param>
                <ww:if test="/currentTask/userWhoStartedTask == true">
                    <page:param name="submitName"><ww:text name="'common.words.acknowledge'"/></page:param>
                    <ui:component name="'taskId'" template="hidden.jsp"/>
                    <ww:if test="/draftMigration == true">
                        <page:param name="submitId">draft_acknowledge_submit</page:param>
                    </ww:if>
                    <ww:else>
                        <page:param name="submitId">acknowledge_submit</page:param>
                    </ww:else>
                </ww:if>
                <ww:else>
                    <page:param name="submitId">done_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.words.done'"/></page:param>
                </ww:else>
                <ui:component name="'destinationURL'" template="hidden.jsp"/>
            </ww:if>
            <ww:else>
                <ww:if test="/currentTask == null">
                    <page:param name="title">
                        <ww:text name="'common.tasks.task.not.found.title'"/>
                    </page:param>
                    <page:param name="action">ViewProjects.jspa</page:param>
                    <page:param name="submitId">done_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.words.done'"/></page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                </ww:if>
                <ww:else>
                    <page:param name="description">
                        <p>
                            <ww:if test="/draftMigration == true">
                                <ww:text name="'admin.selectworkflowscheme.step2.draft.inprogress'">
                                    <ww:param name="'value0'"><b></ww:param>
                                    <ww:param name="'value1'"></b></ww:param>
                                </ww:text>
                            </ww:if>
                            <ww:else>
                                <ww:text name="'admin.selectworkflowscheme.step3.inprogress'">
                                    <ww:param name="'value0'"><b></ww:param>
                                    <ww:param name="'value1'"></b></ww:param>
                                </ww:text>
                            </ww:else>
                        </p>
                    </page:param>

                    <page:param name="action">SelectProjectWorkflowSchemeStep3.jspa</page:param>
                    <page:param name="submitId">refresh_submit</page:param>
                    <page:param name="submitName"><ww:text name="'admin.common.words.refresh'"/></page:param>
                    <page:param name="autoSelectFirst">false</page:param>

                    <ww:if test="projectId != null">
                        <ui:component name="'projectId'" template="hidden.jsp"/>
                    </ww:if>
                    <ui:component name="'taskId'" template="hidden.jsp"/>
                    <ui:component name="'schemeId'" template="hidden.jsp"/>
                    <ui:component name="'draftMigration'" template="hidden.jsp"/>
                    <ui:component name="'projectIdsParameter'" template="hidden.jsp"/>
                </ww:else>
            </ww:else>
        </td>
    </tr>

</page:applyDecorator>
</body>
</html>

<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:if test="workflow/editable == true"><ww:text name="'admin.workflows.edit'"/></ww:if><ww:else><ww:text name="'viewworkflow.view.title'"/></ww:else> &mdash; <ww:property value="/workflowDisplayName" /></title>
    <%
        WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
        webResourceManager.requireResourcesForContext("jira.workflow.view");
    %>
</head>
<body class="page-type-workflow-view">
<ww:property value="/headerHtml" escape="false"/>
<div class="workflow-container">
    <ww:property value="/linksHtml" escape="false"/>
    <ww:if test="workflow/editable != true">
    <div class="workflow-view<ww:if test="/diagramMode == false"> hidden</ww:if>" id="workflow-view-diagram">
        <div id="workflow-diagram-container">
            <div class="workflow-diagram">
            </div>
        </div>
    </div>
    </ww:if>
    <div class="workflow-view<ww:if test="/diagramMode == true && workflow/editable == false"> hidden</ww:if>" id="workflow-view-text">
        <table id="steps_table" class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th>
                        <ww:text name="'admin.workflows.step.name.id'">
                            <ww:param name="'value0'"><span></ww:param>
                            <ww:param name="'value1'"></span></ww:param>
                            <ww:param name="'value2'"><span></ww:param>
                            <ww:param name="'value3'"></span></ww:param>
                        </ww:text>
                    </th>
                    <th>
                        <ww:text name="'admin.workflows.linked.status'"/>
                    </th>
                    <th>
                        <ww:text name="'admin.workflows.transitions'">
                            <ww:param name="'value0'"><span></ww:param>
                            <ww:param name="'value1'"></span></ww:param>
                        </ww:text>
                    </th>
                    <th>
                        <ww:text name="'common.words.operations'"/>
                    </th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value="workflow/descriptor/steps" status="'status'">
                <tr>
                    <td>
                        <a href="<ww:url page="ViewWorkflowStep.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="./id" /></ww:url>"
                                id="step_link_<ww:property value="id"/>"><ww:property value="name"/></a>
                        <span class="smallgrey">(<ww:property value="id" />)</span>
                    </td>
                    <td>
                        <ww:if test="metaAttributes/('jira.status.id')">
                        <ww:property value="metaAttributes/('jira.status.id')">
                            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                <ww:param name="'issueStatus'" value="/status(.)"/>
                                <ww:param name="'isSubtle'" value="false"/>
                                <ww:param name="'isCompact'" value="false"/>
                            </ww:component>
                        </ww:property>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'admin.workflowtransition.no.linked.status'"/>
                        </ww:else>
                    </td>
                    <td>
                        <ww:iterator value="actions">
                            <ww:if test="./common == true"><em></ww:if>
                            <a href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="../id" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                               <ww:if test="./metaAttributes/('jira.description')">title="<ww:property value="./metaAttributes/('jira.description')" />"</ww:if>
                                    id="edit_action_<ww:property value="../id" />_<ww:property value="id" />"><ww:property value="name" /></a>
                            <ww:if test="./common == true"></em></ww:if>
                            <span class="smallgrey">(<ww:property value="id"/>)</span>
                            <br>
                            <span class="smallgrey">&gt;&gt;
                                <ww:if test="/transitionWithoutStepChange(.) == true">
                                    <ww:property value="../metaAttributes/('jira.status.id')">
                                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                            <ww:param name="'issueStatus'" value="/status(.)"/>
                                            <ww:param name="'isSubtle'" value="true"/>
                                            <ww:param name="'isCompact'" value="false"/>
                                        </ww:component>
                                    </ww:property>
                                </ww:if>
                                <ww:else>
                                    <ww:property value="/workflow/descriptor/step(unconditionalResult/step)/metaAttributes/('jira.status.id')">
                                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                            <ww:param name="'issueStatus'" value="/status(.)"/>
                                            <ww:param name="'isSubtle'" value="true"/>
                                            <ww:param name="'isCompact'" value="false"/>
                                        </ww:component>
                                    </ww:property>
                                </ww:else>
                            </span><br>
                        </ww:iterator>

                        <%-- Global Actions --%>
                        <ww:iterator value="workflow/descriptor/globalActions" status="'status'">
                            <em><a href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                   <ww:if test="./metaAttributes/('jira.description')">title="<ww:property value="./metaAttributes/('jira.description')" />"</ww:if>><ww:property value="name" /></a></em> <span class="smallgrey">(<ww:property value="id"/>)</span>
                            <br>
                            <span class="smallgrey">&gt;&gt; <ww:property value="/workflow/descriptor/step(unconditionalResult/step)/name" /></span><br>
                        </ww:iterator>
                    </td>
                    <td>
                        <ul class="operations-list">
                        <ww:if test="workflow/editable == true">
                            <ww:if test="/stepWithoutTransitionsOnDraft(id) == false">
                                <li><a id="add_trans_<ww:property value="id" />" href="<ww:url page="AddWorkflowTransition!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.add.transition'"/></a></li>
                            </ww:if>
                            <ww:if test="actions/empty == false">
                                <li><a id="del_trans_<ww:property value="id" />" href="<ww:url page="DeleteWorkflowTransitions!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.delete.transitions'"/></a></li>
                            </ww:if>
                            <li><a id="edit_step_<ww:property value="id" />" href="<ww:url page="EditWorkflowStep!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                        </ww:if>
                        <li><a href="<ww:url page="ViewWorkflowStepMetaAttributes.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.view.properties'"/></a></li>
                        <%-- canDeleteStep checks if the workflow isEditable --%>
                        <ww:if test="canDeleteStep(.) == true">
                            <li><a id="delete_step_<ww:property value="id" />" href="<ww:url page="DeleteWorkflowStep!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:text name="'admin.workflows.delete.step'"/></a></li>
                        </ww:if>
                        </ul>
                    </td>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
        <ww:if test="workflow/editable == true">
            <%-- Check if there are any available (unlinked) statuses, as we cannot have more than one step per JIRA status
                 in the same workflow --%>
            <ww:if test="unlinkedStatuses && unlinkedStatuses/empty == false">
                <page:applyDecorator id="workflow-step-add" name="auiform">
                    <page:param name="action">AddWorkflowStep.jspa</page:param>
                    <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
                    <page:param name="submitButtonName"><ww:text name="'common.forms.add'"/></page:param>
                    <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

                    <aui:component template="formHeading.jsp" theme="'aui'">
                        <aui:param name="'text'"><ww:text name="'admin.workflows.add.new.step'"/></aui:param>
                    </aui:component>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:textfield label="text('admin.workflows.step.name')" name="'stepName'" mandatory="true" theme="'aui'" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:select label="text('admin.workflows.linked.status')" name="'stepStatus'" list="unlinkedStatuses" listKey="'genericValue/string('id')'" listValue="'nameTranslation'" theme="'aui'" />
                    </page:applyDecorator>

                    <ww:property value="'aui'">
                        <%@ include file="basicworkflowhiddenparameters.jsp" %>
                    </ww:property>

                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">error</aui:param>
                    <aui:param name="'messageHtml'">
                            <p>
                                <ww:text name="'admin.workflows.all.existing.issue.statuses.are.used'">
                                    <ww:param name="'value0'"><br></ww:param>
                                    <ww:param name="'value1'"><a href="ViewStatuses.jspa"></ww:param>
                                    <ww:param name="'value2'"></a></ww:param>
                                </ww:text>
                            </p>
                    </aui:param>
                </aui:component>
            </ww:else>
        </ww:if>
    </div>
</div>
</body>
</html>

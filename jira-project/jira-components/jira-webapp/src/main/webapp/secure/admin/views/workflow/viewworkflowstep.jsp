<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <title><ww:text name="'admin.workflowstep.step.heading'"><ww:param name="'value0'"><ww:property value="step/name" /></ww:param></ww:text></title>
    <%
        WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
        webResourceManager.requireResourcesForContext("jira.workflow.view");
    %>
</head>
<body>

<%@ include file="/includes/admin/workflow/workflowinfobox.jsp" %>

<ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
    <ui:param name="'mainContent'">
        <ol class="aui-nav aui-nav-breadcrumbs">
            <li><a id="workflow-list" href="<ww:url page="ListWorkflows.jspa" />"><ww:text name="'admin.workflows.view.workflows'" /></a></li>
            <li><a id="workflow-steps" href="<ww:url page="ViewWorkflowSteps.jspa"><%@ include file="basicworkflowurlparameters.jsp" %></ww:url>"><ww:property value="/workflowDisplayName" /></a></li>
        </ol>
        <h2><ww:text name="'admin.workflowstep.step.heading'"><ww:param name="'value0'"><ww:property value="step/name" /></ww:param></ww:text></h2>
    </ui:param>
    <ui:param name="'actionsContent'">
        <div class="aui-buttons">
            <ww:if test="/workflow/editable == true">
                <%--Edit step--%>
                <a id="edit_step" class="aui-button" href="<ww:url page="EditWorkflowStep!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>">
                    <ww:text name="'common.forms.edit'" />
                </a>
                <%--Delete this step--%>
                <ww:if test="canDeleteStep(/step) == true">
                    <a id="del_step" class="aui-button" href="<ww:url page="DeleteWorkflowStep!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>">
                        <ww:text name="'common.words.delete'" />
                    </a>
                </ww:if>
                <ww:if test="/oldStepOnDraft(/step) == true">
                    <a id="del_step" class="aui-button" aria-disabled="true" title="<ww:text name="'admin.workflowstep.step.exists.on.active'"/>">
                        <ww:text name="'common.words.delete'" />
                    </a>
                </ww:if>
            </ww:if>
            <%--View properties--%>
            <a id="view_properties_<ww:property value="/step/id"/>" class="aui-button" href="<ww:url page="ViewWorkflowStepMetaAttributes.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /></ww:url>">
                <ww:text name="'admin.workflows.view.properties'" />
            </a>
        </div>
    </ui:param>
    <ui:param name="'helpContent'">
        <ui:component template="help.jsp" name="'workflow'" />
    </ui:param>
</ui:soy>

<div class="workflow-browser">
    <ww:if test="/workflow/editable == true">
        <div class="aui-buttons">
                <%--Add transition--%>
            <ww:if test="/stepWithoutTransitionsOnDraft(/step/id) == false">
                <a id="add_transition" class="aui-button" href="<ww:url page="AddWorkflowTransition!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>">
                    <ww:text name="'admin.workflows.add.outgoing.transition'" />
                </a>
            </ww:if>
            <ww:else>
                <a id="add_transition" class="aui-button" aria-disabled="true" title="<ww:text name="'admin.workflowtransitions.add.transition.draft.step.without.transition.message'" />">
                    <ww:text name="'admin.workflows.add.outgoing.transition'" />
                </a>
            </ww:else>
                <%--BULK Delete transitions--%>
            <ww:if test="/step/actions/empty == false">
                <a id="del_transition" class="aui-button" href="<ww:url page="DeleteWorkflowTransitions!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'originatingUrl'" value="'viewWorkflowStep'" /></ww:url>">
                    <ww:text name="'admin.workflows.delete.outgoing.transitions'" />
                </a>
            </ww:if>
            <ww:else>
                <a id="del_transition" class="aui-button" aria-disabled="true" title="<ww:text name="'admin.workflowtransitions.delete.transitions.unable'" />">
                    <ww:text name="'admin.workflows.delete.outgoing.transitions'" />
                </a>
            </ww:else>
        </div>
    </ww:if>
    <div class="workflow-browser-items">
        <div id="orig_steps" class="workflow-current-origin">
            <ol>
                <ww:if test="/inboundTransitions && inboundTransitions/empty == false">
                    <ww:iterator value="/inboundTransitions">
                        <li>
                            <ww:if test="/global(.) == true">
                                <span class="workflow-transition-lozenge workflow-transition-global">
                                    <a id="view_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                       <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>>
                                        <ww:property value="name" />
                                    </a>
                                </span>
                            </ww:if>
                            <ww:elseIf test="/initial(.) == true">
                                <span class="workflow-transition-lozenge">
                                    <a id="view_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                       <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>>
                                        <ww:property value="name" />
                                    </a>
                                </span>
                            </ww:elseIf>
                            <ww:else>
                                <ww:if test="/stepsForTransition(.)/empty == false">
                                    <span class="workflow-transition-lozenge">
                                        <a id="view_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/stepsForTransition(.)/iterator/next/id" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                           <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>>
                                            <ww:property value="name" />
                                        </a>
                                    </span>
                                </ww:if>
                                <ww:else>
                                    <span class="workflow-transition-lozenge workflow-transition-not-found">
                                        <ww:text name="'admin.workflowstep.error'"/>
                                    </span>
                                </ww:else>
                            </ww:else>
                        </li>
                    </ww:iterator>
                </ww:if>
                <ww:else>
                    <li>
                        <span class="workflow-transition-lozenge workflow-transition-not-found">
                            <ww:text name="'admin.workflowstep.notransitions'"/>
                        </span>
                    </li>
                </ww:else>
            </ol>
        </div>
        <div class="workflow-current-context">
            <ol>
                <li>
                    <ww:property value="/workflow/descriptor/step(transition/unconditionalResult/step)">
                                    <span class="workflow-transition-step workflow-inbound-step">
                                        <ww:property value="/statusObject(/step/metaAttributes/('jira.status.id'))">
                                            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                                <ww:param name="'issueStatus'" value="."/>
                                                <ww:param name="'isSubtle'" value="false"/>
                                                <ww:param name="'isCompact'" value="false"/>
                                            </ww:component>
                                        </ww:property>
                                    </span>
                    </ww:property>
                </li>
            </ol>
        </div>
        <div id="dest_steps" class="workflow-current-destination">
            <ol>
            <%--Destination--%>
                <ww:if test="/outboundTransitions && /outboundTransitions/empty == false">
                    <ww:iterator value="/outboundTransitions">
                        <li>
                            <ww:if test="/global(.) == true">
                                <span class="workflow-transition-lozenge workflow-transition-global">
                                    <a id="view_outbound_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                       <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>>
                                        <ww:property value="name" />
                                    </a>
                                </span>
                            </ww:if>
                            <ww:else>
                                <span class="workflow-transition-lozenge">
                                    <a id="view_outbound_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                       <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>>
                                        <ww:property value="name" />
                                    </a>
                                </span>
                            </ww:else>
                        </li>
                    </ww:iterator>
                </ww:if>
                <ww:else>
                    <li>
                        <ww:if test="/global(.) == true">
                            <span class="workflow-transition-lozenge workflow-transition-global">
                                <a id="view_outbound_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                   <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>>
                                    <ww:property value="name" />
                                </a>
                            </span>
                        </ww:if>
                        <ww:else>
                            <span class="workflow-transition-lozenge">
                                <a id="view_outbound_transition_<ww:property value="id" />" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="id" /></ww:url>"
                                   <ww:if test="metaAttributes/('jira.description')">title="<ww:property value="metaAttributes/('jira.description')"/>"</ww:if>>
                                    <ww:property value="name" />
                                </a>
                            </span>
                        </ww:else>
                        <span class="workflow-transition-lozenge workflow-transition-not-found">
                            <ww:text name="'admin.workflowstep.notransitions'"/>
                        </span>
                    </li>
                </ww:else>
            </ol>
        </div>
    </div>
</div>
</body>
</html>


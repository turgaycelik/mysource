<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	  <title><ww:text name="'admin.workflowtransition.transition.heading'"><ww:param name="'value0'"><ww:property value="transition/name" /></ww:param></ww:text></title>
    <script type="text/javascript">
        AJS.$(function() {

            AJS.$('.criteria-toggle-logic').change(function(){
                var currentVal = AJS.$(this).val();
                var selectedVal = AJS.$(this).find('option[selected]').attr('value');
                if (currentVal !== selectedVal) {
                    var associatedLink = AJS.$(this).attr('data-associated-toggle');
                    var url = AJS.$('.criteria-toggle-link[data-criteria-toggle="' + associatedLink + '"]').attr('href');
                    window.location = url;
                }
            });

        });
    </script>
    <%
        WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
        webResourceManager.requireResourcesForContext("jira.workflow.view");
    %>
</head>
<body>

<div id="workflow-transition-header">
    <%@ include file="/includes/admin/workflow/workflowinfobox.jsp" %>

    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <ol class="aui-nav aui-nav-breadcrumbs">
                <li><a id="workflow-list" href="<ww:url page="ListWorkflows.jspa" />"><ww:text name="'admin.workflows.view.workflows'" /></a></li>
                <li><a id="workflow-steps" href="<ww:url page="ViewWorkflowSteps.jspa"><%@ include file="basicworkflowurlparameters.jsp" %></ww:url>"><ww:property value="/workflowDisplayName" /></a></li>
            </ol>
            <h2><ww:text name="'admin.workflowtransition.transition.heading'"><ww:param name="'value0'"><ww:property value="transition/name" /></ww:param></ww:text></h2>
        </ui:param>
        <ui:param name="'actionsContent'">
            <div class="aui-buttons">
                <ww:if test="/workflow/editable == true">
                    <a id="edit_transition" class="aui-button" href="<ww:url page="EditWorkflowTransition!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>">
                        <ww:text name="'common.words.edit'" />
                    </a>
                </ww:if>
                <a id="view_transition_properties" class="aui-button" href="<ww:url page="ViewWorkflowTransitionMetaAttributes.jspa" atltoken="false"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>">
                    <ww:text name="'admin.workflows.view.properties'" />
                </a>
            </div>
            <ww:if test="/workflow/editable == true">
                <ww:if test="/global == false && /initial == false">
                    <div class="aui-buttons">
                        <a id="delete_transition" class="aui-button" href="<ww:url page="DeleteWorkflowTransitions!confirm.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'transitionIds'" value="transition/id" /></ww:url>">
                            <ww:text name="'common.words.delete'" />
                        </a>
                    </div>
                </ww:if>
            </ww:if>
        </ui:param>
        <ui:param name="'helpContent'">
            <ui:component template="help.jsp" name="'workflow'" />
        </ui:param>
    </ui:soy>

    <div class="workflow-browser">
        <div class="workflow-browser-items">
            <div id="orig_steps" class="workflow-current-origin">
                <ol>
                    <ww:if test="/stepsForTransition/size > 0">
                        <ww:iterator value="/stepsForTransition">
                            <li>
                                <a class="workflow-transition-step" id="view_inbound_step_<ww:property value="id" />" href="<ww:url page="ViewWorkflowStep.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>">
                                    <ww:property value="/statusObject(./metaAttributes/('jira.status.id'))">
                                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                            <ww:param name="'issueStatus'" value="."/>
                                            <ww:param name="'isSubtle'" value="false"/>
                                            <ww:param name="'isCompact'" value="false"/>
                                        </ww:component>
                                    </ww:property>
                                </a>
                            </li>
                        </ww:iterator>
                    </ww:if>
                    <ww:elseIf test="/initial == true">
                        <li>
                            <span class="workflow-transition-initial" title="<ww:text name="'admin.workflowtransition.initial.transition.tooltip'" />"></span>
                        </li>
                    </ww:elseIf>
                </ol>
            </div>
            <div class="workflow-current-context">
                <ol>
                    <li>
                        <ww:if test="/global == true">
                            <ww:property value="/transition">
                                <span class="workflow-transition-lozenge workflow-transition-global"><ww:property value="transition/name" /></span>
                            </ww:property>
                        </ww:if>
                        <ww:else>
                            <ww:property value="/transition">
                                <span class="workflow-transition-lozenge"><ww:property value="transition/name" /></span>
                            </ww:property>
                        </ww:else>
                    </li>
                </ol>
            </div>
            <div id="dest_steps" class="workflow-current-destination">
                <ww:if test="/transitionWithoutStepChange == true"><%-- No change to the step --%>
                    <ww:if test="/stepsForTransition/size > 0">
                        <ol>
                            <ww:iterator value="/stepsForTransition">
                                <li>
                                    <span class="aui-lozenge aui-lozenge-complete workflow-transition-step workflow-inbound-step">
                                        <a id="view_outbound_step_<ww:property value="id" />" href="<ww:url page="ViewWorkflowStep.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>"><ww:property value="name" /></a>
                                    </span>
                                </li>
                            </ww:iterator>
                        </ol>
                    </ww:if>
                </ww:if>
                <ww:else>
                        <ol>
                            <li>
                                <ww:property value="/workflow/descriptor/step(transition/unconditionalResult/step)">
                                    <span class="workflow-transition-step workflow-inbound-step">
                                        <a class="" id="view_outbound_step_<ww:property value="id" />" href="<ww:url page="ViewWorkflowStep.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="id" /></ww:url>">
                                            <ww:property value="/statusObject(metaAttributes/('jira.status.id'))">
                                                <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                                    <ww:param name="'issueStatus'" value="."/>
                                                    <ww:param name="'isSubtle'" value="false"/>
                                                    <ww:param name="'isCompact'" value="false"/>
                                                </ww:component>
                                            </ww:property>
                                        </a>
                                    </span>
                                </ww:property>
                            </li>
                        </ol>
                </ww:else>
            </div>
        </div>
    </div>

    <ww:property value="/transition/metaAttributes/('jira.description')">
        <ww:if test=". && length >  0">
            <p><b><ww:text name="'common.concepts.description'" /></b>: <ww:property value="." /></p>
        </ww:if>
    </ww:property>

    <ww:if test="/global == true">
        <%--Available to all--%>
        <p>
            <ww:text name="'admin.workflowtransition.availabletoall'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text>
        </p>
    </ww:if>
    <ww:elseIf test="/initial == true">
        <%--Initial transition (create issue)--%>
        <p>
            <ww:text name="'admin.workflowtransition.initial.transition'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text>
        </p>
    </ww:elseIf>

    <p>
        <b><ww:text name="'admin.common.words.screen'" /></b>:
        <ww:if test="/initial == true">
            <ww:text name="'admin.workflowtransition.noinitialview'"/>
        </ww:if>
        <ww:elseIf test="/fieldScreen">
            <a id="configure_fieldscreen" href="<%= request.getContextPath() %>/secure/admin/ConfigureFieldScreen.jspa?id=<ww:property value="/fieldScreen/id" />"><ww:property value="/fieldScreen/name" /></a>
        </ww:elseIf>
        <ww:else>
            <ww:text name="'admin.workflowtransition.willhappeninstantly'"/>
        </ww:else>
    </p>
</div>

<%-- Tabs start here --%>
<%--<div id="workflow-transition-info" class="aui-tabs horizontal-tabs">--%>
<div id="workflow-transition-info" class="aui-tabs aui-tabs-disabled horizontal-tabs">
    <ul class="tabs-menu">
        <ww:iterator value="/tabPanels" id="panel">
            <ww:if test="/descriptorTab == @panel/module/key">
                <ww:declare id="isTabActive"> active-tab</ww:declare>
            </ww:if>
            <ww:else>
                <ww:declare id="isTabActive"/>
            </ww:else>

            <li class="menu-item <ww:property value="@isTabActive" />">
                <a id="<ww:property value="@panel/module/name"/>" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /><ww:param name="'descriptorTab'" value="@panel/module/key" /></ww:url>">
                    <strong>
                        <ww:property value="@panel/label"/>
                    </strong>
                    <ww:if test="@panel/count">
                        <span class="aui-badge">
                            <ww:property value="@panel/count"/>
                        </span>
                    </ww:if>
                </a>
            </li>
        </ww:iterator>
    </ul>

    <%-- Tab contents --%>
    <jsp:include page="/secure/admin/views/workflow/workflow-conditions-validators-results.jsp" />

</div>
</body>
</html>

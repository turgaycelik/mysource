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
	<title><ww:text name="'admin.workflowtransition.view.workflow.transition.properties.heading'" /> - <ww:property value="transition/name" /> - <ww:property value="/workflowDisplayName" /></title>
    <%
        WebResourceManager workflowResource = ComponentAccessor.getComponent(WebResourceManager.class);
        workflowResource.requireResource("jira.webresources:workflow-transition-properties");
    %>
</head>

<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <ol class="aui-nav aui-nav-breadcrumbs">
                <li><a id="workflow-list" href="<ww:url page="ListWorkflows.jspa" />"><ww:text name="'admin.workflows.view.workflows'" /></a></li>
                <li><a id="workflow-steps" href="<ww:url page="ViewWorkflowSteps.jspa"><%@ include file="basicworkflowurlparameters.jsp" %></ww:url>"><ww:property value="/workflowDisplayName" /></a></li>
                <li><a id="workflow-transition" href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"><ww:property value="transition/name" /></a></li>
            </ol>
            <h2><ww:text name="'admin.workflowtransition.view.workflow.transition.properties.heading'" /></h2>
        </ui:param>
    </ui:soy>

    <table id="workflow-transition-properties-table" class="aui workflow-transition-properties-restfultable" data-workflowTransition="<ww:property value="transition/id"/>" data-workflowName="<ww:property value="workflow/name"/>" data-workflowMode="<ww:property value="workflow/mode"/>" data-key-header="<ww:text name="'admin.workflows.property.key'"/>" data-value-header="<ww:text name="'admin.workflows.property.value'"/>" data-workflow-editable="<ww:property value="workflow/editable" />"></table>
    <ww:if test="workflow/editable == false && /metaAttributes/empty == true">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.workflows.nowdefinedproperties'"/></p>
            </aui:param>
        </aui:component>
    </ww:if>
</body>
</html>
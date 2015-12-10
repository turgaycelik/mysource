<%@ page import="com.atlassian.jira.workflow.JiraWorkflow"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <title><ww:text name="'admin.workflowtransitions.update.title'"/></title>
</head>
<body>
<page:applyDecorator id="workflow-transition-edit" name="auiform">
    <page:param name="action">EditWorkflowTransition.jspa</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="submitButtonName">Update</page:param>
    <page:param name="cancelLinkURI"><ww:property value="/cancelUrl" /></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.workflowtransitions.update.title'"/></aui:param>
    </aui:component>

    <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.workflowtransitions.update.page.description'">
                    <ww:param name="'value0'"><strong><ww:property value="/transition/name" /></strong></ww:param>
                </ww:text>
            </p>
            <ww:if test="/nameI8n">
                <p>
                    <ww:text name="'admin.workflowtransitions.update.information'">
                        <ww:param name="'value0'"><code><%=JiraWorkflow.JIRA_META_ATTRIBUTE_I18N%></code></ww:param>
                        <ww:param name="'value1'"><code><ww:property value="nameI8n" /></code></ww:param>
                        <ww:param name="'value2'"><a href="<ww:url page="ViewWorkflowTransitionMetaAttributes.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"></ww:param>
                        <ww:param name="'value3'"></a></ww:param>
                    </ww:text>
                </p>
            </ww:if>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('admin.workflowtransitions.transition.name')" name="'transitionName'" mandatory="'true'" theme="'aui'"/>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.description')" name="'description'" theme="'aui'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('admin.workflowtransition.destinationstep')" name="'destinationStep'" list="/transitionSteps" listKey="'id'" listValue="'name'" theme="'aui'" />
    </page:applyDecorator>

    <ww:if test="/setView == true">
        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('admin.workflowtransition.transitionview')" name="'view'" list="/fieldScreens" listKey="'id'" listValue="'name'" theme="'aui'">
                <aui:param name="'defaultOptionText'"><ww:text name="'admin.workflowtransitions.no.view.for.transition'"/></aui:param>
                <aui:param name="'defaultOptionValue'" value="''" />
            </aui:select>
            <page:param name="description"><ww:text name="'admin.workflowtransitions.the.screen.that.appears.for.this.transition'"/></page:param>
        </page:applyDecorator>
    </ww:if>

    <aui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'workflowTransition'" value="transition/id" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'originatingUrl'" template="hidden.jsp" theme="'aui'" />
</page:applyDecorator>
</body>
</html>

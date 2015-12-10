<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <title><ww:text name="'admin.workflowstep.update.workflow.step'"/></title>
</head>
<body>
<page:applyDecorator id="delete-workflow" name="auiform">
    <page:param name="action">EditWorkflowStep.jspa</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="submitButtonName">Update</page:param>
    <page:param name="cancelLinkURI"><ww:property value="/cancelUrl" /></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.workflowstep.update.workflow.step'"/></aui:param>
    </aui:component>

    <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.workflowstep.update.page.description'">
                <ww:param name="'value0'"><b><ww:property value="step/name" /></b></ww:param>
            </ww:text></p>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('admin.workflowstep.step.name')" name="'stepName'" theme="'aui'" />
    </page:applyDecorator>

    <ww:if test="/oldStepOnDraft(step) == true">
        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('admin.workflowstep.linked.status')" name="'stepStatus'" list="/statuses" listKey="'string('id')'" listValue="'string('name')'" disabled="'true'" theme="'aui'" />
            <page:param name="description"><ww:text name="'admin.workflowstep.cannot.change'"/></page:param>
        </page:applyDecorator>
    </ww:if>
    <ww:else>
        <page:applyDecorator name="auifieldgroup">
            <aui:select label="text('admin.workflowstep.linked.status')" name="'stepStatus'" list="/statuses" listKey="'string('id')'" listValue="'string('name')'" theme="'aui'" />
        </page:applyDecorator>
    </ww:else>

    <aui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'aui'"  />
    <aui:component name="'originatingUrl'" template="hidden.jsp" theme="'aui'"  />
    <ww:property value="'aui'">
        <%@ include file="basicworkflowhiddenparameters.jsp" %>
    </ww:property>

</page:applyDecorator>
</body>
</html>

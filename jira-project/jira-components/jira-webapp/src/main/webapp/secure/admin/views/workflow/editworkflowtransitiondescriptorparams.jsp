<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.workflowtransition.update.workflow.transition'"/> <ww:property value="/workflowDescriptorName"/></title>
</head>

<body>

    <page:applyDecorator name="jiraform">
        <page:param name="action"><ww:property value="/actionName"/>.jspa</page:param>
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    	<page:param name="cancelURI"><ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url></page:param>
        <page:param name="title"><ww:text name="'admin.workflowtransition.update.parameters.of'">
                <ww:param name="'value0'"><ww:property value="/workflowDescriptorName"/></ww:param>
            </ww:text></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.workflowtransition.update.parameters.of'">
                <ww:param name="'value0'"><ww:property value="/workflowDescriptorName"/></ww:param>
            </ww:text>
        </page:param>

        <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" />
        <ww:property value="'standard'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>
        <ui:component name="'workflowTransition'" value="transition/id" template="hidden.jsp" />
        <ui:component name="'count'" value="count" template="hidden.jsp" />

        <ww:property value="/descriptorHtml" escape="'false'" />
    </page:applyDecorator>

</body>
</html>

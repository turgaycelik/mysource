<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.add.workflow.transition.name'">
	    <ww:param name="'value0'"><ww:property value="/workflowDescriptorName"/></ww:param>
	</ww:text></title>
</head>

<body>

    <page:applyDecorator name="jiraform">
        <page:param name="action"><ww:property value="/actionName"/>.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    	<page:param name="cancelURI"><ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url></page:param>
        <page:param name="title"><ww:text name="'admin.schemes.add.parameters'">
            <ww:param name="'value0'"><ww:property value="/workflowDescriptorName"/></ww:param>
        </ww:text></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="description">
            <ww:text name="'admin.schemes.add.required.parameters'">
                <ww:param name="'value0'"><ww:property value="/workflowDescriptorName"/></ww:param>
            </ww:text>
        </page:param>

        <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" />
        <ww:property value="'standard'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>
        <ui:component name="'workflowTransition'" value="transition/id" template="hidden.jsp" />
        <ui:component name="'pluginModuleKey'" template="hidden.jsp" />
        <ui:component name="'count'" template="hidden.jsp" />
        <ui:component name="'nested'" template="hidden.jsp" />

        <ww:property value="/descriptorHtml" escape="'false'" />
    </page:applyDecorator>

</body>
</html>

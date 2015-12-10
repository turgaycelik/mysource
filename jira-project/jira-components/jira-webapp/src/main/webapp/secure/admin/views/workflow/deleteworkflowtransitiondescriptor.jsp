<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflowtransition.delete.workflow.transition'"/> <ww:property value="/workflowDescriptorName"/></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.workflowtransition.delete.workflow.transition'"/> <ww:property value="/workflowDescriptorName"/></page:param>
	<page:param name="width">100%</page:param>
	<page:param name="action">ViewWorkflowTransition.jspa</page:param>
	<page:param name="submitId">ok_submit</page:param>
	<page:param name="submitName"> <ww:text name="'admin.common.words.ok'"/> </page:param>

    <ui:component name="'workflowStep'" value="step/id" template="hidden.jsp" />
    <ww:property value="'standard'">
        <%@ include file="basicworkflowhiddenparameters.jsp" %>
    </ww:property>
    <ui:component name="'workflowTransition'" value="transition/id" template="hidden.jsp" />

</page:applyDecorator>

</body>
</html>

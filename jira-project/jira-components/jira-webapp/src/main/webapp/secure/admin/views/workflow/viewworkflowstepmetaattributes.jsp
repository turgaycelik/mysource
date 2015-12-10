<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflows.viewproperties'"/> - <ww:property value="step/name" /></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.workflows.viewproperties'"/>: <ww:property value="step/name" /></page:param>
    <page:param name="width">100%</page:param>
    <p>
        <ww:text name="'admin.workflows.thispageshows'">
            <ww:param name="'value0'"><b><a href="<ww:url page="ViewWorkflowStep.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /></ww:url>"><ww:property value="/step/name" /></a></b></ww:param>
        </ww:text>
    </p>
    <ul>
        <li><ww:text name="'admin.workflows.a.property.consists.of'"/></li>
        <li><ww:text name="'admin.workflows.arbitrary.properties'"/></li>
    </ul>
    <ul class="optionslist">
        <li><ww:text name="'admin.workflows.viewallsteps'">
            <ww:param name="'value0'"><a href="<ww:url page="ViewWorkflowSteps.jspa"><%@ include file="basicworkflowurlparameters.jsp" %></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
            <ww:param name="'value2'"><b><ww:property value="/workflowDisplayName" /></b></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>

<%@ include file="/includes/admin/workflow/metaattributes.jsp" %>


<ww:if test="workflow/editable == true">
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddWorkflowStepMetaAttribute.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="title"><ww:text name="'admin.workflows.add.new.property'"/></page:param>
        <page:param name="width">100%</page:param>

        <ww:property value="'standard'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>
        <ui:component name="'workflowStep'" value="step/id"  template="hidden.jsp"/>

        <ui:textfield label="text('admin.workflows.property.key')" name="'attributeKey'" size="'30'" />

        <ui:textfield label="text('admin.workflows.property.value')" name="'attributeValue'" size="'30'" />
    </page:applyDecorator>
</ww:if>

</body>
</html>

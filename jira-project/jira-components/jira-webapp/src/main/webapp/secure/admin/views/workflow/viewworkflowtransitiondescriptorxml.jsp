<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.workflowtransitions.xml.view.descriptor.xml'"/></title>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.workflowtransitions.xml.conditional.result.xml'"/></page:param>
    <page:param name="width">100%</page:param>
    <p>
    <%--This shows the raw XML for Conditional Result <ww:if test="/step">from <b><ww:property value="/step/name"/></b></ww:if> to <b><ww:property value="/destinationStepDescriptor/name"/></b>.--%>
    <ww:if test="/step">
        <ww:text name="'admin.workflowtransitions.xml.page.description.from'">
            <ww:param name="'value0'"><b><ww:property value="/step/name"/></b></ww:param>
            <ww:param name="'value1'"><b><ww:property value="/destinationStepDescriptor/name"/></b></ww:param>
        </ww:text>
    </ww:if>
    <ww:else>
        <ww:text name="'admin.workflowtransitions.xml.page.description.to'">
            <ww:param name="'value0'"><b><ww:property value="/destinationStepDescriptor/name"/></b></ww:param>
        </ww:text>
    </ww:else>

    <p>
    <img src="<%= request.getContextPath() %>/images/icons/bullet_creme.gif" height=8 width=8 border=0 align=absmiddle>
    <ww:text name="'admin.workflowtransitions.xml.view.transition'">
        <ww:param name="'value0'"><a href="<ww:url page="ViewWorkflowTransition.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/transition/id" /></ww:url>"><b></ww:param>
        <ww:param name="'value1'"></b></a></ww:param>
    </ww:text>
    </p>
</page:applyDecorator>

<style>
.xml { font-size: 11px; background: #fffffc; border: 1px solid #bbb; padding: 4px; }
</style>
<pre class="xml"><ww:property value="/resultXML" escape="true" /></pre>
</body>
</html>

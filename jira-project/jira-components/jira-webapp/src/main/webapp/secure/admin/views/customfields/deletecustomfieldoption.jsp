
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
	<title><ww:text name="'admin.issuefields.customfields.delete.option'">
	    <ww:param name="'value0'"> <ww:property value="/customField/name" /></ww:param>
	</ww:text></title>
</head>
<body>



<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.issuefields.customfields.delete.option'">
	    <ww:param name="'value0'"> <ww:property value="/customField/name" /></ww:param>
	</ww:text>
    </page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="width">100%</page:param>
	<page:param name="description">
        <p>
        <ww:if test="selectedOption/parentOption">
            <ww:text name="'admin.issuefields.customfields.confirm.deletion.parent'">
                <ww:param name="'value0'"><strong><ww:property value="/customField/name" /></strong></ww:param>
                <ww:param name="'value1'"><strong><ww:property value="selectedOption/parentOption/value" /></strong></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.issuefields.customfields.confirm.deletion'">
                <ww:param name="'value0'"><strong><ww:property value="selectedOption/value" /></strong></ww:param>
            </ww:text>
        </ww:else>
        </p>
        <p><ww:text name="'admin.issuefields.customfields.delete.option.note'">
            <ww:param name="'value0'"><font color=#990000></ww:param>
            <ww:param name="'value1'"></font></ww:param>
        </ww:text></p>

	</page:param>
	<page:param name="action">EditCustomFieldOptions!remove.jspa</page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI"><ww:property value="/urlWithParent('default')" /></page:param>

    <ui:component label="text('admin.issuefields.customfields.issues.with.this.value')" name="'affectedIssues/size'" template="textlabel.jsp" >
        <ui:param name="'description'">
        <ww:text name="'common.concepts.issues'"/>:
        <ww:iterator value="affectedIssues" status="'iteratorStatus'">
            <a href="<%= request.getContextPath() %>/browse/<ww:property value="key" />"><ww:property value="key" /></a><ww:if test="@iteratorStatus/last != true">,</ww:if>
            <ww:if test="@iteratorStatus/modulus(10) == 0" ><%-- break every 10 issues --%>
            <br />
            </ww:if>
        </ww:iterator>
        </ui:param>
    </ui:component>


	<ui:component name="'fieldConfigId'" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'selectedParentOptionId'" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'selectedValue'" template="hidden.jsp" theme="'single'"  />
    <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />

</page:applyDecorator>

</body>
</html>

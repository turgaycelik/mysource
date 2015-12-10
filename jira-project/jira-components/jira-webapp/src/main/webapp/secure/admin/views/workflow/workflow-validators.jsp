<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<ww:if test="/descriptorTab == 'all' || /descriptorTab == 'validators'">

    <%-- Display Validators (if there is at least one) --%>
    <ww:if test="/descriptorTab == 'all'"><h3><ww:text name="'admin.workflowtransition.validators'"/></h3></ww:if>

    <%-- Add the 'Add Validator' Link if the workflow is not active --%>
    <ww:if test="workflow/editable == true">
    <img src="<%= request.getContextPath() %>/images/icons/bullet_creme.gif" height=8 width=8 border=0 align=absmiddle>
    <ww:text name="'admin.workflowtransition.addnewvalidator'">
        <ww:param name="'value0'"><a id="add_new_validator" href="<ww:url page="AddWorkflowTransitionValidator!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>"><b></ww:param>
        <ww:param name="'value1'"></b></a></ww:param>
    </ww:text>
    </ww:if>

    <p>
    <ww:if test="/transition/validators && (/transition/validators/empty == false)">
        <ww:if test="/transition/validators/size > 1">
            <div class="top-tick"><!-- --></div>
            <div class="condition-group">
        </ww:if>

            <ww:bean id="descriptorBean" name="'com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean'">
                <ww:param name="'descriptorCollection'" value="/transition/validators"/>
                <ww:param name="'delete'" value="workflow/editable" />
                <ww:param name="'edit'" value="workflow/editable" />
                <ww:param name="'deleteAction'">DeleteWorkflowTransitionValidator.jspa</ww:param>
                <ww:param name="'editAction'">EditWorkflowTransitionValidatorParams!default.jspa</ww:param>
                <ww:param name="'pluginType'">workflow-validator</ww:param>
                <ww:param name="'operatorTextKey'">admin.workflowtransition.operator.and</ww:param>
            </ww:bean>

            <%@ include file="/includes/admin/workflow/viewworkflowdescriptors.jsp" %>
        <ww:if test="/transition/validators/size > 1">
            </div>
            <div class="bottom-tick"><!-- --></div>
        </ww:if>
    </ww:if>
    <ww:else>
        <ww:text name="'admin.workflowtransition.no.input.parameter.checks'"/>
    </ww:else>
    </p>
</ww:if>

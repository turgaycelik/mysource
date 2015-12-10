<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<ww:if test="transition/unconditionalResult/validators && transition/unconditionalResult/validators/empty == false">
<h3><ww:text name="'admin.workflowtransition.unconditional.result.validators'"/></h3>

<p>

    <ww:if test="transition/unconditionalResult/validators/size > 1">
        <div class="top-tick"><!-- --></div>
        <div class="condition-group">
    </ww:if>

    <ww:bean id="descriptorBean" name="'com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean'">
        <ww:param name="'descriptorCollection'" value="transition/unconditionalResult/validators"/>
        <ww:param name="'delete'">false</ww:param>
        <ww:param name="'pluginType'">workflow-validator</ww:param>
        <ww:param name="'operatorTextKey'">admin.workflowtransition.operator.and</ww:param>
    </ww:bean>

    <%@ include file="/includes/admin/workflow/viewworkflowdescriptors.jsp" %>

    <ww:if test="transition/unconditionalResult/validators/size > 1">
        </div>
        <div class="bottom-tick"><!-- --></div>
    </ww:if>
</p>
</ww:if>

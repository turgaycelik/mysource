<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<ww:if test="transition/unconditionalResult/preFunctions && transition/unconditionalResult/preFunctions/empty == false">

    <h3><ww:text name="'admin.workflowtransition.unconditional.result.prefunctions'"/></h3>

    <ww:if test="transition/unconditionalResult/preFunctions/size > 1">
        <div class="top-tick"><!-- --></div>
        <div class="condition-group">
    </ww:if>

    <ww:bean id="descriptorBean" name="'com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean'">
        <ww:param name="'descriptorCollection'" value="transition/unconditionalResult/preFunctions"/>
        <ww:param name="'delete'">false</ww:param>
        <ww:param name="'pluginType'">workflow-function</ww:param>
        <ww:param name="'orderable'">false</ww:param>
        <ww:param name="'operatorTextKey'">admin.workflowtransition.operator.then</ww:param>
    </ww:bean>

    <%@ include file="/includes/admin/workflow/viewworkflowdescriptors.jsp" %>

    <ww:if test="transition/unconditionalResult/preFunctions/size > 1">
        </div>
        <div class="bottom-tick"><!-- --></div>
    </ww:if>

</ww:if>

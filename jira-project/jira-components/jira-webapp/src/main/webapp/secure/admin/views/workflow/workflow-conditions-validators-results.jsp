<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<%-- WebPanel panes --%>
<ww:declare id="webPanelContent"><ww:property value="/tabPanelContent" escape="false"/></ww:declare>
<ww:if test="@webPanelContent != ''">
    <div id="workflow-panel" class="tabs-pane active-pane">
        <ww:property value="@webPanelContent" escape="false"/>
    </div>
</ww:if>

<%-- CONDITIONS PANE --%>
<ww:if test="/initial != true">
    <ww:if test="/descriptorTab == 'conditions' || /descriptorTab == 'all'">
        <ww:declare id="isConditionsPaneActive"> active-pane</ww:declare>
    </ww:if>
    <div id="workflow-conditions" class="tabs-pane<ww:property value="@isConditionsPaneActive" />">
        <ww:if test="transition/restriction && transition/restriction/conditionsDescriptor && transition/restriction/conditionsDescriptor/conditions">
            <ww:property value="transition/restriction/conditionsDescriptor">
                <ww:if test="./conditions">
                    <ww:property id="conditionDescriptor" value="."/>
                    <ww:bean id="descriptorBeanConditions" name="'com.atlassian.jira.web.bean.WorkflowConditionFormatBean'">
                        <ww:param name="'descriptor'" value="@conditionDescriptor"/>
                        <ww:param name="'delete'" value="workflow/editable"/>
                        <ww:param name="'edit'" value="workflow/editable"/>
                        <ww:param name="'deleteAction'">DeleteWorkflowTransitionCondition.jspa</ww:param>
                        <ww:param name="'editAction'">EditWorkflowTransitionConditionParams!default.jspa</ww:param>
                        <ww:param name="'pluginType'">workflow-condition</ww:param>
                    </ww:bean>
                    <%@ include file="/includes/admin/workflow/viewworkflowconditions.jsp" %>
                </ww:if>
                <ww:else>
                    <p><ww:text name="'admin.workflowtransition.available.to.everybody'"/></p>
                </ww:else>
            </ww:property>
        </ww:if>
        <ww:else>
            <p><ww:text name="'admin.workflowtransition.no.conditions'"/></p>
            <ww:if test="workflow/editable == true">
                <p><a id="add-workflow-condition" href="<ww:url page="AddWorkflowTransitionCondition!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id"/><ww:param name="'workflowTransition'" value="transition/id"/></ww:url>" class="aui-button"><ww:text name="'admin.workflowtransition.condition.add'" /></a></p>
            </ww:if>
        </ww:else>
    </div>
</ww:if>

<%-- VALIDATORS PANE --%>
<ww:if test="/descriptorTab == 'validators'">
    <ww:declare id="isValidatorsPaneActive"> active-pane</ww:declare>
</ww:if>
<div id="workflow-validators" class="tabs-pane<ww:property value="@isValidatorsPaneActive" />">
    <ww:if test="/transition/validators && (/transition/validators/empty == false)">
        <ww:bean id="descriptorBeanValidators" name="'com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean'">
            <ww:param name="'descriptorCollection'" value="/transition/validators"/>
            <ww:param name="'delete'" value="workflow/editable" />
            <ww:param name="'edit'" value="workflow/editable" />
            <ww:param name="'deleteAction'">DeleteWorkflowTransitionValidator.jspa</ww:param>
            <ww:param name="'editAction'">EditWorkflowTransitionValidatorParams!default.jspa</ww:param>
            <ww:param name="'pluginType'">workflow-validator</ww:param>
            <ww:param name="'operatorTextKey'">admin.workflowtransition.operator.and</ww:param>
        </ww:bean>
        <%@ include file="/includes/admin/workflow/viewworkflowvalidators.jsp" %>
    </ww:if>
    <ww:else>
        <p><ww:text name="'admin.workflowtransition.no.input.parameter.checks'"/></p>
        <%-- Add the 'Add Validator' Link if the workflow is editable --%>
        <ww:if test="workflow/editable == true">
            <p><a id="add-workflow-validator" href="<ww:url page="AddWorkflowTransitionValidator!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>" class="aui-button"><ww:text name="'admin.workflowtransition.validator.add'" /></a></p>
        </ww:if>
    </ww:else>
</div>


<%-- POST FUNCTIONS PANE --%>
<ww:if test="/descriptorTab == 'postfunctions'">
    <ww:declare id="isPostFunctionsPaneActive"> active-pane</ww:declare>
</ww:if>
<div id="workflow-post-functions" class="tabs-pane<ww:property value="@isPostFunctionsPaneActive" />">
    <ww:if test="transition/unconditionalResult && /transition/unconditionalResult/postFunctions/empty == false">
            <ww:bean id="descriptorBeanPostFunctions" name="'com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean'">
                <ww:param name="'descriptorCollection'" value="transition/unconditionalResult/postFunctions"/>
                <ww:param name="'delete'" value="workflow/editable"/>
                <ww:param name="'edit'" value="workflow/editable"/>
                <ww:param name="'deleteAction'">DeleteWorkflowTransitionPostFunction.jspa</ww:param>
                <ww:param name="'editAction'">EditWorkflowTransitionPostFunctionParams!default.jspa</ww:param>
                <ww:param name="'pluginType'">workflow-function</ww:param>
                <ww:param name="'orderable'" value="workflow/editable"/>
                <ww:param name="'operatorTextKey'">admin.workflowtransition.operator.then</ww:param>
            </ww:bean>
            <%@ include file="/includes/admin/workflow/viewworkflowpostfunctions.jsp" %>
    </ww:if>
    <ww:else>
        <p><ww:text name="'admin.workflowtransition.no.post.functions'"/></p>
        <%-- Add the 'Add Post Function' Link if the workflow is editable and there aren't any results yet --%>
        <ww:if test="workflow/editable == true">
            <p><a id="add-workflow-post-function" href="<ww:url page="AddWorkflowTransitionPostFunction!default.jspa"><%@ include file="basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>" class="aui-button"><ww:text name="'admin.workflowtransition.post.function.add'" /></a></p>
        </ww:if>
    </ww:else>
</div>

<ww:if test="/descriptorTab == 'other'">
    <ww:declare id="isOtherPaneActive"> active-pane</ww:declare>
</ww:if>
<div id="workflow-other" class="tabs-pane<ww:property value="@isOtherPaneActive" />">
    <%--CAS-189 - I was unable to trigger these other 4 types of workflow methods. Will fix if we get complaints and XML which triggers it--%>
    <%--<jsp:include page="/secure/admin/views/workflow/workflow-conditionals.jsp" flush="false" />--%>
    <%--<jsp:include page="/secure/admin/views/workflow/workflow-unconditionals.jsp" flush="false" />--%>
    <%--<jsp:include page="/secure/admin/views/workflow/workflow-uncond-prefunctions.jsp" flush="false" />--%>
    <%--<jsp:include page="/secure/admin/views/workflow/workflow-global-prefuncs.jsp" flush="false" />--%>
    <jsp:include page="/secure/admin/views/workflow/workflow-global-postfuncs.jsp" flush="false" />
</div>

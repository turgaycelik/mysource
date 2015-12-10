<%@ taglib uri="webwork" prefix="ww" %>


<div class="<ww:if test="@descriptorBean/singleDescriptor() == true">single-leaf</ww:if><ww:elseIf test="@descriptorBean/highlighted(@status/count, /currentCount) == true">highlighted-leaf</ww:elseIf><ww:else>leaf</ww:else>">
    <ww:property id="descriptorInfo" value="@descriptorBean/formatDescriptor(.)"/>

    <%-- Test if we have a plugin module that generated a description --%>
    <ww:if test="@descriptorInfo/description != null">
        <ww:property value="@descriptorInfo/description" escape="false"/>
    </ww:if>
    <ww:else>
        <%-- If we do not have a plugin module then simply print all the info we have --%>
        <b>Type</b>: <ww:property value="./type"/><br>
        <ww:if test="./type == 'class' && args">
            <ww:if test="args/('class.name')">
                <b>Class</b>: <ww:property value="./args/('class.name')"/><br>
            </ww:if>
            <%-- Test if we have any other arguments than 'class.name' --%>
            <ww:if test="@descriptorBean/hasRelevantArgs(args) == true">
            <b>Arguments</b>:<br>
            <ww:iterator value="./args">
                    <ww:if test="./key != 'class.name'">
                        <ww:property value="./key"/> = <ww:property value="./value"/><br>
                    </ww:if>
            </ww:iterator>
            </ww:if>
        </ww:if>
    </ww:else>
    <ww:if test="@descriptorBean/delete == true || workflow/editable == true">
        <br />

        <ww:property value="false" id="actionprinted" />

        <ww:if test="@descriptorBean/allowNested() == true">
            <a href="<ww:url page="AddWorkflowTransitionCondition!default.jspa"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBean/parentPrefix" /><ww:property value="@status/count" /></ww:param><ww:param name="'nested'" value="'true'" /></ww:url>">
                <ww:text name="'admin.workflowtransition.addnewcondition.tonestedblock'"/></a>
            </a>
            <ww:property value="true" id="actionprinted" />
        </ww:if>
        <%-- check if the descriptor is editable --%>
        <ww:if test="@descriptorInfo/editable == true">
            <ww:if test="@actionprinted == true">|</ww:if>
            <a href="<ww:url value="@descriptorBean/editAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBean/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>">
                <ww:text name="'common.words.edit'"/>
            </a>
            <ww:property value="true" id="actionprinted" />
        </ww:if>

        <ww:if test="@descriptorInfo/orderable == true">
            <ww:if test="@status/first != true">
                <ww:if test="@actionprinted == true">|</ww:if>
                <a href="<ww:url value="'MoveWorkflowFunctionUp.jspa'"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'up'" value="@status/index" /></ww:url>">
                    Move Up
                </a>
                <ww:property value="true" id="actionprinted" />
            </ww:if>
            <ww:if test="@status/last != true">
                <ww:if test="@actionprinted == true">|</ww:if>
                <a href="<ww:url value="'MoveWorkflowFunctionDown.jspa'"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'down'" value="@status/index" /></ww:url>">
                    Move Down
                </a>
                <ww:property value="true" id="actionprinted" />
            </ww:if>
        </ww:if>

        <ww:if test="@descriptorInfo/deletable == true">
            <ww:if test="@actionprinted == true">|</ww:if>
            <a href="<ww:url value="@descriptorBean/deleteAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBean/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>">
                <ww:text name="'common.words.delete'"/>
            </a>
            <ww:property value="true" id="actionprinted" />
        </ww:if>

        <%-- reset the value for the next descriptor --%>
        <ww:property value="false" id="actionprinted" />
    </ww:if>
</div>

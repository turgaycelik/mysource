<%@ taglib uri="webwork" prefix="ww" %>
<%-- Expects a WorkflowConditionFormatBean to be in context with id 'descriptorBeanConditions' --%>
<div class="criteria-group">
    <ww:iterator value="@descriptorBeanConditions/descriptorCollection" status="'status'" >
        <ww:if test="@status/first == true">
                <form class="aui criteria-group-actions dont-default-focus">
                    <ww:if test="workflow/editable == true">
                        <a class="aui-button aui-button-link criteria-condition-add" href="<ww:url page="AddWorkflowTransitionCondition!default.jspa"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanConditions/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>"><ww:text name="'admin.workflowtransition.condition.add'" /></a>
                        <select class="select criteria-toggle-logic" data-associated-toggle="workflow-<ww:property value="step/id" /><ww:property value="transition/id" />-toggle-condition-<ww:property value="@descriptorBeanConditions/parentPrefix" /><ww:property value="@status/count" />">
                            <option value="AND" <ww:if test="@descriptorBeanConditions/operatorType == 'AND'">selected</ww:if>><ww:text name="'admin.workflowtransition.condition.operator.and'" /></option>
                            <option value="OR" <ww:if test="@descriptorBeanConditions/operatorType == 'OR'">selected</ww:if>><ww:text name="'admin.workflowtransition.condition.operator.or'" /></option>
                        </select>
                        <a class="criteria-toggle-link" href="<ww:url value="'ViewWorkflowTransition!changeLogicOperator.jspa'"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanConditions/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" data-criteria-toggle="workflow-<ww:property value="step/id" /><ww:property value="transition/id" />-toggle-condition-<ww:property value="@descriptorBeanConditions/parentPrefix" /><ww:property value="@status/count" />">
                            <ww:text name="'admin.workflowtransition.changeoperator'">
                                <ww:param name="'value0'"><ww:text name="@descriptorBeanConditions/otherOperatorTextKey"/></ww:param>
                            </ww:text>
                        </a>
                    </ww:if>
                    <ww:else>
                        <a class="aui-button aui-button-link criteria-condition-add" aria-disabled="true"><ww:text name="'admin.workflowtransition.condition.add'" /></a>
                        <select disabled class="select">
                            <option value="AND" <ww:if test="@descriptorBeanConditions/operatorType == 'AND'">selected</ww:if>><ww:text name="'admin.workflowtransition.condition.operator.and'" /></option>
                            <option value="OR" <ww:if test="@descriptorBeanConditions/operatorType == 'OR'">selected</ww:if>><ww:text name="'admin.workflowtransition.condition.operator.or'" /></option>
                        </select>
                    </ww:else>
                </form>
                <ul class="criteria-list">
        </ww:if>

        <ww:if test="@descriptorBeanConditions/nestedDescriptor(.) == false">

            <ww:property id="descriptorInfo" value="@descriptorBeanConditions/formatDescriptor(.)"/>
            <ww:if test="@descriptorBeanConditions/highlighted(@status/count, /currentCount) == true">
                <ww:declare id="isHighlighted" value="true" />
            </ww:if>

            <li class="criteria-item">
                <ww:if test="@descriptorBeanConditions/delete == true || workflow/editable == true">
                    <div class="aui-buttons">

                        <%-- Add grouped condition --%>
                        <ww:if test="@descriptorBeanConditions/allowNested() == true">
                            <a class="aui-button aui-button-link criteria-condition-add-grouped" href="<ww:url page="AddWorkflowTransitionCondition!default.jspa"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanConditions/parentPrefix" /><ww:property value="@status/count" /></ww:param><ww:param name="'nested'" value="'true'" /></ww:url>" title="<ww:text name="'admin.workflowtransition.addnewcondition.tonestedblock'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-devtools-fork">
                                    <ww:text name="'admin.workflowtransition.addnewcondition.tonestedblock'"/>
                                </span>
                            </a>
                        </ww:if>
                        <ww:else>
                            <a class="aui-button aui-button-link criteria-condition-add-grouped" aria-disabled="true" title="<ww:text name="'admin.workflowtransition.addnewcondition.tonestedblock'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-devtools-fork">
                                    <ww:text name="'admin.workflowtransition.addnewcondition.tonestedblock'"/>
                                </span>
                            </a>
                        </ww:else>

                        <%-- Edit--%>
                        <ww:if test="@descriptorInfo/editable == true">
                            <a class="aui-button aui-button-link criteria-condition-edit" href="<ww:url value="@descriptorBeanConditions/editAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanConditions/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.edit'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-edit"><ww:text name="'common.words.edit'"/></span>
                            </a>
                        </ww:if>
                        <ww:else>
                            <a class="aui-button aui-button-link criteria-condition-edit" aria-disabled="true" title="<ww:text name="'common.words.edit'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-edit"><ww:text name="'common.words.edit'"/></span>
                            </a>
                        </ww:else>

                        <%-- Delete --%>
                        <ww:if test="@descriptorInfo/deletable == true">
                            <a class="aui-button aui-button-link criteria-condition-delete" href="<ww:url value="@descriptorBeanConditions/deleteAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanConditions/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.delete'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-remove"><ww:text name="'common.words.delete'"/></span>
                            </a>
                        </ww:if>
                        <ww:else>
                            <a class="aui-button aui-button-link criteria-condition-delete" aria-disabled="true" title="<ww:text name="'common.words.delete'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-remove"><ww:text name="'common.words.delete'"/></span>
                            </a>
                        </ww:else>
                    </div>
                </ww:if>

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
                        <ww:if test="@descriptorBeanConditions/hasRelevantArgs(args) == true">
                            <b>Arguments</b>:<br>
                            <ww:iterator value="./args">
                                <ww:if test="./key != 'class.name'">
                                    <ww:property value="./key"/> = <ww:property value="./value"/><br>
                                </ww:if>
                            </ww:iterator>
                        </ww:if>
                    </ww:if>
                </ww:else>
            </li>
        </ww:if>
        <ww:else>
            <%-- Loop into this JSP with a new descriptorBeanConditions that represents the nested ConditionsDescriptor --%>

            <%-- needed to get around the WW bug of looking up the stack - so use the id --%>
            <ww:property value="." id="conditionsDescriptor" />

            <ww:if test="@conditionsDescriptor/conditions && @conditionsDescriptor/conditions/empty == false">

                <%-- save the descriptor bean on top of the stack --%>
                <ww:property value="@descriptorBeanConditions">
                    <%-- Hack to get around the WebWork EL bug --%>
                    <ww:property value="./deleteAction" id="da" />
                    <ww:property value="./editAction" id="ea" />
                    <ww:property value="@descriptorBeanConditions/parentPrefix" id="pp" />
                    <%-- create a new descriptor bean to represent the conditions element --%>
                    <ww:bean id="descriptorBeanConditions" name="'com.atlassian.jira.web.bean.WorkflowConditionFormatBean'">
                        <ww:param name="'descriptor'" value="@conditionsDescriptor"/>
                        <ww:param name="'pluginType'" value="'workflow-condition'" />
                        <ww:param name="'parentPrefix'"><ww:property value="@pp" /><ww:property value="@status/count" /></ww:param>
                        <ww:param name="'delete'" value="/workflow/editable" />
                        <ww:param name="'edit'" value="workflow/editable" />
                        <ww:param name="'deleteAction'" value="@da" />
                        <ww:param name="'editAction'" value="@ea" />
                    </ww:bean>

                    <%-- recurse to this JSP to print out the ConditionsDescriptor which likely has more than one
                    ConditionDescriptor inside --%>

                    <li class="criteria-item">
                        <jsp:include page="/includes/admin/workflow/viewworkflowconditions.jsp" />
                    </li>

                    <%-- put the old descriptor format bean back --%>
                    <ww:property id="descriptorBeanConditions" value="."/>

                </ww:property>

            </ww:if>
        </ww:else>
    </ww:iterator>
    </ul>
</div>
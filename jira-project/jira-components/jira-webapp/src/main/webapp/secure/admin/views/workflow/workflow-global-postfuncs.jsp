<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<ww:if test="transition/postFunctions && transition/postFunctions/empty == false">
    <div class="criteria-group">
        <div class="criteria-group-actions">
            <p><ww:text name="'admin.workflows.global.post.functions'" /></p>
        </div>

        <ww:bean id="descriptorBeanWorkflowGlobalPostFuncs" name="'com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean'">
            <ww:param name="'descriptorCollection'" value="transition/postFunctions"/>
            <ww:param name="'delete'">false</ww:param>
            <ww:param name="'pluginType'">workflow-function</ww:param>
            <ww:param name="'operatorTextKey'">admin.workflowtransition.operator.then</ww:param>
        </ww:bean>

        <ol class="criteria-list criteria-post-functions">
            <ww:iterator value="@descriptorBeanWorkflowGlobalPostFuncs/descriptorCollection" status="'status'" >
                <ww:property id="descriptorInfo" value="@descriptorBeanWorkflowGlobalPostFuncs/formatDescriptor(.)"/>

                <ww:if test="@descriptorBeanWorkflowGlobalPostFuncs/highlighted(@status/count, /currentCount) == true">
                    <ww:declare id="isHighlighted" value="true" />
                </ww:if>

                <li class="criteria-item">
                    <ww:if test="@descriptorBeanWorkflowGlobalPostFuncs/delete == true || workflow/editable == true">
                        <div class="aui-buttons">

                            <%-- Add grouped condition --%>
                            <ww:if test="@descriptorBeanWorkflowGlobalPostFuncs/allowNested() == true">
                                <a class="aui-button aui-button-link criteria-condition-add-grouped" href="<ww:url page="AddWorkflowTransitionCondition!default.jspa"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanWorkflowGlobalPostFuncs/parentPrefix" /><ww:property value="@status/count" /></ww:param><ww:param name="'nested'" value="'true'" /></ww:url>" title="<ww:text name="'admin.workflowtransition.addnewcondition.tonestedblock'"/>">
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

                            <%-- Move up - if it's the first one or not orderable we disable it and remove the href  --%>
                            <ww:if test="@descriptorInfo/orderable == false || @status/first == true">
                                <a class="aui-button aui-button-link criteria-move-up" aria-disabled="true" title="<ww:text name="'admin.workflowdescriptor.move.up'" />"><span><ww:text name="'admin.workflowdescriptor.move.up'" /></span></a>
                            </ww:if>
                            <ww:else>
                                <a class="aui-button aui-button-link criteria-move-up" href="<ww:url value="'MoveWorkflowFunctionUp.jspa'"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'up'" value="@status/index" /></ww:url>" title="<ww:text name="'admin.workflowdescriptor.move.up'" />"><span><ww:text name="'admin.workflowdescriptor.move.up'" /></span></a>
                            </ww:else>

                            <%-- Move down - if it's the last one or not orderable we disable it and remove the href --%>
                            <ww:if test="@descriptorInfo/orderable == false || @status/last == true">
                                <a class="aui-button aui-button-link criteria-move-down" aria-disabled="true" title="<ww:text name="'admin.workflowdescriptor.move.down'" />"><span><ww:text name="'admin.workflowdescriptor.move.down'" /></span></a>
                            </ww:if>
                            <ww:else>
                                <a class="aui-button aui-button-link criteria-move-down" href="<ww:url value="'MoveWorkflowFunctionDown.jspa'"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'down'" value="@status/index" /></ww:url>" title="<ww:text name="'admin.workflowdescriptor.move.down'" />"><span><ww:text name="'admin.workflowdescriptor.move.down'" /></span></a>
                            </ww:else>

                            <%-- Edit--%>
                            <ww:if test="@descriptorInfo/editable != true">
                                <a class="aui-button aui-button-link criteria-post-function-edit" aria-disabled="true">
                                    <span class="aui-icon aui-icon-small aui-iconfont-edit"><ww:text name="'common.words.edit'"/></span>
                                </a>
                            </ww:if>
                            <ww:else>
                                <a class="aui-button aui-button-link criteria-post-function-edit" href="<ww:url value="@descriptorBeanWorkflowGlobalPostFuncs/editAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanWorkflowGlobalPostFuncs/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.edit'"/>">
                                    <span class="aui-icon aui-icon-small aui-iconfont-edit"><ww:text name="'common.words.edit'"/></span>
                                </a>
                            </ww:else>

                            <%-- Delete --%>
                            <ww:if test="@descriptorInfo/deletable != true">
                                <a class="aui-button aui-button-link criteria-post-function-delete" aria-disabled="true" title="<ww:text name="'common.words.delete'"/>">
                                    <span class="aui-icon aui-icon-small aui-iconfont-remove"><ww:text name="'common.words.delete'"/></span>
                                </a>
                            </ww:if>
                            <ww:else>
                                <a class="aui-button aui-button-link criteria-post-function-delete" href="<ww:url value="@descriptorBeanWorkflowGlobalPostFuncs/deleteAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanWorkflowGlobalPostFuncs/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.delete'"/>">
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
                            <ww:if test="@descriptorBeanWorkflowGlobalPostFuncs/hasRelevantArgs(args) == true">
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

            </ww:iterator>
        </ol>
    </div>
</ww:if>
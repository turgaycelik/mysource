<%@ taglib uri="webwork" prefix="ww" %>
<%-- Expects a WorkflowConditionFormatBean to be in context with id 'descriptorBeanPostFunctions' --%>
<div class="criteria-group">
    <form class="aui criteria-group-actions">
        <ww:if test="workflow/editable == true">
            <a href="<ww:url page="AddWorkflowTransitionPostFunction!default.jspa"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>" class="aui-button aui-button-link criteria-post-function-add"><ww:text name="'admin.workflowtransition.post.function.add'" /></a>
            <p><ww:text name="'admin.workflowtransition.post.function.occurs'" /></p>
        </ww:if>
        <ww:else>
            <a aria-disabled="true" class="aui-button aui-button-link criteria-post-function-add"><ww:text name="'admin.workflowtransition.post.function.add'" /></a>
            <p><ww:text name="'admin.workflowtransition.post.function.occurs'" /></p>
        </ww:else>
    </form>

    <ol class="criteria-list criteria-post-functions">
        <ww:iterator value="@descriptorBeanPostFunctions/descriptorCollection" status="'status'" >
            <ww:property id="descriptorInfo" value="@descriptorBeanPostFunctions/formatDescriptor(.)"/>

            <ww:if test="@descriptorBeanPostFunctions/highlighted(@status/count, /currentCount) == true">
                <ww:declare id="isHighlighted" value="true" />
            </ww:if>

            <li class="criteria-item">
                <ww:if test="@descriptorBeanPostFunctions/delete == true || workflow/editable == true">
                    <div class="aui-buttons">

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
                            <a class="aui-button aui-button-link criteria-post-function-edit" href="<ww:url value="@descriptorBeanPostFunctions/editAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanPostFunctions/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.edit'"/>">
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
                            <a class="aui-button aui-button-link criteria-post-function-delete" href="<ww:url value="@descriptorBeanPostFunctions/deleteAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanPostFunctions/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.delete'"/>">
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
                        <ww:if test="@descriptorBeanPostFunctions/hasRelevantArgs(args) == true">
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
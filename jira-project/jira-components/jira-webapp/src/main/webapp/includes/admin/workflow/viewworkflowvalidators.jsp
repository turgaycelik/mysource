<%@ taglib uri="webwork" prefix="ww" %>
<%-- Expects a WorkflowConditionFormatBean to be in context with id 'descriptorBeanValidators' --%>
<div class="criteria-group">
    <form class="aui criteria-group-actions">
        <ww:if test="workflow/editable == true">
            <a href="<ww:url page="AddWorkflowTransitionValidator!default.jspa"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /></ww:url>" class="aui-button aui-button-link criteria-validator-add"><ww:text name="'admin.workflowtransition.validator.add'" /></a>
            <p><ww:text name="'admin.workflowtransition.validator.occurs'" /></p>
        </ww:if>
        <ww:else>
            <a aria-disabled="true" class="aui-button aui-button-link criteria-validator-add"><ww:text name="'admin.workflowtransition.validator.add'" /></a>
            <p><ww:text name="'admin.workflowtransition.validator.occurs'" /></p>
        </ww:else>
    </form>

    <ol class="criteria-list">
        <ww:iterator value="@descriptorBeanValidators/descriptorCollection" status="'status'" >
            <ww:property id="descriptorInfo" value="@descriptorBeanValidators/formatDescriptor(.)"/>
            <ww:if test="@descriptorBeanValidators/highlighted(@status/count, /currentCount) == true">
                <ww:declare id="isHighlighted" value="true" />
            </ww:if>

            <li class="criteria-item">
                <ww:if test="@descriptorBeanValidators/delete == true || workflow/editable == true">
                    <div class="aui-buttons">

                        <%-- Edit--%>
                        <ww:if test="@descriptorInfo/editable == true">
                            <a class="aui-button aui-button-link criteria-validator-edit" href="<ww:url value="@descriptorBeanValidators/editAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanValidators/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.edit'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-edit"><ww:text name="'common.words.edit'"/></span>
                            </a>
                        </ww:if>
                        <ww:else>
                            <a class="aui-button aui-button-link criteria-validator-edit" aria-disabled="true" title="<ww:text name="'common.words.edit'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-edit"><ww:text name="'common.words.edit'"/></span>
                            </a>
                        </ww:else>

                        <%-- Delete --%>
                        <ww:if test="@descriptorInfo/deletable == true">
                            <a class="aui-button aui-button-link criteria-validator-delete" href="<ww:url value="@descriptorBeanValidators/deleteAction"><%@ include file="/secure/admin/views/workflow/basicworkflowurlparameters.jsp" %><ww:param name="'workflowStep'" value="step/id" /><ww:param name="'workflowTransition'" value="transition/id" /><ww:param name="'count'"><ww:property value="@descriptorBeanValidators/parentPrefix" /><ww:property value="@status/count" /></ww:param></ww:url>" title="<ww:text name="'common.words.delete'"/>">
                                <span class="aui-icon aui-icon-small aui-iconfont-remove"><ww:text name="'common.words.delete'"/></span>
                            </a>
                        </ww:if>
                        <ww:else>
                            <a class="aui-button aui-button-link criteria-validator-delete" aria-disabled="true" title="<ww:text name="'common.words.delete'"/>">
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
                        <ww:if test="@descriptorBeanValidators/hasRelevantArgs(args) == true">
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
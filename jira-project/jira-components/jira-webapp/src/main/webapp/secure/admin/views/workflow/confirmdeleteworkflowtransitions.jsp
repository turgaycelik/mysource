<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <title><ww:text name="'admin.workflowtransitions.delete.workflow.transitions'"/></title>
</head>
<body>
<page:applyDecorator id="add-workflow" name="auiform">
    <page:param name="action">DeleteWorkflowTransitions.jspa</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="submitButtonName">Delete</page:param>
    <page:param name="cancelLinkURI"><ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="workflow/mode" /><ww:param name="'workflowName'" value="/workflow/name" /><ww:param name="'workflowStep'" value="/step/id" /><ww:param name="'workflowTransition'" value="/selectedTransitions/iterator/next/id" /></ww:url></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.workflowtransitions.delete.workflow.transitions'"/></aui:param>
    </aui:component>

    <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.workflowtransitions.delete.confirm.deletion'">
                    <ww:param name="'value0'"><ww:iterator value="/selectedTransitions" status="'status'">
                        <ww:if test="@status/first == false">, </ww:if><b><ww:property value="name" /></b>
                    </ww:iterator></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

    <aui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'aui'"  />
    <aui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" theme="'aui'"  />
    <aui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" theme="'aui'" />
    <ww:iterator value="/selectedTransitions">
        <aui:component name="'transitionIds'" value="./id" template="hidden.jsp" theme="'aui'" />
    </ww:iterator>
</page:applyDecorator>

</body>
</html>

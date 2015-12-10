<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <title><ww:text name="'admin.workflowtransitions.delete.title'"/></title>
</head>
<body>
<page:applyDecorator id="add-workflow" name="auiform">
    <page:param name="action">DeleteWorkflowTransitions.jspa</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="submitButtonName">Delete</page:param>
    <page:param name="cancelLinkURI"><ww:property value="/cancelUrl" /></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.workflowtransitions.delete.title'"/></aui:param>
    </aui:component>

    <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.workflowtransitions.delete.instructions'">
                    <ww:param name="'value0'"><b><ww:property value="/step/name" /></b></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <aui:select label="text('admin.workflowtransitions.transitions')" name="'transitionIds'" list="/transitions" listKey="'id'" listValue="'name'" theme="'aui'">
            <aui:param name="'rows'">5</aui:param>
        </aui:select>
    </page:applyDecorator>

    <aui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'aui'"  />
    <aui:component name="'workflowName'" value="workflow/name" template="hidden.jsp" theme="'aui'"  />
    <aui:component name="'workflowMode'" value="workflow/mode" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'originatingUrl'" template="hidden.jsp" theme="'aui'"  />
</page:applyDecorator>
</body>
</html>

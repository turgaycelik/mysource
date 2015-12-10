<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	  <title><ww:text name="'admin.workflowstep.delete'"/>: <ww:property value="step/name" /></title>
</head>
<body>
    <page:applyDecorator id="delete-workflow" name="auiform">
        <page:param name="action">DeleteWorkflowStep.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="cancelLinkURI"><ww:property value="/cancelUrl" /></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.workflowstep.delete'"/>: <ww:property value="step/name" /></aui:param>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.workflowstep.delete.confirmation'"/></p>
            </aui:param>
        </aui:component>

        <aui:component name="'workflowStep'" value="step/id" template="hidden.jsp" theme="'aui'"  />
        <aui:component name="'originatingUrl'" template="hidden.jsp" theme="'aui'"  />
        <ww:property value="'aui'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>
    </page:applyDecorator>
</body>
</html>

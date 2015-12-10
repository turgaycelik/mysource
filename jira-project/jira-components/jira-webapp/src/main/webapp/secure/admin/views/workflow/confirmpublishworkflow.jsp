<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
	<title><ww:text name="'admin.workflows.publish.title'"/></title>
</head>
<body>
<page:applyDecorator id="publish-workflow" name="auiform">
        <page:param name="action">PublishDraftWorkflow.jspa</page:param>
        <page:param name="cssClass">long-label</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.publish'"/></page:param>
        <page:param name="submitButtonName">Publish</page:param>
        <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.workflows.publish.title'"/></aui:param>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.workflows.publish.description'">
                        <ww:param name="'value0'"><strong></ww:param>
                        <ww:param name="'value1'"><ww:property value="/workflowDisplayName"/></ww:param>
                        <ww:param name="'value2'"></strong></ww:param>
                        <ww:param name="'value3'"><ww:property value="/workflow/name"/></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>

        <ww:if test="/madeDeliberateChoice == false">
            <script language="javascript" type="text/javascript">
               // This will ensure that the user MUST select a check box by choice!
               <%-- TODO: SEAN clean this up and move this inline JS into somewhere more appropriate --%>
               AJS.$(function(){
                   AJS.$('#publish-workflow-false, #publish-workflow-true').attr("checked", false);

                   var newWorkflowNameTextfield = AJS.$('#publish-workflow-newWorkflowName').attr('disabled','disabled');
                   AJS.$(':radio[name="enableBackup"]').click(function(){
                       AJS.$(this).val() === "true" ? newWorkflowNameTextfield.removeAttr('disabled') : newWorkflowNameTextfield.attr('disabled', true);
                   });
               });
           </script>
        </ww:if>
        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:param name="legend"><ww:text name="'admin.workflows.publish.save.copy.active'"/></page:param>
            <aui:radio value="enableBackup" label="''" list="booleanList" listKey="'id'" listValue="'name'" name="'enableBackup'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.common.phrases.use.only.ascii'"/></page:param>
            <aui:textfield label="text('admin.workflows.publish.save.backup.name')" id="newWorkflowName" name="'newWorkflowName'" size="'50'" maxlength="255" mandatory="'true'" theme="'aui'" />
        </page:applyDecorator>

        <ww:property value="'aui'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>

    <aui:component name="'madeDeliberateChoice'" value="/madeDeliberateChoice" template="hidden.jsp" theme="aui" />
</page:applyDecorator>
</body>
</html>

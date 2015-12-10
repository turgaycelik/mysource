<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <title><ww:text name="'admin.schemes.workflows.copy.workflow'"/>  - <ww:property value="workflow/name" /></title>
</head>
<body>
    <page:applyDecorator id="copy-workflow" name="auiform">
        <page:param name="action">CloneWorkflow.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.copy'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>
        <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.schemes.workflows.copy.workflow'"/>: <ww:property value="workflow/name" escape="'false'" /></aui:param>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.schemes.workflows.please.enter.a.name.and.description'"/></p>
            </aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="description"><ww:text name="'admin.common.phrases.use.only.ascii'"/></page:param>
            <aui:textfield label="text('admin.schemes.workflows.workflow.name')" name="'newWorkflowName'" size="'50'" maxlength="255" mandatory="'true'" theme="'aui'"/>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.description')" name="'description'" size="'50'" maxlength="255" mandatory="'false'" theme="'aui'"/>
        </page:applyDecorator>

        <aui:component name="'workflowName'" value="/workflow/name" template="hidden.jsp" theme="aui" />
        <aui:component name="'workflowMode'" value="/workflow/mode" template="hidden.jsp" theme="aui" />
    </page:applyDecorator>
</body>
</html>

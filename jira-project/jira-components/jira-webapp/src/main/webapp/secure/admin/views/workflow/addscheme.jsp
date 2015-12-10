<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.schemes.workflow.add.workflow.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
    <meta name="decorator" content="panel-admin"/>
</head>
<body>

    <page:applyDecorator id="add-workflow-scheme" name="auiform">
        <page:param name="action">AddWorkflowScheme.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="submitButtonName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="cancelLinkURI">ViewWorkflowSchemes.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.schemes.workflow.add.workflow.scheme'"/></aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.name')" id="'name'" mandatory="true" maxlength="100" name="'name'" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.description')" id="'description'" mandatory="false" maxlength="255" name="'description'" theme="'aui'" />
        </page:applyDecorator>
    </page:applyDecorator>

</body>
</html>

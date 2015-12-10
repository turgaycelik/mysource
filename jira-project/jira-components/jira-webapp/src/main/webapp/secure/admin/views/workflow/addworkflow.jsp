<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.workflows.add.new.workflow'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <meta name="decorator" content="panel-admin"/>
</head>
<body>

    <page:applyDecorator id="add-workflow" name="auiform">
        <page:param name="action">AddWorkflow.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="submitButtonName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.workflows.add.new.workflow'"/></aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.name')" id="'newWorkflowName'" mandatory="true" maxlength="255" name="'newWorkflowName'" theme="'aui'" />
            <page:param name="description"><ww:text name="'admin.common.phrases.use.only.ascii'"/></page:param>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.description')" id="'description'" mandatory="false" maxlength="255" name="'description'" theme="'aui'" />
        </page:applyDecorator>
    </page:applyDecorator>

</body>
</html>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.workflows.importworkflow.title'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="workflows"/>
    <meta name="decorator" content="panel-admin"/>
</head>
<body>

<page:applyDecorator id="import-workflow" name="auiform">
    <page:param name="action">ImportWorkflowFromXml.jspa</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.import'"/></page:param>
    <page:param name="submitButtonName"><ww:text name="'common.forms.import'"/></page:param>
    <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.workflows.importworkflow.import.workflow'"/></aui:param>
    </aui:component>

    <p><ww:text name="'admin.workflows.importworkflow.specify.name'"/></p>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.name')" id="'name'" mandatory="true" maxlength="255" name="'name'" theme="'aui'" size="'long'" />
        <page:param name="description"><ww:text name="'admin.workflows.importworkflow.name.description'"/></page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('common.words.description')" id="'description'" mandatory="false" maxlength="255" name="'description'" theme="'aui'" size="'long'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="cssClass">content-toggle</page:param>
        <page:param name="legend">Workflow Definition</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <aui:radio id="import-xml-file" label="text('admin.workflows.importworkflow.specify.provide.xml.path')" list="null" name="'definition'" theme="'aui'" >
                <aui:param name="'customValue'">file</aui:param>
                <ww:if test="definition != 'inline'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <aui:radio id="import-xml-definition" label="text('admin.workflows.importworkflow.paste.xml.definition')" list="null" name="'definition'" theme="'aui'" >
                <aui:param name="'customValue'">inline</aui:param>
                <ww:if test="definition == 'inline'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup" id="definition-file-content">
        <ww:if test="definition == 'inline'">
            <page:param name="cssClass">hidden</page:param>
        </ww:if>
        <aui:textfield label="text('admin.workflows.importworkflow.file.path')" id="'filePath'" mandatory="true" maxlength="60" name="'filePath'" theme="'aui'" size="'long'" />
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup" id="definition-inline-content">
        <ww:if test="definition != 'inline'">
            <page:param name="cssClass">hidden</page:param>
        </ww:if>
        <aui:textarea label="text('admin.workflows.importworkflow.workflow.definition.xml')" id="'workflowXML'" mandatory="true" rows="10" cols="40" name="'workflowXML'" theme="'aui'" size="'long'" />
    </page:applyDecorator>

</page:applyDecorator>

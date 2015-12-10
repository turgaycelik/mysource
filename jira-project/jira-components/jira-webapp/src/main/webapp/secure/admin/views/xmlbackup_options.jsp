<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.export.backup.all.jira.data'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/import_export_section"/>
    <meta name="admin.active.tab" content="backup_data"/>
</head>
<body>

<page:applyDecorator name="jiraform">
	<page:param name="action">XmlBackup.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.export.backup.jira.data'"/></page:param>
	<page:param name="description">
		<p><ww:text name="'admin.export.xml.backup.description'"/></p>
        <p><ww:text name="'admin.export.xml.backup.description2'"/><p>
        <p><ww:text name="'admin.export.xml.backup.description3'"/>: <strong><ww:property value="/safeBackupPath"/></strong><p>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.export.xml.backup.note2'" /></p>
                <p><ww:text name="'admin.export.xml.backup.note'" /></p>
            </aui:param>
        </aui:component>
    </page:param>
	<page:param name="submitId">backup_submit</page:param>
	<page:param name="submitName"><ww:text name="'admin.common.words.backup'"/></page:param>
	<page:param name="cancelURI">default.jsp</page:param>
	<page:param name="width">100%</page:param>
    <page:param name="helpURL">backup_data</page:param>
	<ui:textfield label="text('admin.export.file.name')" name="'filename'">		<ui:param name="'size'">50</ui:param>
		<%--<ui:param name="'style'">width:95%;</ui:param>--%>
	</ui:textfield>

    <ui:component name="'saxParser'" template="hidden.jsp" theme="'single'"  />
</page:applyDecorator>
</body>
</html>

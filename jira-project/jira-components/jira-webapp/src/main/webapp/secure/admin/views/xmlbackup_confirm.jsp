
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.export.backup.data.confirm'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/import_export_section"/>
    <meta name="admin.active.tab" content="backup_data"/>
</head>

<body>
<table cellpadding=0 cellspacing=0 border=0 width=100%>
<tr>
<td bgcolor=#ffffff valign=top>

<table width=100% cellpadding=10 cellspacing=0 border=0><tr><td>

    <page:applyDecorator name="jiraform">
        <page:param name="title"><ww:text name="'admin.export.backup.data.confirm'"/></page:param>
        <page:param name="description">
            <p><ww:text name="'admin.export.file.already.exists'">
                <ww:param name="'value0'"><b><font color="#ff0000"><ww:property value="destinationFile" /></font></b></ww:param>
            </ww:text></p>
            <p><ww:text name="'admin.export.confirm.file.replacement'"/></p>
        </page:param>
        <page:param name="columns">1</page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
        <page:param name="action">XmlBackup.jspa</page:param>
        <page:param name="submitId">replace_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.export.replace.file'"/></page:param>
        <page:param name="cancelURI"><ww:url page="XmlBackup!default.jspa"><ww:param name="'filename'" value="filename" /><ww:param name="'useZip'" value="useZip" /></ww:url></page:param>


        <ui:component name="'filename'" template="hidden.jsp" />
        <ui:component name="'useZip'" template="hidden.jsp" />
        <ui:component name="'saxParser'" template="hidden.jsp" />

        <input type="hidden" name="confirm" value="true">
    </page:applyDecorator>

</td></tr></table>
</td></tr></table>

</body>
</html>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.subtasks.disable'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="subtasks"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">DisableSubTasks.jspa</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.subtasks.disable'"/></page:param>
    <page:param name="submitId">ok_submit</page:param>
    <page:param name="submitName"> <ww:text name="'admin.common.words.ok'"/> </page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <tr>
        <td>
            <ww:text name="'admin.subtasks.disable.subtasks.present.in.system'">
                <ww:param name="'value0'"><b><ww:property value="/subTaskCount"/></b></ww:param>
                <ww:param name="'value1'"><br></ww:param>
                <ww:param name="'value2'"><strong class="status-inactive"></ww:param>
                <ww:param name="'value3'"></strong></ww:param>
            </ww:text>
        </td>
    </tr>
</page:applyDecorator>

</body>
</html>

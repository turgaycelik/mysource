<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.subtasks.manage.sub.tasks'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="subtasks"/>
</head>
<body>
<header class="aui-page-header">
    <div class="aui-page-header-inner">
        <div class="aui-page-header-main">
            <h2><ww:text name="'admin.menu.globalsettings.sub.tasks'"/></h2>
        </div>
        <ww:if test="/subTasksEnabled == true">
        <div class="aui-page-header-actions">
            <nav class="aui-toolbar">
                <div class="toolbar-split toolbar-split-right">
                    <ul class="toolbar-group">
                        <li class="toolbar-item">
                            <a id="add-subtask-type" class="toolbar-trigger trigger-dialog" href="AddNewSubTaskIssueType.jspa">
                                <span class="icon jira-icon-add"></span>&nbsp;<ww:text name="'admin.subtasks.add.new.issue.type'"/>
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>
        </div>
        </ww:if>
    </div>
</header>

<page:applyDecorator name="jirapanel">
    <page:param name="width">100%</page:param>
    <ww:if test="/subTasksEnabled == true">
        <p>
        <ww:text name="'admin.subtasks.status'">
            <ww:param name="'value0'"><strong class="status-active"></ww:param>
            <ww:param name="'value1'"><ww:text name="'admin.common.words.on'"/></ww:param>
            <ww:param name="'value2'"></strong></ww:param>
        </ww:text> <ww:text name="'admin.subtasks.manage.subtasks.here'">
            <ww:param name="'value0'"><a href="<ww:url page="/secure/admin/ViewIssueTypes.jspa" />"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.subtasks.disable.sub.tasks'">
                    <ww:param name="'value0'"><a id="disable_subtasks" href="<ww:url page="DisableSubTasks!default.jspa"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </li>
            <ww:if test="/translatable == true">
            <li>
                <ww:text name="'admin.subtasks.translate.sub.tasks'">
                    <ww:param name="'value0'"><a id="translate_sub_tasks" href="<ww:url page="ViewTranslations!default.jspa" atltoken="false"><ww:param name="'issueConstantType'" value="'subTask'"/></ww:url>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </li>
            </ww:if>
            <li>
                <ww:text name="'admin.subtasks.manage.sub.tasks.2'">
                    <ww:param name="'value0'"><a href="<ww:url page="/secure/admin/ViewIssueTypes.jspa" atltoken="false"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:if>
    <ww:else>
        <p>
        <ww:text name="'admin.subtasks.status'">
            <ww:param name="'value0'"><strong class="status-inactive"></ww:param>
            <ww:param name="'value1'"><ww:text name="'admin.common.words.off'"/></ww:param>
            <ww:param name="'value2'"></strong></ww:param>
        </ww:text>
        </p>
        <ul class="optionslist">
            <li>
                <ww:text name="'admin.subtasks.enable.sub.tasks'">
                    <ww:param name="'value0'"><a id="enable_subtasks" href="<ww:url page="EnableSubTasks.jspa"/>"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </li>
        </ul>
    </ww:else>
</page:applyDecorator>

<ww:if test="/subTasksIssueTypes != null && /subTasksIssueTypes/empty == false">
    <table id="sub-task-list" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'common.words.description'"/>
                </th>
                <th>
                    <ww:text name="'admin.common.words.icon'"/>
                </th>
                <ww:if test="/subTasksEnabled == true">
                    <th>
                        <ww:text name="'admin.common.words.operation'"/>
                    </th>
                </ww:if>
            </tr>
        </thead>
        <tbody>
        <ww:property value="/subTasksIssueTypes">
        <ww:iterator value="." status="'status'">
            <tr>
                <td>
                    <b><ww:property value="string('name')"/></b><ww:if test="../default(.) == true"> <span class="secondary-text">(<ww:text name="'admin.common.words.default.small'"/>)</span></ww:if>
                </td>
                <td>
                    <ww:property value="string('description')"/>
                </td>
                <td>
                    <ww:component name="'issuetype'" template="constanticon.jsp">
                      <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                      <ww:param name="'iconurl'" value="./string('iconurl')" />
                      <ww:param name="'alt'"><ww:property value="./string('name')" /></ww:param>
                    </ww:component>
                </td>
                <ww:if test="/subTasksEnabled == true">
                    <td>
                        <ul class="operations-list">
                            <li><a id="edit_<ww:property value="string('name')"/>" href="<ww:url page="EditSubTaskIssueTypes!default.jspa" atltoken="false"><ww:param name="'id'" value="string('id')"/></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                        <%-- At least one sub-task issue type must exists - users can turn sub-tasks off if they do not want to
                        use them. Check that there is more tahn one sub-task issue type --%>
                        <ww:if test="../size > 1">
                            <li><a id="del_<ww:property value="string('name')"/>" href="<ww:url page="DeleteSubTaskIssueType!default.jspa" atltoken="false"><ww:param name="'id'" value="string('id')"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                        </ww:if>
                        </ul>
                    </td>
                </ww:if>
            </tr>
        </ww:iterator>
        </ww:property>
        </tbody>
    </table>
</ww:if>
</body>
</html>

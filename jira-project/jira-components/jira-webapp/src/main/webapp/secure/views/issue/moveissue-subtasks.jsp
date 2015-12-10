<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'moveissue.title'"/>: <ww:property value="issue/string('key')"/></title>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>
<body>
    <page:applyDecorator name="bulkops-general">
        <page:param name="pageTitle"><ww:text name="'moveissue.title'"/></page:param>
        <page:param name="navContentJsp">/secure/views/issue/moveissuepane.jsp</page:param>

            <page:applyDecorator name="jiraform">
                <page:param name="title">
                    <ww:text name="'moveissue.subtasks.issuetypes.title'"/>
                </page:param>
                <page:param name="description">
                    <ww:text name="'moveissue.subtasks.issuetypes.desc'"/>
                </page:param>
                <page:param name="columns">1</page:param>
                <page:param name="width">100%</page:param>
                <page:param name="action">MoveIssueSubtasks.jspa</page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
                <page:param name="submitId">next_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>
                <tr>
                    <td>
                        <table id="issuetypechoices" class="aui">
                            <thead>
                                <th colspan="5"><ww:text name="'moveissue.subtask.choose.issuetypes'"/></th>
                            </thead>
                            <tbody>
                            <ww:iterator value="/migrateIssueTypes">
                                <tr>
                                    <%-- Select Issue Type --%>
                                    <td class="nowrap" width="20%"><strong><ww:text name="'moveissue.currentissuetype'"/></strong>:</td>
                                    <td class="nowrap"><ww:property value="./name"/></td>
                                    <td class="nowrap"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0"></td>
                                        <%-- Target Issue Type --%>
                                    <td class="nowrap" width="20%"><strong><ww:text name="'moveissue.newissuetype'"/></strong>:</td>
                                    <td class="nowrap"><select name="<ww:property value="/prefixIssueTypeId(./id)"/>"><ww:iterator value="/projectSubtaskIssueTypes"><option value="<ww:property value="./id"/>"><ww:property value="./name"/></option></ww:iterator></select></td>
                                </tr>
                            </ww:iterator>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
            </page:applyDecorator>

    </page:applyDecorator>
</body>
</html>

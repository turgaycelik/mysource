<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'" />
<html>
<head>
	<title><ww:if test="subTask == true"><ww:text name="'movesubtask.title'"/></ww:if><ww:else><ww:text name="'moveissue.title'"/></ww:else>: <ww:property value="issue/string('key')" /></title>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>
<body>
    <page:applyDecorator name="bulkops-general">
        <page:param name="pageTitle"><ww:text name="'moveissue.title'"/></page:param>
        <page:param name="navContentJsp"><ww:if test="subTask == true">/secure/views/issue/movetaskpane.jsp</ww:if><ww:else>/secure/views/issue/moveissuepane.jsp</ww:else></page:param>

            <page:applyDecorator name="jiraform">
                <page:param name="action">MoveIssueConfirm.jspa</page:param>
                <page:param name="columns">1</page:param>
                <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
                <page:param name="submitId">move_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.move'"/></page:param>
                <page:param name="width">100%</page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <page:param name="title">
                    <ww:if test="subTask == true">
                        <ww:text name="'movesubtask.title'"/>: <ww:text name="'moveissue.confirm'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'moveissue.title'"/>: <ww:text name="'moveissue.confirm'"/>
                    </ww:else>
                </page:param>
                <page:param name="description">
                    <ww:text name="'moveissue.confirm.desc.ent'"/>
                    <ww:if test="subTasks/empty == false">
                        <p>
                            <ww:text name="'movesubtask.loss.of.data'">
                                <ww:param name="'value0'"><span class="warning"></ww:param>
                                <ww:param name="'value0'"></span></ww:param>
                            </ww:text>
                        </p>
                    </ww:if>
                </page:param>
                <tr>
                    <td>
                        <table id="move_confirm_table" class="aui">
                            <thead>
                                <tr>
                                    <th width="20%">&nbsp;</th>
                                    <th width="40%"><ww:text name="'moveissue.originalvalue'"/></th>
                                    <th width="40%"><ww:text name="'moveissue.newvalue'"/></th>
                                </tr>
                            </thead>
                            <tbody>
                            <!-- Breaking page into smaller parts - JRA-5059 -->
                            <jsp:include page="/secure/views/issue/moveissue-confirm-part1.jsp" flush="false" />

                            <%-- Show all the fields that have changed for the move --%>
                            <ww:iterator value="confimationFieldLayoutItems">
                                <tr>
                                    <td><ww:property value="/fieldName(./orderableField)" /></td>
                                    <td>
                                        <span class="status-inactive"><ww:property value="oldViewHtml(./orderableField)" escape="'false'" /></span>
                                    </td>
                                    <td>
                                        <span class="status-active"><ww:property value="newViewHtml(./orderableField)" escape="'false'" /></span>
                                    </td>
                                </tr>
                            </ww:iterator>
                            <%-- Show all the fields that will be removed --%>
                            <ww:iterator value="removeFields">
                                <tr>
                                    <td><ww:property value="/fieldName(.)" /></td>
                                    <td>
                                        <span class="status-inactive"><ww:property value="oldViewHtml(.)" escape="'false'" /></span>
                                    </td>
                                    <td>
                                        &nbsp;
                                    </td>
                                </tr>
                            </ww:iterator>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <%-- Do not put these in the MoveIssueBean --%>
                <ui:component name="'confirm'" value="'true'" template="hidden.jsp" />
                <ui:component name="'id'" template="hidden.jsp" />
            </page:applyDecorator>

    </page:applyDecorator>
</body>
</html>

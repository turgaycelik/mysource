<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%
    KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
    keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
%>
<html>
<head>
    <title><ww:text name="textKey('title')"/>: <ww:property value="issue/key" /></title>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>
<body>
    <page:applyDecorator name="bulkops-subtask">
        <page:param name="navContentJsp">/secure/views/issue/convertissuetosubtaskpane.jsp</page:param>

            <ww:if test="errorMessages/empty == true && targetWorkflow != null">
                <page:applyDecorator name="jiraform">
                    <page:param name="title">
                        <ww:text name="textKey('title')"/>: <ww:property value="issue/key" />
                    </page:param>
                    <page:param name="description">
                        <ww:text name="textKey('step2.desc.ent')">
                            <ww:param name="value0"><strong></ww:param>
                            <ww:param name="value1"></strong></ww:param>
                        </ww:text>
                    </page:param>
                    <page:param name="columns">1</page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action"><ww:property value="/actionPrefix"/>SetStatus.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><%= request.getContextPath() %>/secure/<ww:property value="/actionPrefix"/>!cancel.jspa?id=<ww:property value="issue/id" /></page:param>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:property value="text('common.forms.next')"/> &gt;&gt;</page:param>

                    <tr>
                        <td>
                            <table width="100%" class="aui wizardTable">
                                <tbody>
                                    <tr>
                                        <td width="20%">
                                            <strong><ww:text name="'convert.issue.to.subtask.selectstatus'"/>:</strong>
                                        </td>
                                        <%-- Current Issue Status --%>
                                        <td>
                                            <table width="100%">
                                                <tbody>
                                                <tr>
                                                    <td class="nowrap" width="20%">
                                                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                                            <ww:param name="'issueStatus'" value="/issue/statusObject"/>
                                                            <ww:param name="'isSubtle'" value="false"/>
                                                            <ww:param name="'isCompact'" value="false"/>
                                                        </ww:component>
                                                        <span class="secondary-text">(<strong><ww:text name="'convert.issue.to.subtask.workflow'"/></strong>:&nbsp;<ww:property value="currentWorkflow/name"/>)</span>
                                                    </td>
                                                    <td width="1%">
                                                        <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" alt=""/>
                                                    </td>
                                                    <%-- Target Status --%>
                                                    <td>
                                                        <table>
                                                            <tbody>
                                                            <tr>
                                                                <ui:select label="" name="'targetStatusId'" list="./targetWorkflow/linkedStatusObjects" value="./targetStatusId" listKey="'id'"  listValue="'nameTranslation(../..)'" theme="'single'">
                                                                    <ui:param name="'mandatory'" value="true"/>
                                                                </ui:select>
                                                                <td>
                                                                    <span class="secondary-text">(<strong><ww:text name="'convert.issue.to.subtask.workflow'"/></strong>:&nbsp;<ww:property value="targetWorkflow/name"/>)</span>
                                                                </td>
                                                            </tr>
                                                            </tbody>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>

                    <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
                    <ui:component name="'guid'" template="hidden.jsp"  theme="'single'" />
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <%-- display error message --%>
                <page:applyDecorator name="jiraform">
                    <%-- Must have body, else NullPointer is thrown --%>
                </page:applyDecorator>
            </ww:else>

    </page:applyDecorator>
</body>
</html>

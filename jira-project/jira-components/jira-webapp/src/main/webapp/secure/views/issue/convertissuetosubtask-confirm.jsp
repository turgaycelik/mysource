<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'" />
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
                    <page:param name="title"><ww:text name="textKey('title')"/>: <ww:property value="issue/key"/></page:param>
                    <page:param name="description">
                        <ww:text name="textKey('step4.desc.ent')">
                            <ww:param name="value0"><strong></ww:param>
                            <ww:param name="value1"></strong></ww:param>
                        </ww:text>
                    </page:param>
                    <page:param name="columns">1</page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action"><ww:property value="/actionPrefix" />Convert.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><%= request.getContextPath() %>/secure/<ww:property value="/actionPrefix" />!cancel.jspa?id=<ww:property value="issue/id"/></page:param>
                    <page:param name="submitId">finish_submit</page:param>
                    <page:param name="submitName"><ww:property value="text('common.forms.finish')"/></page:param>

                    <tr>
                        <td>
                            <table id="convert_confirm_table" class="aui">
                                <thead>
                                    <tr>
                                        <th width="20%">&nbsp;</th>
                                        <th width="40%"><ww:text name="textKey('originalvalue')"/></th>
                                        <th width="40%"><ww:text name="textKey('newvalue')"/></th>
                                    </tr>
                                </thead>
                                <tbody>
                                <%-- Breaking page into smaller parts - JRA-5059 --%>
                                <%-- this displays type, status and security level --%>
                                <jsp:include page="/secure/views/issue/convertissuetosubtask-confirm-part1.jsp" flush="false" />

                                <%-- Show all the fields that have changed for the move --%>
                                <ww:iterator value="convertFieldLayoutItems">
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

                    <ui:component name="'id'" template="hidden.jsp"  theme="'single'" />
                    <ui:component name="'guid'" template="hidden.jsp"  theme="'single'" />
                </page:applyDecorator>

            </ww:if>
            <ww:else>
                <%-- display error message --%>
                <page:applyDecorator name="jiraform" >
                    <%-- Must have body, else NullPointer is thrown --%>
                </page:applyDecorator>
            </ww:else>

    </page:applyDecorator>
</body>
</html>

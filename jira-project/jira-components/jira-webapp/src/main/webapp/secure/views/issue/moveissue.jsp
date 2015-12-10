<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="/issueValid == true && allowedProjects/size > 0">
        <title><ww:text name="'moveissue.title'"/>: <ww:if test="/issueValid == 'true'"><ww:property value="issue/string('key')" /></ww:if></title>
        <%
            KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true && allowedProjects/size > 0">
    <page:applyDecorator name="bulkops-general">
        <page:param name="pageTitle"><ww:text name="'moveissue.title'"/></page:param>
        <page:param name="navContentJsp">/secure/views/issue/moveissuepane.jsp</page:param>

            <ww:if test="subTask == false">
                <page:applyDecorator name="jiraform">
                    <page:param name="title"><ww:text name="'moveissue.title'"/>: <ww:property value="issue/string('key')" /> - <ww:property value="issue/string('summary')" /></page:param>
                    <page:param name="description">
                        <ww:text name="'moveissue.chooseproject.desc.ent'"/>
                    </page:param>
                    <page:param name="columns">1</page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action">MoveIssue.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false" /></page:param>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>
                    <tr>
                        <td>
                            <table class="aui">
                                <tbody>
                                    <tr class="totals">
                                        <td colspan="5"><ww:text name="'moveissue.selectproject'"/></td>
                                    </tr>
                                    <tr>
                                        <%-- Select Project --%>
                                        <td width="20%" class="nowrap"><b><ww:text name="'moveissue.currentproject'"/></b>:</td>
                                        <td class="nowrap"><ww:property value="project/string('name')"/></td>
                                        <td width="1%" class="nowrap"><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" /></td>
                                        <%-- Target Project --%>
                                        <td width="20%" class="nowrap"><b><ww:text name="'moveissue.newproject'"/></b>:</td>
                                        <ww:property value="/fieldHtml('project')" escape="'false'" />
                                    </tr>
                                    <tr class="totals">
                                        <td colspan="5"><ww:text name="'moveissue.selectissuetype'"/></td>
                                    </tr>
                                    <tr>
                                        <%-- Select Issue Type --%>
                                        <td width="20%" class="nowrap"><b><ww:text name="'moveissue.currentissuetype'"/></b>:</td>
                                        <td class="nowrap"><ww:property value="./constantsManager/issueType(currentIssueType)/string('name')"/></td>
                                        <td align="absmiddle" nowrap><img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" /></td>
                                        <%-- Target Issue Type --%>
                                        <td width="20%" class="nowrap"><b><ww:text name="'moveissue.newissuetype'"/></b>:</td>
                                        <ww:property value="/fieldHtml('issuetype')" escape="'false'" />
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <ui:component name="'id'" template="hidden.jsp"  theme="'single'" />
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <%-- Sub Tasks cannot be moved - must move parent issue. This will only happen if someone intentionally crafts
                a URL to try to do this --%>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:iterator value="flushedErrorMessages">
                            <p><ww:property /></p>
                        </ww:iterator>
                    </aui:param>
                </aui:component>
            </ww:else>

    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%-- TODO: SEAN check this error pattern --%>
        <ww:if test="hasErrorMessages == 'true'">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:if test="hasErrorMessages == 'true'">
                        <ww:iterator value="flushedErrorMessages">
                            <p><ww:property value="." /></p>
                        </ww:iterator>
                    </ww:if>
                </aui:param>
            </aui:component>
        </ww:if>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <%@ include file="/includes/noprojects.jsp" %>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>

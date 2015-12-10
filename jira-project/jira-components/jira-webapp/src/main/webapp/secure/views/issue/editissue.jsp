<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.util.JiraUtils" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <meta content="issueaction" name="decorator" />
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false"/>" />
    <title>
        <ww:text name="'editissue.title'"/>: <ww:property value="summary" /> [<ww:property value="issue/string('key')" />]
    </title>
</head>
<body>
<ww:if test="editable == true">
    <page:applyDecorator id="issue-edit" name="auiform">
        <page:param name="action"><ww:url value="'/secure/EditIssue.jspa'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.update'" /></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
        <page:param name="isMultipart">true</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'editissue.title'"/></aui:param>
        </aui:component>

        <ww:if test="/issueExists == true">

            <aui:component name="'id'" template="hidden.jsp"  theme="'aui'" />

            <ww:component template="issuefields.jsp" name="'createissue'">
                <ww:param name="'displayParams'" value="/displayParams"/>
                <ww:param name="'issue'" value="/issueObject"/>
                <ww:param name="'tabs'" value="/fieldScreenRenderTabs"/>
                <ww:param name="'errortabs'" value="/tabsWithErrors"/>
                <ww:param name="'selectedtab'" value="/selectedTab"/>
                <ww:param name="'ignorefields'" value="/ignoreFieldIds"/>
                <ww:param name="'create'" value="'false'"/>
            </ww:component>

            <jsp:include page="/includes/panels/updateissue_comment.jsp" />

        </ww:if>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <ww:if test="hasErrorMessages == 'true'">
                <ul>
                    <ww:iterator value="flushedErrorMessages">
                        <li><ww:property value="." /></li>
                    </ww:iterator>
                </ul>
            </ww:if>
            <ww:if test="remoteUser == null">
                <p><ww:text name="'editissue.notloggedin'"/></p>
                <p>
                    <ww:text name="'editissue.mustfirstlogin'">
                        <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
                        <ww:param name="'value1'"></a></ww:param>
                    </ww:text>
                    <ww:if test="extUserManagement != true">
                        <% if (JiraUtils.isPublicMode()) { %>
                            <ww:text name="'noprojects.signup'">
                                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                                <ww:param name="'value1'"></a></ww:param>
                            </ww:text>
                        <% } %>
                    </ww:if>
                </p>
            </ww:if>
            <ww:else>
                <ww:if test="hasEditIssuePermission(/issueObject) == 'true'">
                    <p><ww:text name="'editissue.error.no.edit.workflow'"/></p>
                </ww:if>
                <ww:else>
                    <p><ww:text name="'editissue.error.no.edit.permission'"/></p>
                </ww:else>
            </ww:else>
        </aui:param>
    </aui:component>
</ww:else>
</body>
</html>

<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/parentIssueKey != null && allowedProjects/size > 0">
        <title><ww:text name="'createsubtaskissue.title'"/></title>
        <meta name="decorator" content="panel-general" />
        <link rel="index" href="<ww:url value="/parentIssuePath" atltoken="false"/>" />
        <%
            KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        %>
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/parentIssueKey != null && allowedProjects/size > 0">
    <page:applyDecorator id="subtask-create-start" name="auiform">
        <page:param name="action">CreateSubTaskIssue.jspa</page:param>
        <page:param name="submitButtonName">Create</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.next'" /></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/parentIssuePath" atltoken="false"/></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'createsubtaskissue.title'"/></aui:param>
        </aui:component>

        <aui:component id="'project'" name="'pid'" template="hidden.jsp" theme="'aui'">
            <aui:param name="'cssClass'">project-field-readonly</aui:param>
        </aui:component>

        <aui:component name="'parentIssueId'" template="hidden.jsp" theme="'aui'" />

        <ww:property value="/field('issuetype')/createHtml(null, /, /, /issueObject, /displayParams)" escape="'false'" />

    </page:applyDecorator>
</ww:if>
<ww:elseIf test="/parentIssueKey != null">
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <%@ include file="/includes/noprojects.jsp" %>
            </aui:param>
        </aui:component>
    </div>
</ww:elseIf>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'issue.service.issue.wasdeleted'"/></p>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>

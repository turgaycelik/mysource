<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/issueExists == true && /hasIssuePermission('delete', /issue) == true ">
        <title><ww:text name="'deleteissue.title'"/></title>
        <meta name="decorator" content="issueaction"/>
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
<ww:if test="/issueExists == true && /hasIssuePermission('delete', /issue) == true ">
    <page:applyDecorator id="delete-issue" name="auiform">
        <page:param name="action"><ww:url value="'/secure/DeleteIssue.jspa'" atltoken="false"/></page:param>
        <page:param name="showHint">true</page:param>
        <ww:property value="/hint('delete_issue')">
            <ww:if test=". != null">
                <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
            </ww:if>
        </ww:property>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="submitButtonText"><ww:text name="'deleteissue.submitname'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:text name="'deleteissue.title'"/></aui:param>
            <aui:param name="'subtaskTitle'"><ww:text name="'deleteissue.title.subtask'"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="/cameFromParent"/>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'deleteissue.desc.line1'"/></p>
                <p><ww:text name="'deleteissue.desc.line2'"/></p>
                <p><ww:text name="'deleteissue.desc.line3'"/></p>
                <ww:if test="/numberOfSubTasks > 0 ">
                    <p>
                        <ww:text name="'deleteissue.subtask.warning'">
                        <ww:param name="'value0'"><span class="warning"></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                        <ww:param name="'value2'"><ww:property value="/numberOfSubTasks"/></ww:param>
                    </ww:text>
                    </p>
                </ww:if>
            </aui:param>
        </aui:component>

        <aui:component name="'id'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'viewIssueKey'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'confirm'"  value="'true'" template="hidden.jsp" theme="'aui'" />
        <aui:component id="'return-url'" name="'targetUrl'" template="hiddenUrl.jsp" theme="'aui'" />

    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/issue/generic-errors.jsp" %>
    </div>
</ww:else>
</body>
</html>
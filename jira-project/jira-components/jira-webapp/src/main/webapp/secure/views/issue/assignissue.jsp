<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/issueValid == true && /hasIssuePermission('assign', /issue) == true && /workflowAllowsEdit(/issueObject) == true">
        <title><ww:text name="'assign.title'"/></title>
        <meta name="decorator" content="issueaction" />
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
<ww:if test="/issueValid == true && /hasIssuePermission('assign', /issue) == true && /workflowAllowsEdit(/issueObject) == true">
    <page:applyDecorator id="assign-issue" name="auiform">
        <page:param name="action">AssignIssue.jspa</page:param>
        <page:param name="showHint">true</page:param>
        <ww:property value="/hint('assign')">
            <ww:if test=". != null">
                <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
            </ww:if>
        </ww:property>
        <page:param name="submitButtonName">Assign</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.assign'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false" /></page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:text name="'assign.title'"/></aui:param>
            <aui:param name="'subtaskTitle'"><ww:text name="'assign.title.subtask'"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="/cameFromParent"/>
        </aui:component>

        <aui:component name="'id'" template="hidden.jsp" theme="'aui'"  />

        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'assign.details.legend'" /></page:param>

            <ww:property value="/field('assignee')/editHtml(null, /, /, ./issueObject, /displayParams)" escape="'false'" />

            <%@ include file="/includes/panels/updateissue_comment.jsp" %>

        </page:applyDecorator>

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

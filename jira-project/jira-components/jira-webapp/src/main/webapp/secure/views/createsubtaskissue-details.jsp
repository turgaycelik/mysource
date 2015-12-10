<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="!/requiresLogin || /requiresLogin == false">
        <title><ww:text name="'createsubtaskissue.title'"/></title>
        <meta name="decorator" content="panel-general" />
        <link rel="index" href="<ww:url value="/parentIssuePath" atltoken="false" />" />
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
<ww:if test="!/requiresLogin || /requiresLogin == false">
    <page:applyDecorator id="subtask-create-details" name="auiform">
        <page:param name="action">CreateSubTaskIssueDetails.jspa</page:param>
        <page:param name="submitButtonName">Create</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.create'" /></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/parentIssuePath" atltoken="false"/></page:param>
        <page:param name="isMultipart">true</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'createsubtaskissue.title'"/></aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:component id="'project-name'" label="text('issue.field.project')" name="'project/string('name')'" template="formFieldValue.jsp" theme="'aui'" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:component id="'issue-type'" label="text('issue.field.issuetype')" name="'issueType'" template="formIssueType.jsp" theme="'aui'">
                <aui:param name="'issueType'" value="/constantsManager/issueType(issuetype)" />
            </aui:component>
        </page:applyDecorator>

        <ww:component template="issuefields.jsp" name="'createissue'">
            <ww:param name="'displayParams'" value="/displayParams"/>
            <ww:param name="'issue'" value="/issueObject"/>
            <ww:param name="'tabs'" value="/fieldScreenRenderTabs"/>
            <ww:param name="'errortabs'" value="/tabsWithErrors"/>
            <ww:param name="'selectedtab'" value="/selectedTab"/>
            <ww:param name="'ignorefields'" value="/ignoreFieldIds"/>
            <ww:param name="'create'" value="'true'"/>
        </ww:component>

        <aui:component name="'issuetype'" template="hidden.jsp" theme="'aui'"  />
        <aui:component name="'viewIssueKey'" template="hidden.jsp" theme="'aui'"  />
        <aui:component name="'pid'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'parentIssueId'" template="hidden.jsp" theme="'aui'" />
    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/createissue-notloggedin.jsp" %>
    </div>
</ww:else>
</body>
</html>

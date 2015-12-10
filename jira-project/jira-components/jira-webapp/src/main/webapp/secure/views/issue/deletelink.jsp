<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="/issueValid == true">
        <title><ww:text name="'viewissue.delete.link.title'"/> <ww:property value="del" /></title>
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
<ww:if test="/issueValid == true && /hasIssuePermission('link', /issue) == true && /hasErrorMessages == false">
        <page:applyDecorator id="issue-link-delete" name="auiform">
            <ww:if test="/remoteIssueLink == true">
                <page:param name="action"><ww:url page="DeleteRemoteIssueLink.jspa"><ww:param name="'id'" value="id"/><ww:param name="'remoteIssueLinkId'" value="remoteIssueLinkId"/></ww:url></page:param>
            </ww:if>
            <ww:else>
                <page:param name="action"><ww:url page="DeleteLink.jspa"><ww:param name="'id'" value="id"/><ww:param name="'destId'" value="destId"/><ww:param name="'sourceId'" value="sourceId"/><ww:param name="'linkType'" value="linkType"/></ww:url></page:param>
            </ww:else>
            <page:param name="submitButtonName">Delete</page:param>
            <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
            <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>


            <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
                <ww:if test="/remoteIssueLink == true">
                    <aui:param name="'title'"><ww:text name="'viewissue.delete.link.title'"/>: <ww:property value="linkTitle" escape="false"/></aui:param>
                </ww:if>
                <ww:else>
                    <aui:param name="'title'"><ww:text name="'viewissue.delete.link.title'"/>: <ww:property value="/issue/string('key')" escape="false"/> <ww:property value="directionName" escape="false" /> <ww:property value="targetIssueKey" escape="false"/></aui:param>
                </ww:else>
                <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
                <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
                <aui:param name="'cameFromParent'" value="/cameFromParent"/>
            </aui:component>

            <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'viewissue.delete.link.msg'"/></p>
                </aui:param>
            </aui:component>

            <aui:component name="'confirm'" value="true" template="hidden.jsp" theme="'aui'"  />
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
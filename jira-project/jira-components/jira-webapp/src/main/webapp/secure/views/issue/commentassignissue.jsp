<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="/issueValid == true">
        <meta content="issueaction" name="decorator" />
        <title><ww:property value="/i18nTextViaMetaAttr('jira.i18n.title',actionDescriptor/name)"/> [<ww:property value="issue/string('key')" />]</title>
        <%
            KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message"/>
    </ww:else>
</head>
<body>
    <ww:if test="/issueValid == true">
        <page:applyDecorator id="issue-workflow-transition" name="auiform">
            <page:param name="action"><ww:url value="'/secure/CommentAssignIssue.jspa'"/></page:param>
            <page:param name="submitButtonName">Transition</page:param>
            <page:param name="showHint">true</page:param>
            <ww:property value="/hint('transition')">
                <ww:if test=". != null">
                    <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                    <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
                </ww:if>
            </ww:property>
            <page:param name="submitButtonText"><ww:property value="/i18nTextViaMetaAttr('jira.i18n.submit', actionDescriptor/name)" escape="false" /></page:param>
            <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>
            <page:param name="isMultipart">true</page:param>

            <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
                <aui:param name="'title'"><ww:property value="/i18nTextViaMetaAttr('jira.i18n.title',actionDescriptor/name)" escape="false"/></aui:param>
                <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
                <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
                <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
                <aui:param name="'cameFromParent'" value="false"/>
            </aui:component>

            <ww:if test="/i18nTextViaMetaAttr('jira.i18n.description', actionDescriptor/metaAttributes/('description'))/length != 0">
                <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
                    <aui:param name="'messageHtml'">
                        <p><ww:property value="/i18nTextViaMetaAttr('jira.i18n.description', actionDescriptor/metaAttributes/('description'))"/></p>
                        <ww:if test="actionDescriptor/metaAttributes/('jira.i18n.description')">
                            <ww:if test="actionDescriptor/metaAttributes/('jira.i18n.description.2')">
                                <p><ww:text name="actionDescriptor/metaAttributes/('jira.i18n.description.2')"/></p>
                            </ww:if>
                            <ww:if test="actionDescriptor/metaAttributes/('jira.i18n.description.3')">
                                <p><ww:text name="actionDescriptor/metaAttributes/('jira.i18n.description.3')"/></p>
                            </ww:if>
                         </ww:if>
                    </aui:param>
                </aui:component>
            </ww:if>

            <aui:component name="'action'" template="hidden.jsp"  theme="'aui'" />
            <aui:component name="'id'" template="hidden.jsp"  theme="'aui'" />
            <aui:component name="'viewIssueKey'" template="hidden.jsp"  theme="'aui'" />

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

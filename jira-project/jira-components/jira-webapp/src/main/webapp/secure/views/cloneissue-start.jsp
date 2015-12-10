<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/issueValid == true && /originalIssue != null && /hasIssuePermission('create', /issue) == true">
        <title><ww:text name="'cloneissue.title'"/></title>
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
<ww:if test="/issueValid == true && /originalIssue != null && /hasIssuePermission('create', /issue) == true">
    <page:applyDecorator id="assign-issue" name="auiform">
        <page:param name="action">CloneIssueDetails.jspa</page:param>
        <page:param name="submitButtonName">Create</page:param>
        <page:param name="showHint">true</page:param>
         <ww:property value="/hint('clone')">
            <ww:if test=". != null">
                <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
            </ww:if>
        </ww:property>
        <page:param name="submitButtonText"><ww:text name="'cloneissue.create'" /></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false"/></page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:text name="'cloneissue.title'"/></aui:param>
            <aui:param name="'subtaskTitle'"><ww:text name="'cloneissue.title.subtask'"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="/cameFromParent"/>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'cloneissue.step1.desc'" /></p>
            </aui:param>
        </aui:component>
        <%-- if there is no 'clone' link type in the system, print a warning --%>
        <ww:if test="displayCloneLinkWarning == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'cloneissue.linktype.does.not.exist'">
                            <ww:param name="value0" value="cloneLinkTypeName" />
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
        <%-- if the user cannot modify the reporter, print a warning --%>
        <ww:if test="canModifyReporter == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'cloneissue.reporter.modify'" /></p>
                </aui:param>
            </aui:component>
        </ww:if>

        <aui:component name="'id'" template="hidden.jsp" theme="'aui'"  />

        <ww:if test="/originalIssue">

            <page:applyDecorator name="auifieldset">
                <page:param name="legend"><ww:text name="'cloneissue.clone.options.legend'" /></page:param>

                <ww:property value="/fieldScreenRenderLayoutItem('summary')/createHtml(/, /, /issueObject, /displayParams)" escape="'false'" />

                <ww:if test="/displayCopySubTasks == true || /displayCopyAttachments == true || /displayCopyLink == true">
                    <page:applyDecorator name="auifieldset">
                        <page:param name="type">group</page:param>

                        <ww:if test="/displayCopySubTasks == true">
                            <page:applyDecorator name="auifieldgroup">
                                <page:param name="type">checkbox</page:param>

                                <aui:checkbox id="'clone-subtasks'" fieldValue="'true'" label="text('cloneissue.clone.subtasks.label')" name="'cloneSubTasks'" theme="'aui'" />
                            </page:applyDecorator>
                        </ww:if>

                        <ww:if test="/displayCopyAttachments == true">
                            <page:applyDecorator name="auifieldgroup">
                                <page:param name="type">checkbox</page:param>

                                <aui:checkbox id="'clone-attachments'" fieldValue="'true'" label="text('cloneissue.clone.attachments.label')" name="'cloneAttachments'" theme="'aui'" />
                            </page:applyDecorator>
                        </ww:if>

                        <ww:if test="/displayCopyLink == true">
                            <page:applyDecorator name="auifieldgroup">
                                <page:param name="type">checkbox</page:param>

                                <aui:checkbox id="'clone-links'" fieldValue="'true'" label="text('cloneissue.clone.issuelinks.label')" name="'cloneLinks'" theme="'aui'" />
                            </page:applyDecorator>
                        </ww:if>
                    </page:applyDecorator>
                </ww:if>
            </page:applyDecorator>
        </ww:if>
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

<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/issueValid == true && /hasIssuePermission('attach', /issue) == true && /workflowAllowsEdit(/issueObject) == true">
        <title><ww:text name="'attachfile.title.multiple'"/></title>
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
<ww:if test="/issueValid == true && /hasIssuePermission('attach', /issue) == true && /workflowAllowsEdit(/issueObject) == true">
    <page:applyDecorator id="attach-file" name="auiform">
        <page:param name="action">AttachFile.jspa</page:param>
        <page:param name="showHint">true</page:param>
        <ww:property value="/hint('attach')">
            <ww:if test=". != null">
                <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
            </ww:if>
        </ww:property>
        <page:param name="submitButtonName">Attach</page:param>
        <page:param name="submitButtonText"><ww:text name="'attachfile.submitname'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false" /></page:param>
        <page:param name="isMultipart">true</page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:text name="'attachfile.title.multiple'"/></aui:param>
            <aui:param name="'subtaskTitle'"><ww:text name="'attachfile.title.multiple.subtask'"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="/cameFromParent"/>
        </aui:component>

        <aui:component name="'id'" template="hidden.jsp" theme="'aui'"/>
        <aui:component name="'formToken'" template="hidden.jsp" theme="'aui'" />

        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'attachfile.attachment.label'"/></page:param>
            <page:param name="type">group</page:param>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="cssClass">file-input-list</page:param>
                <page:param name="description"><ww:text name="'attachfile.filebrowser.warning'"><ww:param name="'value0'" value="/maxSizePretty"/></ww:text></page:param>

                <ww:iterator value="/temporaryAttachments">
                    <page:applyDecorator name="auifieldgroup">
                    <aui:checkbox label="./filename" name="'filetoconvert'" fieldValue="./id" theme="'aui'">
                        <ww:if test="/fileToConvertChecked(./id) == true">
                            <aui:param name="'checked'" value="'checked'"/>
                        </ww:if>
                    </aui:checkbox>
                    </page:applyDecorator>
                </ww:iterator>
                <page:applyDecorator name="auifieldgroup">
                    <aui:component label="''" name="'tempFilename'" template="inputFile.jsp" theme="'aui'" />
                    <div id='attach-max-size' class="hidden"><ww:property value="/maxSize"/></div>
                    <page:param name="cssClass"><ww:property value="@fileInputCssClass"/></page:param>
                </page:applyDecorator>
            </page:applyDecorator>

        </page:applyDecorator>

        <%@include file="/includes/panels/updateissue_comment.jsp" %>

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

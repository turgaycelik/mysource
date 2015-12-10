<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="/issueValid == true && /hasIssuePermission('work', /issue) == true && /timeTrackingFieldHidden(/issueObject) == false && /workflowAllowsEdit(/issueObject) == true">
        <title><ww:text name="'worklog.delete.title'"/></title>
        <meta name="decorator" content="issueaction" />
        <%
            KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
        %>
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false"/>" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true && /hasIssuePermission('work', /issue) == true && /timeTrackingFieldHidden(/issueObject) == false && /workflowAllowsEdit(/issueObject) == true">
    <page:applyDecorator id="delete-log-work" name="auiform">
        <page:param name="action"><ww:property value="/actionName"/>.jspa</page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="showHint">true</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelLinkURI"><ww:if test="/issueValid == true"><ww:url value="/issuePath" atltoken="false"/></ww:if></page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:text name="'worklog.delete.title'"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="/cameFromParent"/>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <ww:text name="'worklog.delete.desc'"/>
            </aui:param>
        </aui:component>

        <aui:component name="'worklogId'" template="hidden.jsp" theme="'aui'" value="/worklogId" />
        <aui:component name="'id'" template="hidden.jsp" theme="'aui'"/>

        <page:applyDecorator name="auifieldset">
            <page:applyDecorator name="auifieldset">
                <page:param name="type">group</page:param>
                <page:param name="legend"><ww:text name="'common.concepts.remaining.estimate'"/></page:param>

                <%--         Radio 1           --%>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>
                    <page:param name="description"><ww:text name="'logwork.bullet1.autoadjust.desc'"/></page:param>

                    <%-- Set the checked state of the radio --%>
                    <ww:if test="adjustEstimate == 'auto'"><ww:property id="adjust-estimate-auto-checked" value="'true'"/></ww:if>
                    <aui:radio checked="@adjust-estimate-auto-checked" id="'adjust-estimate-auto'" label="text('logwork.bullet1.adjust.automatically')" list="null" name="'adjustEstimate'" theme="'aui'" value="'auto'"/>
                </page:applyDecorator>

                <%--         Radio 2           --%>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>

                    <%-- Conditionally set the content of the label to a variable we can insert into the component attribute --%>
                    <ww:if test="estimate==null"><ww:property id="label-estimate-leave" value="text('logwork.bullet2.leave.unset')"/></ww:if>
                    <ww:else><ww:property id="label-estimate-leave" value="text('logwork.bullet2.use.existing.estimate', estimate)"/></ww:else>
                    <%-- Set the checked state of the radio --%>
                    <ww:if test="adjustEstimate == 'leave'"><ww:property id="adjust-estimate-leave-checked" value="'true'"/></ww:if>
                    <aui:radio checked="@adjust-estimate-leave-checked" id="'adjust-estimate-leave'" label="@label-estimate-leave" list="null" name="'adjustEstimate'" theme="'aui'" value="'leave'"/>
                </page:applyDecorator>

                <%--         Radio 3           --%>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>

                    <%-- Set the checked state of the radio --%>
                    <ww:if test="adjustEstimate == 'new'"><ww:property id="adjust-estimate-new-checked" value="'true'"/></ww:if>
                    <aui:radio checked="@adjust-estimate-new-checked" id="'adjust-estimate-new'" label="text('logwork.bullet3.set.to')" list="null" name="'adjustEstimate'" theme="'aui'" value="'new'"/>
                    <aui:textfield id="'adjust-estimate-new-value'" label="''" name="'newEstimate'" size="'short'" theme="'aui'" value="/newEstimate"/>
                    <span class="aui-form example"><ww:text name="'logwork.example'"><ww:param value="'3w 4d 12h'"/></ww:text></span>
                </page:applyDecorator>

                <%--         Radio 4           --%>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>

                    <ww:if test="adjustEstimate == 'manual'"><ww:property id="adjust-estimate-manual-checked" value="true"/></ww:if>
                    <aui:radio checked="@adjust-estimate-manual-checked" id="'adjust-estimate-manual'" label="text('logwork.bullet4.increaseestimate')" list="null" name="'adjustEstimate'" theme="'aui'" value="'manual'"/>
                    <aui:textfield id="'adjust-estimate-manual-value'" label="''" name="'adjustmentAmount'" size="'short'" theme="'aui'" value="/adjustmentAmount"/>
                    <span class="aui-form example"><ww:text name="'logwork.example'"><ww:param value="'3w 4d 12h'"/></ww:text></span>
                </page:applyDecorator>

            </page:applyDecorator>

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
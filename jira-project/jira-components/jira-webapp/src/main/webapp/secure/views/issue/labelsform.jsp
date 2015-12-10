<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="/issueValid == true && /hasIssuePermission('edit', /issue) == true && /workflowAllowsEdit(/issueObject) == true">
        <title><ww:text name="'label.edit.title'"/></title>
        <meta name="decorator" content="issueaction" />
        <%
            final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
            fieldResourceIncluder.includeFieldResourcesForCurrentUser();

            final KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
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
<ww:if test="/issueValid == true && /hasIssuePermission('edit', /issue) == true && /workflowAllowsEdit(/issueObject) == true">
    <page:applyDecorator name="auiform">
        <page:param name="cssClass">edit-labels</page:param>
        <page:param name="id">edit-labels-form</page:param>
        <page:param name="action"><ww:url value="'/secure/EditLabels.jspa'"/></page:param>
        <page:param name="showHint">true</page:param>
        <ww:property value="/hint('labels')">
                <ww:if test=". != null">
                    <page:param name="hint"><ww:property value="./text" escape="false" /></page:param>
                    <page:param name="hintTooltip"><ww:property value="./tooltip" escape="false" /></page:param>
                </ww:if>
            </ww:property>
        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">edit-labels-submit</page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false" /></page:param>

        <aui:component template="issueFormHeading.jsp" theme="'aui/dialog'">
            <aui:param name="'title'"><ww:property value="/fieldName"  escape="false"/></aui:param>
            <aui:param name="'issueKey'"><ww:property value="/issueObject/key" escape="false"/></aui:param>
            <aui:param name="'issueSummary'"><ww:property value="/issueObject/summary" escape="false"/></aui:param>
            <aui:param name="'cameFromSelf'" value="/cameFromIssue"/>
            <aui:param name="'cameFromParent'" value="false"/>
        </aui:component>

        <aui:component theme="'aui'" template="hidden.jsp" name="'id'" value="/id"/>
        <ww:if test="/customFieldId != null">
            <aui:component theme="'aui'" template="hidden.jsp" name="'customFieldId'" value="/customFieldId"/>
        </ww:if>
        <aui:component theme="'aui'" template="hidden.jsp" name="'noLink'" value="/noLink"/>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="cssClass">aui-field-labelpicker</page:param>
            <aui:component theme="'aui'" template="labelsSelect.jsp" label="text('issue.field.labels')">
                <aui:param name="'id'"><ww:property value="/domId"/></aui:param>
                <aui:param name="'issueId'"><ww:property value="/id"/></aui:param>
                <aui:param name="'labels'" value="/existingLabels"/>
                <aui:param name="'errorCollectionKey'" value="/errorCollectionKey" />
            </aui:component>
        </page:applyDecorator>
        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:applyDecorator name="auifieldgroup">
               <ww:checkbox id="send-notifications" theme="'aui'" label="text('label.edit.sendnotification')" name="'sendNotification'" fieldValue="'true'"/>
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


<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'" />
<html>
<head>
	<title><ww:if test="subTask == true"><ww:text name="'movesubtask.title'"/></ww:if><ww:else><ww:text name="'moveissue.title'"/></ww:else>: <ww:property value="issue/string('key')" /></title>
    <%
        KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
        keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
    %>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>
<body>
    <page:applyDecorator name="bulkops-general">
        <page:param name="pageTitle"><ww:text name="'moveissue.title'"/></page:param>
        <page:param name="navContentJsp"><ww:if test="subTask == true">/secure/views/issue/movetaskpane.jsp</ww:if><ww:else>/secure/views/issue/moveissuepane.jsp</ww:else></page:param>

            <page:applyDecorator name="jiraform">
                <page:param name="title">
                    <ww:if test="subTask == true">
                        <ww:text name="'movesubtask.title'"/>: <ww:text name="'moveissue.update.fields'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'moveissue.title'"/>: <ww:text name="'moveissue.update.fields'"/>
                    </ww:else>
                </page:param>
                <page:param name="description">
                    <ww:if test="subTask == true">
                        <%-- Sub Task --%>
                        <p><ww:text name="'movesubtask.step2.desc'"/></p>
                    </ww:if>
                    <ww:else>
                        <p><ww:text name="'moveissue.updatefields.desc.ent'"/></p>
                        <%-- If current statues of issue and all subtasks exist in the target workflows - these statuses will remain in effect --%>
                        <%-- This means that step 2 of move process has been skipped --%>
                        <ww:if test="statusChangeRequired == false">
                            <p>
                            <span class="red-highlight"><b><ww:text name="'common.words.note'"/></b></span>:&nbsp;<ww:text name="'moveissue.step2.notrequired'"/>.
                            </p>
                        </ww:if>
                    </ww:else>
                </page:param>
                <page:param name="action">MoveIssueUpdateFields.jspa</page:param>
                <page:param name="width">100%</page:param>
                <page:param name="autoSelectFirst">false</page:param>
                <page:param name="cancelURI"><ww:url value="issuePath" atltoken="false" /></page:param>
                <page:param name="submitId">next_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>

                <ww:if test="moveFieldLayoutItems != null && moveFieldLayoutItems/empty == false">
                    <ww:iterator value="moveFieldLayoutItems">
                        <ww:property value="/fieldHtml(.)" escape="'false'" />
                    </ww:iterator>
                </ww:if>
                <ww:else>
                    <%-- We still need to go through this action and this step in the move issue wizard, as during the doValidation stage of this action errors can be detected.
                    For example, if a field is required but cannot be set by the user because of permissions, the error will be detected here. Later need to refactor that the step is skipped,
                    however, the doValidation() of the action still needs to be executed. --%>
                    <tr><td><ww:text name="'moveissue.step3.nofieldsneedupdate'"/></td></tr>
                    <page:param name="columns">1</page:param>
                </ww:else>

                <%-- Do not put these in the MoveIssueBean --%>
                <ui:component name="'id'" template="hidden.jsp" theme="'single'" />

            </page:applyDecorator>

    </page:applyDecorator>
</body>
</html>

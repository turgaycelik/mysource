<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="webwork" prefix="iterator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'"/>
 <%
     final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
     fieldResourceIncluder.includeFieldResourcesForCurrentUser();

     KeyboardShortcutManager keyboardShortcutManager = ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
     keyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
%>
<html>
<head>
    <title><ww:text name="textKey('title')"/>: <ww:property value="issue/key"/></title>
    <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
</head>
<body>
    <page:applyDecorator name="bulkops-subtask">
        <page:param name="navContentJsp">/secure/views/issue/convertissuetosubtaskpane.jsp</page:param>

            <ww:if test="errorMessages/empty == true">
                <page:applyDecorator name="jiraform">
                    <page:param name="title">
                        <ww:text name="textKey('title')"/>: <ww:property value="issue/key"/>
                    </page:param>
                    <page:param name="description">
                        <ww:text name="textKey('step3.desc.ent')">
                            <ww:param name="value0"><strong></ww:param>
                             <ww:param name="value1"></strong></ww:param>
                         </ww:text>
                        <%-- This means that step 2 of move process has been skipped --%>
                        <ww:if test="statusChangeRequired == false">
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">info</aui:param>
                                <aui:param name="'messageHtml'">
                                    <p>
                                        <strong><ww:text name="'common.words.note'"/></strong>: <ww:text name="textKey('step2.notrequired')"/>.
                                    </p>
                                </aui:param>
                            </aui:component>
                        </ww:if>
                    </page:param>
                    <page:param name="width">100%</page:param>
                    <page:param name="action"><ww:property value="/actionPrefix"/>UpdateFields.jspa</page:param>
                    <page:param name="autoSelectFirst">false</page:param>
                    <page:param name="cancelURI"><%= request.getContextPath() %>/secure/<ww:property value="/actionPrefix"/>!cancel.jspa?id=<ww:property value="issue/id"/></page:param>
                    <page:param name="submitId">next_submit</page:param>
                    <page:param name="submitName"><ww:property value="text('common.forms.next')"/> &gt;&gt;</page:param>

                    <ww:if test="convertFieldLayoutItems/empty == false">
                        <ww:iterator value="convertFieldLayoutItems">
                            <ww:property value="/fieldHtml(.)" escape="'false'"/>
                        </ww:iterator>
                        <page:param name="columns">2</page:param>
                    </ww:if>
                    <ww:else>
                    <%-- We still need to go through this action and this step in the move issue wizard, as during the doValidation stage of this action errors can be detected.
                        For example, if a field is required but cannot be set by the user becuase of permissions, the error will be detected here. Later need to refactor that the step is skipped,
                        however, the doValidation() of the action still needs to be executed. --%>
                        <tr>
                            <td>
                                <ww:text name="textKey('step3.nofieldsneedupdate')"/>
                            </td>
                        </tr>
                        <page:param name="columns">1</page:param>
                    </ww:else>

                    <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>
                    <ui:component name="'guid'" template="hidden.jsp"  theme="'single'" />
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <page:applyDecorator name="jiraform">
                    <%-- Must have body, else NullPointer is thrown --%>
                </page:applyDecorator>
            </ww:else>

    </page:applyDecorator>
</body>
</html>

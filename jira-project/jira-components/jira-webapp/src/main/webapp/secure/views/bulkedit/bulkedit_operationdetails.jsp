<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <!-- Step 3 - Bulk Operation: Operation Details -->
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'id'" value="'stepped-process'" />
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelNav'">
                <ui:param name="'content'">
                    <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
                </ui:param>
            </ui:soy>
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
                        <ui:param name="'content'">
                            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                                <ui:param name="'content'">
                                    <h2><ww:text name="'bulkedit.step3'"/>: <ww:text name="'bulkedit.step3.title'"/></h2>
                                </ui:param>
                            </ui:soy>
                        </ui:param>
                    </ui:soy>

                    <!-- check for EDIT_ISSUE permissions and show an appropriate error message if user does not have this permission -->
                    <ww:if test="hasAvailableActions == false">
                        <ww:if test="/bulkEditBean/multipleProjects == true">
                            <p>
                                <ww:text name="'bulkedit.step2.note.noactions.multiple'">
                                    <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssues/size"/></b></ww:param>
                                    <ww:param name="'value1'"><b><ww:property value="/bulkEditBean/projectIds/size"/></b></ww:param>
                                </ww:text>
                            </p>
                        </ww:if>
                        <ww:else>
                            <p>
                                <ww:text name="'bulkedit.step2.note.noactions.single'">
                                    <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssues/size"/></b></ww:param>
                                    <ww:param name="'value1'"><b><ww:property value="/bulkEditBean/project/string('name')"/></b></ww:param>
                                </ww:text>
                            </p>
                        </ww:else>
                    </ww:if>
                    <ww:else>
                        <p>
                            <ww:text name="'bulkedit.step2.desc'">
                                <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssues/size"/></b></ww:param>
                            </ww:text>
                        </p>
                    </ww:else>

                    <page:applyDecorator id="bulkedit" name="auiform">
                        <page:param name="action">BulkEditDetailsValidation.jspa</page:param>
                        <page:param name="cssClass">top-label</page:param>
                        <page:param name="useCustomButtons">true</page:param>

                        <ww:if test="visibleActions/empty == 'false'">
                            <table id="availableActionsTable">
                                <ww:iterator value="visibleActions">
                                    <tr class="availableActionRow">
                                        <td class="cell-type-collapsed">
                                            <input class="checkbox" type="checkbox" id="cb<ww:property value="./field/id"/>" name="actions" value="<ww:property value="./field/id"/>" <ww:if test="/checked(./field/id) == true">checked</ww:if>>
                                        </td>
                                        <td style="max-width: 200px; width: 200px; word-wrap: break-word;">
                                            <label for="cb<ww:property value="./field/id"/>">
                                            <ww:text name="'bulkedit.actions.changefield'">
                                                <ww:param name="'value0'" value="./fieldName"/>
                                            </ww:text>
                                            </label>
                                        </td>
                                        <ww:property value="/fieldHtml(./field/id)" escape="'false'" />
                                    </tr>
                                </ww:iterator>
                            </table>
                        </ww:if>
                        <ww:if test="hiddenActions/empty == 'false'">
                            <div id="unavailableActionsTable" class="twixi-block collapsed">
                                <div class="twixi-trigger">
                                    <h5><span class="icon icon-twixi"></span><ww:text name="'bulkedit.actions.show.unavailable'"/></h5>
                                </div>
                                <div class="twixi-content">
                                    <ul>
                                        <ww:iterator value="hiddenActions">
                                            <li>
                                                <ww:text name="'bulkedit.actions.changefield'">
                                                    <ww:param name="'value0'"><ww:property value="./fieldName" /></ww:param>
                                                </ww:text>
                                                <div class="description">
                                                    <ww:text name="unavailableMessage">
                                                        <ww:param name="'value0'"><span class="highlight"></ww:param>
                                                        <ww:param name="'value1'"></span></ww:param>
                                                    </ww:text>
                                                </div>
                                            </li>
                                        </ww:iterator>
                                    </ul>
                                </div>
                            </div>
                        </ww:if>
                        <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications.jsp"/>
                        <%@include file="bulkchooseaction_submit_buttons.jsp"%>
                        <!-- Hidden field placed here so as not affect the buttons -->
                        <ww:if test="/canDisableMailNotifications() == false">
                            <ui:component name="'sendBulkNotification'" template="hidden.jsp" theme="'single'" value="'true'" />
                        </ww:if>
                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>

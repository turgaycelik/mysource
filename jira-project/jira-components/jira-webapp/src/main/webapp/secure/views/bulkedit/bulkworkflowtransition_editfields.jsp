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
    <title><ww:text name="'bulkworkflowtransition.title'"/></title>
</head>
<body>
    <!-- Step 3 - Bulk Operation: Operation Details -->
    <page:applyDecorator name="bulkpanel" >
        <page:param name="title"><ww:text name="'bulkworkflowtransition.title'"/>: <ww:text name="'bulkworkflowtransition.edit.fields'"/></page:param>
        <page:param name="action">BulkWorkflowTransitionEditValidation.jspa</page:param>
        <ww:property value="'true'" id="hideSubMenu" />
        <page:param name="instructions">
            <p><ww:text name="'bulkworkflowtransition.fields.available.edit'"/></p>
        </page:param>

            <ul class="item-details bulk-details">
                <li>
                    <dl>
                        <dt><ww:text name="'bulkworkflowtransition.issue.workflow'"/></dt>
                        <dd><ww:property value="/bulkEditBean/selectedWFTransitionKey/workflowName" /></dd>
                    </dl>
                </li>
                <li>
                    <dl>
                        <dt><ww:text name="'bulkworkflowtransition.selected.transition'"/></dt>
                        <dd><ww:property value="/bulkWorkflowTransitionOperation/actionDescriptor(/bulkEditBean/selectedWFTransitionKey)/name" /></dd>
                    </dl>
                </li>
                <li>
                    <dl>
                        <dt><ww:text name="'bulkworkflowtransition.status.transition'"/></dt>
                        <dd>
                            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                <ww:param name="'issueStatus'" value="/originStatusObject(/bulkEditBean/selectedWFTransitionKey)"/>
                                <ww:param name="'isSubtle'" value="false"/>
                                <ww:param name="'isCompact'" value="false"/>
                            </ww:component>
                            <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0" alt="" align=absmiddle>
                            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                <ww:param name="'issueStatus'" value="/destinationStatusObject(/bulkEditBean/selectedWFTransitionKey)"/>
                                <ww:param name="'isSubtle'" value="false"/>
                                <ww:param name="'isCompact'" value="false"/>
                            </ww:component>
                        </dd>
                    </dl>
                </li>
            </ul>
            <p class="bulk-affects">
                <ww:text name="'bulkworkflowtransition.number.affected.issues'">
                    <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size()" /></strong></ww:param>
                </ww:text>
            </p>


            <ww:if test="/fieldScreenRenderTabs/empty == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:text name="'bulkworkflowtransition.nofields.onscreen'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"></strong></ww:param>
                        </ww:text>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:else>
            <div class="aui-tabs horizontal-tabs">
                <ww:if test="/fieldScreenRenderTabs/size > 1">
                    <ul class="tabs-menu">
                        <ww:iterator value="/fieldScreenRenderTabs" status="'status'">
                            <li class="menu-item<ww:if test="@status/count == /selectedTab"> active-tab</ww:if><ww:if test="/errorTabs/contains(.) == true"> has-errors</ww:if>">
                                <a href="#screen-tab-<ww:property value="@status/count" />"><ww:property value="./name" /></a>
                            </li>
                        </ww:iterator>
                    </ul>
                </ww:if>

                <%-- Show the actual tabs with their fields --%>
                <ww:iterator value="/fieldScreenRenderTabs" status="'status'">
                    <div id="screen-tab-<ww:property value="@status/count" />" class="tabs-pane<ww:if test="@status/count == /selectedTab"> active-pane</ww:if>">
                        <table class="aui" id="screen-tab-<ww:property value="@status/count"/>-editfields">
                        <%-- Show tab's fields --%>
                        <ww:iterator value="/editActions(./name)">
                            <tr>
                            <ww:if test="./available(/bulkEditBean) == true">
                                <td width="1%">
                                    <!-- Force Resolution requirability -->
                                    <!-- If a resolution is detected on a screen - force the user to make a selection -->
                                    <!-- Avoids transitioning the issue to a 'Resolved' status without setting the 'resolution' -->
                                    <ww:if test="/forceResolution(./field) == true">
                                        <input type="checkbox" disabled="true" id="cb<ww:property value="./field/id"/>" name="actions" value="<ww:property value="./field/id"/>" checked="true" />
                                        <input type="hidden" name="forcedResolution" value="<ww:property value="./field/id"/>">
                                    </ww:if>
                                    <ww:else>
                                        <input type="checkbox" id="cb<ww:property value="./field/id"/>" name="actions" value="<ww:property value="./field/id"/>" <ww:if test="/checked(./field/id) == true">checked</ww:if> />
                                    </ww:else>
                                </td>
                                <td class="fieldLabelArea">
                                    <label for="cb<ww:property value="./field/id"/>">
                                    <ww:text name="'bulkedit.actions.changefield'">
                                        <ww:param name="'value0'"><ww:text name="./fieldName"/></ww:param>
                                    </ww:text>
                                    </label>
                                </td>
                                <ww:property value="/fieldHtml(./field)" escape="'false'" />
                            </ww:if>
                            <ww:else>
                                <td width="1%"><ww:text name="'bulkedit.constants.na'"/></td>
                                <td class="fieldLabelArea">
                                    <ww:text name="'bulkedit.actions.changefield'">
                                        <ww:param name="'value0'"><ww:property value="./fieldName" /></ww:param>
                                    </ww:text>
                                </td>
                                <td>
                                    <ww:text name="unavailableMessage">
                                        <ww:param name="'value0'"><strong></ww:param>
                                        <ww:param name="'value1'"></strong></ww:param>
                                    </ww:text>
                                </td>
                            </ww:else>
                            </tr>
                        </ww:iterator>
                        </table>
                    </div>
                </ww:iterator>
            </div>


            <ww:if test="/commentBulkEditAction/available(/bulkEditBean) == true">
                <h4><ww:text name="'comment.update.title'"/></h4>
                <p>(<ww:text name="'comment.update.desc'"/>)</p>

                <table class="aui">
                <tr>
                    <td width="1%">
                        <input type="checkbox" id="cb<ww:property value="/commentBulkEditAction/field/id"/>" name="commentaction" value="<ww:property value="/commentBulkEditAction/field/id"/>" <ww:if test="/checked(/commentBulkEditAction/field/id) == true">checked</ww:if> />
                    </td>
                    <td class="fieldLabelArea">
                        <label for="cb<ww:property value="/commentBulkEditAction/field/id"/>">
                        <ww:text name="'bulkedit.actions.changefield'">
                            <ww:param name="'value0'"><ww:text name="/commentBulkEditAction/fieldName"/></ww:param>
                        </ww:text>
                        </label>
                    </td>
                    <ww:property value="/commentHtml" escape="false" />
                </tr>
                </table>
            </ww:if>

            <script language="JavaScript" type="text/javascript">
            <!--
                function check(field_id) {
                    var cbox = document.getElementById("cb" + field_id);
                    if (cbox) {
                        cbox.checked = true;
                    }
                }

                var autoCheckFields = [];
                autoCheckFields.push("<ww:property value="/commentBulkEditAction/field/id"/>");
                <ww:iterator value="/fieldScreenRenderTabs">
                    <ww:iterator value="/editActions(./name)">
                        <ww:if test="./available(/bulkEditBean) == true">
                            <ww:if test="/forceResolution(./field) == false">
                                autoCheckFields.push("<ww:property value="./field/id"/>");
                            </ww:if>
                        </ww:if>
                    </ww:iterator>
                </ww:iterator>

                jQuery(autoCheckFields).each(function(index, fieldId) {
                    var $el = jQuery("#" + fieldId);
                    if ($el.size()) {
                        $el.on("change", function() { check(fieldId); });
                    }
                });
            //-->
            </script>
            </ww:else>


            <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications.jsp"/>

    </page:applyDecorator>
</body>
</html>

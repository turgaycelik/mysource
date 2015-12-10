<%@ page import="com.atlassian.jira.web.action.issue.bulkedit.BulkWorkflowTransition,
                 com.atlassian.jira.web.component.IssueTableLayoutBean,
                 java.util.List"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'bulkworkflowtransition.title'"/></title>
</head>
<body>
    <!-- Step 4 - Bulk Operation: Confirmation for EDIT -->
    <page:applyDecorator name="bulkpanel" >
        <page:param name="title"><ww:text name="'bulkworkflowtransition.title'"/>: <ww:text name="'bulkworkflowtransition.confirmation'"/></page:param>
        <page:param name="action">BulkWorkflowTransitionPerform.jspa</page:param>
        <ww:property value="'true'" id="hideSubMenu" />
        <page:param name="instructions">
            <p><ww:text name="'bulkworkflowtransition.confirmation.instructions'"/></p>
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
                        <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height=16 width=16 border=0 align=absmiddle>
                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                            <ww:param name="'issueStatus'" value="/destinationStatusObject(/bulkEditBean/selectedWFTransitionKey)"/>
                            <ww:param name="'isSubtle'" value="false"/>
                            <ww:param name="'isCompact'" value="false"/>
                        </ww:component>
                    </dd>
                </dl>
            </li>
        </ul>
        <ww:property value="/bulkEditBean/actions">
            <ww:if test=". != null && ./empty() == false">
                <table id="updatedfields" class="aui aui-table-rowhover">
                    <thead>
                        <tr>
                            <th colspan=2><ww:text name="'bulkedit.confirm.updatedfields'"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <ww:iterator value="./values">
                            <tr>
                                <td width="19%"><b><ww:property value="./fieldName"/></b></td>
                                <td><ww:property value="/fieldViewHtml(./field)" escape="false" /></td>
                            </tr>
                        </ww:iterator>
                    </tbody>
                </table>
                <p>
                    <ww:text name="'bulkedit.confirm.msg'">
                        <ww:param name="'value0'"><b><ww:property value="/bulkEditBean/selectedIssues/size"/></b></ww:param>
                    </ww:text><br>
                    <ww:text name="'bulkedit.confirm.warning.about.blanks'"/>
                </p>
                <!-- Send Mail confirmation -->
                <ww:if test="/canDisableMailNotifications() == true && /bulkEditBean/hasMailServer == true">
                    <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications-confirmation.jsp"/>
                </ww:if>
            </ww:if>
        </ww:property>
        <p class="bulk-affects">
            <ww:text name="'bulkworkflowtransition.number.affected.issues'">
                <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size()" /></strong></ww:param>
            </ww:text>
        </p>
        <%-- Set this so that it can be used further down --%>
        <ww:property value="/" id="bulkWorkflowTransition" />
        <%
            BulkWorkflowTransition bulkWorkflowTransition = (BulkWorkflowTransition) request.getAttribute("bulkWorkflowTransition");
        %>
        <ui:issuetable layoutBean="<%=bulkWorkflowTransition.getIssueTableLayoutBean()%>"
                       issues="<%=bulkWorkflowTransition.getBulkEditBean().getSelectedIssues()%>"/>
    
        <!-- Add a breakline between issue table and confirmation buttons. -->
    </page:applyDecorator>
</body>
</html>

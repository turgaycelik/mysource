<%@ page import="com.atlassian.jira.web.bean.BulkEditBean"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:if test="/bulkEditBean/invalidSubTaskTypes != null && /bulkEditBean/invalidSubTaskTypes/empty == false">
<p class="bulk-affects">
    <ww:text name="'bulk.move.affects.subtasks'">
        <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/invalidSubTaskCount"/></strong></ww:param>
    </ww:text>
</p>
<ww:iterator value="/bulkEditBean/invalidSubTaskTypes">
    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="20%" colspan="2"><ww:text name="'bulk.move.subtask.type'" /></th>
                <th width="5%"><ww:text name="'bulk.move.to'" /></th>
                <th width="20%"><ww:property value="/constantsManager/issueType(.)/string('name')" /></th>
                <th><ww:text name="'bulk.move.targetworkflow'"/>&nbsp;(<ww:property value="/bulkEditBean/targetWorkflowByType(./string('id'))/name"/>)</th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/bulkEditBean/invalidSubTaskStatusesByType(.)">
            <tr>
                <td>
                    <strong><ww:text name="'bulk.move.currentstatus'"/></strong>
                </td>
                <td>
                    <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                        <ww:param name="'issueStatus'" value="/constantsManager/statusObject(.)"/>
                        <ww:param name="'isSubtle'" value="true"/>
                        <ww:param name="'isCompact'" value="false"/>
                    </ww:component>
                </td>
                <td>
                    <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height="16" width="16" border="0"/>
                </td>
                <td>
                    <strong><ww:text name="'bulk.move.targetstatus'"/></strong>
                </td>
                <td><%-- Selection is given name with BulkEditBean.SUBTASK_STATUS_INFO and subtask type and status id in order to retrieve it from params later --%>
                    <select name="<%=BulkEditBean.SUBTASK_STATUS_INFO%><ww:property value=".." />_<ww:property value="."/>" >
                        <ww:iterator value="targetWorkflowStatuses(../string('id'))">
                            <option value="<ww:property value="./string('id')" />"> <ww:property value="/nameTranslation(.)" />
                            </option>
                        </ww:iterator>
                    </select>
                </td>
            <tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:iterator>
</ww:if>
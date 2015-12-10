<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<script type="text/javascript">
function toggle(mode, elementId)
{
    var hideElement;
    var showElement;

    if (mode == "hide")
    {
        hideElement = document.getElementById('long_' + elementId);
        showElement = document.getElementById('short_' + elementId);
    }
    else
    {
        hideElement = document.getElementById('short_' + elementId);
        showElement = document.getElementById('long_' + elementId);
    }

    if (hideElement && showElement)
    {
        hideElement.style.display = 'none';
        showElement.style.display = '';
    }
}

function selectCellRadioBox(cell)
{
    document.forms['jiraform'].elements[cell].checked = true;
}
</script>

<ww:iterator value="/bulkEditBean/workflowsInUse" status="'workflowstatus'">
    <h3><ww:text name="'bulkworkflowtransition.issue.workflow'"/>: <ww:property value="." /></h3>
    <table id="workflow_<ww:property value="@workflowstatus/index"/>" class="aui aui-table-rowhover" >
        <thead>
            <tr>
                <th nowrap width="25%">
                    <ww:text name="'bulkworkflowtransition.available.actions'"/>
                </th>
                <th nowrap width="25%" colspan="3">
                    <ww:text name="'bulkworkflowtransition.status.transition'" />
                </th>
                <th nowrap>
                    <ww:text name="'bulkworkflowtransition.affected.issues'"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/bulkEditBean/transitionIdsForWorkflow(.)" status="'mapstatus'">
                <tr <ww:if test="@mapstatus/odd == true">class="rowNormal"</ww:if><ww:else>class="rowAlternate"</ww:else>>
                    <!--Workflow Transition/Action-->
                    <td onclick="selectCellRadioBox('id_<ww:property value="/encodeWorkflowTransitionKey(.)" />')">
                        <input type=radio name="wftransition" id="id_<ww:property value="/encodeWorkflowTransitionKey(.)" />"
                               value="<ww:property value="/encodeWorkflowTransitionKey(.)" />"
                               <ww:if test="/bulkEditBean/transitionChecked(.) == true">checked</ww:if>
                                />
                        <ww:property value="/bulkEditBean/transitionName(.., ./actionDescriptorId)" /><br>
                    </td>
                    <!--Status Transition-->
                    <td nowrap>
                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                            <ww:param name="'issueStatus'" value="/originStatusObject(.)"/>
                            <ww:param name="'isSubtle'" value="true"/>
                            <ww:param name="'isCompact'" value="false"/>
                        </ww:component>
                    </td>
                    <td >
                        <img src="<%= request.getContextPath() %>/images/icons/arrow_right_small.gif" height=16 width=16 border=0 align="middle" alt="Arrow Image">
                    </td>
                    <td nowrap>
                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                            <ww:param name="'issueStatus'" value="/destinationStatusObject(.)"/>
                            <ww:param name="'isSubtle'" value="true"/>
                            <ww:param name="'isCompact'" value="false"/>
                        </ww:component>
                    </td>

                    <!--Affected Issues-->
                    <td width="50%">
                        <div id="short_<ww:property value="/encodeWorkflowTransitionKey(.)"/>" onclick="toggle('expand', '<ww:property value="/encodeWorkflowTransitionKey(.)"/>');">
                            <ww:property value="/bulkEditBean/transitionIssueKeys(.)">
                                <ww:iterator value="/shortListTransitionIssueKeys(.)" status="'status'">
                                    <ww:property value="." /><ww:if test="@status/last == false">, </ww:if>
                                    <ww:if test="../size > 5 && @status/last == true"></ww:if>
                                </ww:iterator>
                                <ww:if test="./size > 5">&hellip; <a href="#" onclick="return false;" title="<ww:text name="'bulkworkflowtransition.click.fulllist'" />">(<ww:text name="'bulkworkflowtransition.affected.issues.size'"><ww:param  name="'value0'"><ww:property value="./size" /></ww:param></ww:text>)</a></ww:if>
                            </ww:property>
                        </div>
                        <ww:if test="/bulkEditBean/transitionIssueKeys(.)/size > 5">
                            <div style="display:none; cursor:pointer;" id="long_<ww:property value="/encodeWorkflowTransitionKey(.)"/>" onclick="toggle('hide', '<ww:property value="/encodeWorkflowTransitionKey(.)"/>');">
                                <ww:property value="/bulkEditBean/transitionIssueKeys(.)">
                                    <ww:iterator value="." status="'status'">
                                        <ww:property value="." /><ww:if test="@status/last == false">, </ww:if>
                                    </ww:iterator>
                                </ww:property>
                                &hellip; <a href="#" onclick="return false;"><ww:text name="'bulkworkflowtransition.hide.list'" /></a>
                            </div>
                        </ww:if>
                    </td>
                </tr>
            </ww:iterator>
        </tbody>
    </table>
</ww:iterator>

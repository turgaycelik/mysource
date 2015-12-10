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
</script>

<table id="eventTypeTable" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
            </th>
            <th>
                <ww:text name="'common.words.description'"/>
            </th>
            <th>
                <ww:text name="'admin.event.types.status'"/>
            </th>
            <th>
                <ww:text name="'admin.event.types.template'"/>
            </th>
            <th>
                <ww:text name="'admin.event.types.assocaited.notification.schemes'"/>
            </th>
            <th>
                <ww:text name="'admin.event.types.assocaited.workflows'"/>
            </th>
            <th>
                <ww:if test="./systemEventType == false">
                    <ww:text name="'admin.event.types.operations'"/>
                </ww:if>
            </th>
        </tr>
    </thead>
    <tbody>
    <% int evtCount = 0; %>
    <ww:iterator value="." status="'status'">
        <tr>
            <!-- Name -->
            <td>
                <ww:if test="./systemEventType == true">
                    <b><ww:property value="./translatedName(/remoteUser)" /></b>
                    <span class="smallgrey">(<ww:text name="'admin.event.types.system'" />)</span>
                </ww:if>
                <!-- Custom event types are not fully i18n'ized yet. -->
                <ww:else>
                    <ww:property value="./name" />
                </ww:else>
            </td>
            <!-- Description -->
            <td>
                <ww:if test="./systemEventType == true">
                    <ww:property value="./translatedDesc(/remoteUser)" />
                </ww:if>
                <ww:else>
                    <ww:property value="./description" />
                </ww:else>
            </td>
            <!-- Event Type Status -->
            <td>
                <ww:if test="/eventTypeManager/active(.) == true">
                    <span class="green-highlight"><ww:text name="'admin.event.types.active'" /></span>
                </ww:if>
                <ww:else>
                    <span class="red-highlight"><ww:text name="'admin.event.types.inactive'" /></span>
                </ww:else>
            </td>
            <!-- Template -->
            <td >
                <ww:property value="/templateManager/defaultTemplate(.)/name" />
            </td>
            <!-- Associated Notification Schemes -->
            <td >
                <ww:property value="/associatedNotificationSchemes(.)" >
                    <ww:if test="./empty == false">
                        <ul>
                            <ww:iterator value="./keySet">
                                <li>
                                    <a href="<ww:url page="/secure/admin/EditNotifications!default.jspa"><ww:param name="'schemeId'"><ww:property value="." /></ww:param></ww:url>">
                                        <ww:property value="../(.)" />
                                    </a>
                                </li>
                            </ww:iterator>
                        </ul>
                    </ww:if>
                </ww:property>
            </td>
            <!-- Associated Workflows and Transitions -->
            <% int wfCount = 0; %>
            <td >
                <ww:property value="/associatedWorkflows(.)" >
                    <ww:if test="./keySet/empty == false">
                        <ul>
                            <ww:iterator value="./keySet">
                                <li>
                                     <div id="short_<%=evtCount%>_<%=wfCount%>" onclick="toggle('expand', '<%=evtCount%>_<%=wfCount%>');">
                                        <!-- Workflow Link -->
                                        <a title="<ww:text name="'admin.event.type.workflowlink'" />" href="<ww:url page="/secure/admin/workflows/ViewWorkflowSteps.jspa"><ww:param name="'workflowName'"><ww:property value="." /></ww:param><ww:param name="'workflowMode'" value="'live'"/></ww:url>">
                                            <ww:property value="." />
                                        </a>
                                        <!-- Transition Link -->
                                        <span class="small">[
                                            <ww:iterator value="/shortList(../(.))" status="'status'">
                                                <ww:if test="@status/first == false">,</ww:if>
                                                <a title="<ww:text name="'admin.event.type.transitionlink'" />" href="<ww:url page="/secure/admin/workflows/ViewWorkflowTransition.jspa">
                                                    <ww:param name="'descriptorTab'" value="'postfunctions'" />
                                                    <ww:property value="/stepId(.., ./id)">
                                                        <ww:if test=". != null">
                                                            <ww:param name="'workflowStep'"><ww:property value="." /></ww:param>
                                                        </ww:if>
                                                    </ww:property>
                                                    <ww:param name="'workflowTransition'"><ww:property value="./id" /></ww:param>
                                                    <ww:param name="'workflowName'"><ww:property value=".." /></ww:param>
                                                    <ww:param name="'workflowMode'" value="'live'"/>
                                                </ww:url>">
                                                    <ww:property value="./name"/> (<ww:property value="./id" />)
                                                </a>
                                            </ww:iterator>
                                            <ww:if test="../(.)/size > 3"><span style="cursor:pointer;" class="smallgrey" title="<ww:text name="'admin.event.type.list.fulllist'" />">...</span></ww:if> ]
                                        </span>
                                    </div>
                                    <ww:if test="../(.)/size > 3">
                                        <div style="display:none; cursor:pointer;" id="long_<%=evtCount%>_<%=wfCount%>" onclick="toggle('hide', '<%=evtCount%>_<%=wfCount%>');">
                                            <!-- Workflow Link -->
                                            <a title="<ww:text name="'admin.event.type.workflowlink'" />" href="<ww:url page="/secure/admin/workflows/ViewWorkflowSteps.jspa">
                                            <ww:param name="'workflowName'"><ww:property value="." /></ww:param>
                                            <ww:param name="'workflowMode'" value="'live'"/>
                                            </ww:url>">
                                                <ww:property value="." />
                                            </a>
                                            <!-- Transition Link -->
                                            <span class="small">[
                                                <ww:iterator value="../(.)" status="'status'">
                                                    <ww:if test="@status/first == false">,</ww:if>
                                                    <a title="<ww:text name="'admin.event.type.transitionlink'" />" href="<ww:url page="/secure/admin/workflows/ViewWorkflowTransition.jspa">
                                                        <ww:param name="'descriptorTab'" value="'postfunctions'" />
                                                        <ww:property value="/stepId(.., ./id)">
                                                            <ww:if test=". != null">
                                                                <ww:param name="'workflowStep'"><ww:property value="." /></ww:param>
                                                            </ww:if>
                                                        </ww:property>
                                                        <ww:param name="'workflowTransition'"><ww:property value="./id" /></ww:param>
                                                        <ww:param name="'workflowName'"><ww:property value=".." /></ww:param>
                                                        <ww:param name="'workflowMode'" value="'live'"/>
                                                    </ww:url>">
                                                        <ww:property value="./name"/> (<ww:property value="./id" />)
                                                    </a>
                                                </ww:iterator>
                                                <span class="smallgrey">(<ww:text name="'admin.event.type.list.hide'" />)</span> ]
                                            </span>
                                        </div>
                                    </ww:if>
                                </li>
                                <% wfCount++; %>
                            </ww:iterator>
                        </ul>
                    </ww:if>
                </ww:property>
            </td>
            <td>
                <!-- Event Type Operations -->
                <ww:if test="./systemEventType == false">
                        <ul class="operations-list">
                            <li><a id="edit_<ww:property value="./name"/>" href="<ww:url page="EditEventType.jspa"><ww:param name="'eventTypeId'" value="./id" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                            <ww:if test="/eventTypeManager/active(.) == false">
                                <li><a id="del_<ww:property value="./name"/>" href="<ww:url page="DeleteEventType.jspa"><ww:param name="'eventTypeId'" value="./id" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                            </ww:if>
                        </ul>
                </ww:if>
            </td>
        </tr>
        <% evtCount++; %>
    </ww:iterator>
    </tbody>
</table>

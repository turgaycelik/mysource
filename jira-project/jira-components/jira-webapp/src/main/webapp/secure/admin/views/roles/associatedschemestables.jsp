<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>

<ww:if test="/associatedNotificationSchemes/size() != 0">
    <p>
        <ww:text name="'admin.projectroles.usage.notificationschemes'">
            <ww:param><ww:property value="/role/name"/></ww:param>
            <ww:param><ww:property value="/associatedNotificationSchemes/size()"/></ww:param>
        </ww:text>:
    </p>
    <table id="relatednotificationschemes" class="aui related-tables">
        <thead>
            <tr>
                <th><ww:text name="'admin.projects.notification.scheme'"/></th>
                <th><ww:text name="'admin.projectroles.usage.associated.projects'"/></th>
                <th><ww:text name="'admin.projectroles.usage.role.members.per.project'"/></th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/associatedNotificationSchemes" status="'outside'">
                <ww:if test="/associatedProjectsForNotificationScheme(.)/size() != 0">
                    <ww:iterator value="/associatedProjectsForNotificationScheme(.)" status="'status'">
                        <tr>
                            <ww:if test="@status/first == true">
                                <td rowspan="<ww:property value="/associatedProjectsForNotificationScheme(..)/size()"/>">
                                    <a href="<%=request.getContextPath()%>/secure/admin/EditNotifications!default.jspa?schemeId=<ww:property value="../long('id')"/>"><ww:property value="../string('name')"/></a>
                                </td>
                            </ww:if>
                            <td>
                                <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./key"/>/summary"><ww:property value="./name"/></a>
                            </td>
                            <td>
                                <ww:property value="/memberCountForProject(.)"/> (<a id="view_project_role_actors_<ww:property value="./id"/>" href="<%=request.getContextPath()%>/plugins/servlet/project-config/<ww:property value="./key"/>/roles"><ww:text name="'common.words.view'"/></a>)
                            </td>
                        </tr>
                    </ww:iterator>
                </ww:if>
                <ww:else>
                    <tr>
                        <td>
                            <a href="<%=request.getContextPath()%>/secure/admin/EditNotifications!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')"/></a>
                        </td>
                        <td><ww:text name="'common.words.none'"/></td>
                        <td><ww:text name="'common.words.none'"/></td>
                    </tr>
                </ww:else>
            </ww:iterator>
        </tbody>
    </table>
</ww:if>


<ww:if test="/associatedPermissionSchemes/size() != 0">
    <p>
        <ww:text name="'admin.projectroles.usage.permissionschemes'">
            <ww:param><ww:property value="/role/name"/></ww:param>
            <ww:param><ww:property value="/associatedPermissionSchemes/size()"/></ww:param>
        </ww:text>:
    </p>
    <table id="relatedpermissionschemes" class="aui related-tables">
        <thead>
            <tr>
                <th><ww:text name="'admin.projects.permission.scheme'"/></th>
                <th><ww:text name="'admin.projectroles.usage.associated.projects'"/></th>
                <th><ww:text name="'admin.projectroles.usage.role.members.per.project'"/></th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/associatedPermissionSchemes" status="'outside'">
                <ww:if test="/associatedProjectsForPermissionScheme(.)/size() != 0">
                    <ww:iterator value="/associatedProjectsForPermissionScheme(.)" status="'status'">
                        <tr>
                            <ww:if test="@status/first == true">
                                <td rowspan="<ww:property value="/associatedProjectsForPermissionScheme(..)/size()"/>">
                                    <a href="<%=request.getContextPath()%>/secure/admin/EditPermissions!default.jspa?schemeId=<ww:property value="../long('id')"/>"><ww:property value="../string('name')"/></a>
                                </td>
                            </ww:if>
                            <td>
                                <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./key"/>/summary"><ww:property value="./name"/></a>
                            </td>
                            <td>
                                <ww:property value="/memberCountForProject(.)"/> (<a href="<%=request.getContextPath()%>/plugins/servlet/project-config/<ww:property value="./key"/>/roles"><ww:text name="'common.words.view'"/></a>)
                            </td>
                        </tr>
                    </ww:iterator>
                </ww:if>
                <ww:else>
                    <tr>
                        <td>
                            <a href="<%=request.getContextPath()%>/secure/admin/EditPermissions!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')"/></a>
                        </td>
                        <td><ww:text name="'common.words.none'"/></td>
                        <td><ww:text name="'common.words.none'"/></td>
                    </tr>
                </ww:else>
            </ww:iterator>
        </tbody>
    </table>
</ww:if>

<ww:if test="/associatedIssueSecuritySchemes/size() != 0">
    <p>
        <ww:text name="'admin.projectroles.usage.issuesecurityschemes'">
            <ww:param><ww:property value="/role/name"/></ww:param>
            <ww:param><ww:property value="/associatedIssueSecuritySchemes/size()"/></ww:param>
        </ww:text>:
    </p>
    <table id="issuesecurityschemes" class="aui related-tables">
        <thead>
            <tr>
                <th><ww:text name="'admin.projects.issue.security.scheme'"/></th>
                <th><ww:text name="'admin.projectroles.usage.associated.projects'"/></th>
                <th><ww:text name="'admin.projectroles.usage.role.members.per.project'"/></th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/associatedIssueSecuritySchemes" status="'outside'">
                <ww:if test="/associatedProjectsForIssueSecurityScheme(.)/size() != 0">
                    <ww:iterator value="/associatedProjectsForIssueSecurityScheme(.)" status="'status'">
                        <tr>
                            <ww:if test="@status/first == true">
                                <td rowspan="<ww:property value="/associatedProjectsForIssueSecurityScheme(..)/size()"/>">
                                    <a href="<%=request.getContextPath()%>/secure/admin/EditIssueSecurities!default.jspa?schemeId=<ww:property value="../long('id')"/>"><ww:property value="../string('name')"/></a>
                                </td>
                            </ww:if>
                            <td>
                                <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./key"/>/summary"><ww:property value="./name"/></a>
                            </td>
                            <td>
                                <ww:property value="/memberCountForProject(.)"/> (<a href="<%=request.getContextPath()%>/plugins/servlet/project-config/<ww:property value="./key"/>/roles"><ww:text name="'common.words.view'"/></a>)
                            </td>
                        </tr>
                    </ww:iterator>
                </ww:if>
                <ww:else>
                    <tr>
                        <td>
                            <a href="<%=request.getContextPath()%>/secure/admin/EditIssueSecurities!default.jspa?schemeId=<ww:property value="./long('id')"/>"><ww:property value="./string('name')"/></a>
                        </td>
                        <td><ww:text name="'common.words.none'"/></td>
                        <td><ww:text name="'common.words.none'"/></td>
                    </tr>
                </ww:else>
            </ww:iterator>
        </tbody>
    </table>
</ww:if>

<ww:if test="/associatedWorkflows/size() != 0">
    <p>
        <ww:text name="'admin.projectroles.usage.workflows'">
            <ww:param><ww:property value="/associatedWorkflows/size()"/></ww:param>
            <ww:param><ww:property value="/role/name"/></ww:param>
        </ww:text>
    </p>
    <table class="aui">
        <thead>
            <tr>
                <th><ww:text name="'issue.field.workflow'"/></th>
                <th><ww:text name="'issue.field.workflow.action'"/></th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/associatedWorkflows" status="'workflowstatus'">
                <ww:iterator value="./value" status="'actionstatus'">
                    <tr>
                        <ww:if test="@actionstatus/first == true">
                            <td rowspan="<ww:property value="../value/size()"/>">
                                <a href="<ww:url value="'ViewWorkflowSteps.jspa'">
                                      <ww:param name="'workflowMode'" value="../key/mode" />
                                      <ww:param name="'workflowName'" value="../key/name"/>
                                    </ww:url>">
                                    <ww:property value="../key/name" />
                                </a>
                            </td>
                        </ww:if>
                        <td>
                            <a href="<ww:url value="'ViewWorkflowTransition.jspa'">
                                  <ww:param name="'workflowStep'" value="/stepId(./id, ../key/name)"/>
                                  <ww:param name="'workflowTransition'" value="./id"/>
                                  <ww:param name="'workflowMode'" value="../key/mode" />
                                  <ww:param name="'workflowName'" value="../key/name"/>
                                  <ww:param name="'descriptorTab'" value="conditions"/>
                                </ww:url>">
                                <ww:property value="./name"/>
                            </a>
                        </td>
                    </tr>
                </ww:iterator>
            </ww:iterator>
        </tbody>
    </table>
</ww:if>

<ww:if test="/associatedNotificationSchemes/size() == 0 && /associatedPermissionSchemes/size() == 0 && /associatedWorkflows/size() == 0 && /associatedIssueSecuritySchemes/size() == 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.projectroles.usage.no.associations.ent'"/></aui:param>
    </aui:component>
</ww:if>

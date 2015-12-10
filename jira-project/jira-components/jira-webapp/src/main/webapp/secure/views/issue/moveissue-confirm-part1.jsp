<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<!-- Breaking page into smaller parts - JRA-5059 -->
<!-- PROJECT, TYPE & STATUS -->
<ww:if test="subTask == false">
    <tr>
        <td>
            <strong><ww:text name="'common.concepts.project'"/></strong>
        </td>
        <td>
            <%-- Highlight differences --%>
            <ww:if test="projectMatch == false">
                <span class="status-inactive"><ww:property value="projectManager/project(issue)/string('name')" /></span>
            </ww:if>
            <ww:else>
                <ww:property value="projectManager/project(issue)/string('name')" />
            </ww:else>
        </td>
        <td>
            <ww:if test="projectMatch == false">
                <span class="status-active"><ww:property value="targetProject/string('name')" /></span>
            </ww:if>
            <ww:else>
                <ww:property value="targetProject/string('name')" />
            </ww:else>
        </td>
    </tr>
</ww:if>
<tr>
    <td>
        <strong><ww:text name="'issue.field.type'"/></strong>
    </td>
    <td>
    <%-- Highlight differences --%>
        <ww:if test="issueTypeMatch == false">
            <span class="status-inactive"><ww:property value="/nameTranslation(constantsManager/issueType(issue/string('type')))" /></span>
        </ww:if>
        <ww:else>
            <ww:property value="/nameTranslation(constantsManager/issueType(issue/string('type')))" />
        </ww:else>
    </td>
    <td>
        <ww:if test="issueTypeMatch == false">
            <span class="status-active"><ww:property value="./moveIssueBean/targetTypeName" /></span>
        </ww:if>
        <ww:else>
            <ww:property value="./moveIssueBean/targetTypeName" />
        </ww:else>
    </td>
</tr>
<ww:if test="workflowMatch(issue/string('type'), targetIssueType) == false">
    <tr>
        <td>
            <strong><ww:text name="'issue.field.status'"/></strong> &nbsp;<span class="secondary-text">(<ww:text name="'moveissue.workflow'"/>)</span>
        </td>
        <td>
            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                <ww:param name="'issueStatus'" value="constantsManager/statusObject(issue/string('status'))"/>
                <ww:param name="'isSubtle'" value="false"/>
                <ww:param name="'isCompact'" value="false"/>
            </ww:component>

            <ww:if test="workflowMatch(issue/string('type'), targetIssueType) == false">
                &nbsp;<span class="status-inactive">(<ww:property value="currentWorkflow/name"/>)</span>
            </ww:if>
            <ww:else>
                &nbsp;<span class="secondary-text">(<ww:property value="currentWorkflow/name"/>)</span>
            </ww:else>
        </td>
        <td>
            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                <ww:param name="'issueStatus'" value="constantsManager/statusObject(./moveIssueBean/targetStatusId)"/>
                <ww:param name="'isSubtle'" value="false"/>
                <ww:param name="'isCompact'" value="false"/>
            </ww:component>

            <ww:if test="workflowMatch(issue/string('type'), targetIssueType) == false">
                &nbsp;<span class="status-active">(<ww:property value="targetWorkflow/name"/>)</span>
            </ww:if>
            <ww:else>
                &nbsp;<span class="secondary-text">(<ww:property value="targetWorkflow/name"/>)</span>
            </ww:else>
        </td>
    </tr>
</ww:if>

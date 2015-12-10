<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>

<!-- Breaking page into smaller parts - JRA-5059 -->

<!-- TYPE & STATUS -->
<tr>
    <td>
        <strong><ww:text name="'issue.field.type'"/></strong>
    </td>
    <td>
    <%-- Highlight differences --%>
        <span class="status-inactive"><ww:property value="/issue/issueTypeObject/nameTranslation(.)" /></span>

    </td>
    <td>
        <span class="status-active"><ww:property value="/updatedIssue/issueTypeObject/nameTranslation(.)" /></span>
    </td>
</tr>

<ww:if test="currentWorkflow != targetWorkflow">
<tr>
    <td>
        <strong><ww:text name="'issue.field.status'"/></strong> &nbsp;<span class="secondary-text">(<ww:text name="'convert.issue.to.subtask.workflow'"/>)</span>
    </td>
    <td>
        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
            <ww:param name="'issueStatus'" value="/issue/statusObject"/>
            <ww:param name="'isSubtle'" value="false"/>
            <ww:param name="'isCompact'" value="false"/>
        </ww:component>

        <ww:if test="currentWorkflow != targetWorkflow">
            &nbsp;<span class="status-inactive">(<ww:property value="currentWorkflow/name"/>)</span>
        </ww:if>
        <ww:else>
            &nbsp;<span class="secondary-text">(<ww:property value="currentWorkflow/name"/>)</span>
        </ww:else>
    </td>
    <td>
        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
            <ww:param name="'issueStatus'" value="/updatedIssue/statusObject"/>
            <ww:param name="'isSubtle'" value="false"/>
            <ww:param name="'isCompact'" value="false"/>
        </ww:component>

        <ww:if test="currentWorkflow != targetWorkflow">
            &nbsp;<span class="status-active">(<ww:property value="targetWorkflow/name"/>)</span>
        </ww:if>
        <ww:else>
            &nbsp;<span class="secondary-text">(<ww:property value="targetWorkflow/name"/>)</span>
        </ww:else>
    </td>
</tr>
</ww:if>

<ww:if test="/issue/subTask == false">
    <!-- Security Level -->
    <ww:if test="issue/securityLevelId != targetIssue/parentObject/securityLevelId">
    <tr>
        <td>
            <ww:text name="'issue.field.securitylevel'"/>
        </td>
        <td>
            <ww:if test="/issue/securityLevel">
                <span class="status-inactive"><ww:property value="/issue/securityLevel/string('name')" /></span>
            </ww:if>
            <ww:else>
                <span class="status-inactive"><ww:text name="'common.words.none'"/></span>
            </ww:else>
        </td>
        <td>
            <ww:if test="/targetIssue/parentObject/securityLevel">
                <span class="status-active"><ww:property value="/targetIssue/parentObject/securityLevel/string('name')" /></span>
            </ww:if>
            <ww:else>
                <span class="status-active"><ww:text name="'common.words.none'"/></span>    
            </ww:else>
        </td>
    </tr>
    </ww:if>
</ww:if>

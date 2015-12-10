<%--
  -- Renders the DOM structure required for the labels lozenges, both system and custom fields, when updating
  -- labels in-place (via Ajax).
  --%>

<%@ taglib uri="webwork" prefix="ww" %>

<%-- The DOM structure here should match issuesummaryblock.vm, labels-columnview.vm, column-view-label, and view-label.vm. --%>
<div class="labels-wrap value">
<ww:if test="/existingLabels/size > 0">
    <ul class="labels" id="<ww:property value="/domId"/>-<ww:property value="/issueObject/id"/>-value">
    <ww:iterator value="/existingLabels">
        <ww:if test="/noLink == true">
            <li><ww:property value="."/></li>
        </ww:if>
        <ww:else>
            <li><a class="lozenge" href="<%= request.getContextPath()%>/secure/IssueNavigator.jspa?reset=true<ww:property value="/labelNavigatorUrl(.)"/>" title="<ww:property value="."/>"><span><ww:property value="."/></span></a></li>
        </ww:else>
    </ww:iterator>
    <ww:if test="/editable == true">
        <%-- Edit links don't appear in issue tables, so id's of the form edit-labels-(labels|customfield_10000) are good enough. --%>
        <li><a class="icon jira-icon-edit edit-labels" id="edit-labels-<ww:property value="/issueObject/id"/>-<ww:property value="/domId"/>" href="<%= request.getContextPath()%>/secure/EditLabels!default.jspa?id=<ww:property value="/issueObject/id"/>&noLink=<ww:property value="/noLink"/><ww:if test="/customFieldId">&customFieldId=<ww:property value="/customFieldId"/></ww:if>"><span><ww:text name="'label.edit.title'"/></span></a></li>
    </ww:if>
    </ul>
</ww:if>
<ww:else>
    <span class="labels" id="<ww:property value="/domId"/>-<ww:property value="/issueObject/id"/>-value"><ww:text name="'common.words.none'"/></span>
    <ww:if test="/editable == true">
        <%-- Edit links don't appear in issue tables, so id's of the form edit-labels-(labels|customfield_10000) are good enough. --%>
        <a class="icon jira-icon-edit edit-labels" id="edit-labels-<ww:property value="/issueObject/id"/>-<ww:property value="/domId"/>" href="<%= request.getContextPath()%>/secure/EditLabels!default.jspa?id=<ww:property value="/issueObject/id"/>&noLink=<ww:property value="/noLink"/><ww:if test="/customFieldId">&customFieldId=<ww:property value="/customFieldId"/></ww:if>"><span><ww:text name="'label.edit.title'"/></span></a>
    </ww:if>
</ww:else>
</div>

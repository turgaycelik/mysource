<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<ww:if test="assigneeTypes/size > 1">
<ui:select label="text('admin.projects.default.assignee')" name="'assigneeType'" list="assigneeTypes" listKey="'key'" listValue="'text(value)'">
    <ui:param name="'description'">
            <ww:text name="'admin.addproject.default.assignee.description'"/>
    </ui:param>
</ui:select>
</ww:if>
<ww:else>
    <ui:component name="'assigneeType'" value="assigneeTypes/keySet/iterator/next" template="hidden.jsp" theme="'single'" />
    <ww:label label="text('admin.projects.default.assignee')" name="'assigneeType'">
        <ui:param name="'value'"><ww:text name="assigneeTypes/values/iterator/next"/></ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.addproject.default.assignee.description'"/>  
        </ui:param>
    </ww:label>
</ww:else>

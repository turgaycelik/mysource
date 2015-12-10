<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="com.atlassian.jira.ComponentManager"%>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<html>
<head>
	<title><ww:text name="'admin.projects.edit.project.lead.and.default.assignee'"/>: <ww:property value="project/string('name')" /></title>

    <%
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");
    %>

</head>
<body>

<page:applyDecorator id="project-edit-lead-and-default-assignee" name="auiform">
    <page:param name="action">EditProjectLeadAndDefaultAssignee.jspa</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="submitButtonName">Update</page:param>
    <page:param name="cancelLinkURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/summary</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'admin.projects.edit.project.lead.and.default.assignee'"/>: <ww:text name="project/string('name')" /></aui:param>
    </aui:component>

    <page:applyDecorator name="auifieldgroup">
        <page:param name="id">lead-container</page:param>
        <aui:component label="text('common.concepts.projectlead')" name="'lead'" id="'lead'" template="singleSelectUserPicker.jsp" theme="'aui'">
            <aui:param name="'userName'" value="/lead"/>
            <aui:param name="'userFullName'" value="leadUserObj/displayName"/>
            <aui:param name="'userAvatar'" value="leadUserAvatarUrl"/>
            <aui:param name="'inputText'" value="/leadError" />
            <aui:param name="'mandatory'" value="'true'" />
            <aui:param name="'disabled'" value="userPickerDisabled" />
        </aui:component>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <page:param name="description">
            <ww:text name="'admin.addproject.default.assignee.description'"/>
         </page:param>
        <ww:if test="assigneeTypes/size > 1">
            <aui:select label="text('admin.projects.default.assignee')" name="'assigneeType'" list="assigneeTypes" listKey="'key'" listValue="'text(value)'" theme="'aui'" />
        </ww:if>
        <ww:else>
            <aui:component name="'assigneeType'" value="assigneeTypes/keySet/iterator/next" template="hidden.jsp" theme="'aui'" />
            <aui:component label="text('admin.projects.default.assignee')" name="'assigneeType'" value="text(assigneeTypes/values/iterator/next)" template="formFieldValue.jsp" theme="'aui'" />
        </ww:else>
    </page:applyDecorator>

    <aui:component name="'pid'" template="hidden.jsp" theme="'aui'"/>
</page:applyDecorator>
</body>
</html>

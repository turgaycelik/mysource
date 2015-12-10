<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.issuemigration.title'"/></title>
</head>

<body>
	<page:applyDecorator name="jiraform">
        <%--<page:param name="helpURL">configcustomfield</page:param>--%>
        <%--<page:param name="helpURLFragment">#Managing+multiple+configuration+schemes</page:param>--%>
		<page:param name="title"><ww:text name="'admin.issuemigration.map.status'"/></page:param>

        <page:param name="instructions">
            <ww:text name="'admin.issuemigration.issue.type'"/>
           <ww:component name="'issuetype'" template="constanticon.jsp">
             <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
             <ww:param name="'iconurl'" value="/currentIssueContext/issueType/string('iconurl')" />
             <ww:param name="'alt'"><ww:property value="/currentIssueContext/issueType/string('name')" /></ww:param>
           </ww:component>
            <ww:text name="'admin.issuemigration.issue.type.not.valid'">
                <ww:param name="'value0'"><strong><ww:property value="/currentIssueContext/issueType/string('name')" /></strong></ww:param>
                <ww:param name="'value1'"><strong><ww:property value="/currentIssueContext/project/string('name')" /></strong></ww:param>
                <ww:param name="'value2'"><ww:component name="'issuetype'" template="constanticon.jsp">
             <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
             <ww:param name="'iconurl'" value="/bulkEditBean/targetIssueTypeGV/string('iconurl')" />
             <ww:param name="'alt'"><ww:property value="/bulkEditBean/targetIssueTypeGV/string('name')" /></ww:param>
           </ww:component>
            <strong><ww:property value="/bulkEditBean/targetIssueTypeGV/string('name')" /></strong></ww:param>
            </ww:text>
            <ww:text name="'bulk.move.statusmapping'" />.

        </page:param>
		<page:param name="action">MigrateIssueTypes!chooseStatus.jspa</page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>
		<page:param name="wizard">true</page:param>

        <tr><td colspan="2">
            <%@include file="/secure/views/bulkedit/includes/statusmapping.jsp"%>

            <%@include file="/secure/views/bulkedit/subtaskstatusmapping.jsp"%>
        </td></tr>
    </page:applyDecorator>

</body>
</html>

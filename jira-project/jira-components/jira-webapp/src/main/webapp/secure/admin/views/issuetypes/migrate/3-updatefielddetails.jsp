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
		<page:param name="title"><ww:text name="'admin.issuemigration.update.fields'"/></page:param>
        <page:param name="action">MigrateIssueTypes!setFields.jspa</page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>
		<page:param name="wizard">true</page:param>
        <page:param name="instructions">
            <ww:text name="'admin.issuemigration.update.fields.with.current'"/>
           <ww:component name="'issuetype'" template="constanticon.jsp">
             <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
             <ww:param name="'iconurl'" value="/currentIssueContext/issueType/string('iconurl')" />
             <ww:param name="'alt'"><ww:property value="/nameTranslation(/currentIssueContext/issueType)" /></ww:param>
           </ww:component>
            <strong><ww:text name="'admin.issuemigration.issue.in.project'">
                <ww:param name="'value0'"><ww:property value="/nameTranslation(/currentIssueContext/issueType)" /></strong></ww:param>
                <ww:param name="'value1'"><strong><ww:property value="/currentIssueContext/project/string('name')" /></strong></ww:param>
            </ww:text>.

            <%@ include file="/secure/views/bulkedit/updatefieldsinstruction.jsp"%>
        </page:param>

        <tr><td>

            <%@include file="/secure/views/bulkedit/fielddetails.jsp"%>

        </td></tr>

    </page:applyDecorator>
</body>
</html>

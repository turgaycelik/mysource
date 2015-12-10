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
		<page:param name="title"><ww:text name="'admin.issuemigration.select.type'"/></page:param>
        <page:param name="description">
        </page:param>
        <page:param name="instructions">
             <ww:text name="'admin.issuemigration.select.type.instruction'"/>
            <ww:component name="'issuetype'" template="constanticon.jsp">
              <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
              <ww:param name="'iconurl'" value="/currentIssueContext/issueType/string('iconurl')" />
              <ww:param name="'alt'"><ww:property value="/nameTranslation(/currentIssueContext/issueType)" /></ww:param>
            </ww:component>
             <strong><ww:text name="'admin.issuemigration.issue.in.project'">
                 <ww:param name="'value0'"><ww:property value="/nameTranslation(/currentIssueContext/issueType)" /></strong></ww:param>
                 <ww:param name="'value1'"><strong><ww:property value="/currentIssueContext/project/string('name')" /></ww:param>
             </ww:text></strong>
        </page:param>
		<page:param name="action">MigrateIssueTypes!chooseContext.jspa</page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>
		<page:param name="wizard">true</page:param>

        <ui:component name="'pid'" template="hidden.jsp" theme="'single'" value="/currentIssueContext/project/long('id')" />
        <ww:property value="/issueTypeEditHtml" escape="false"/>
    </page:applyDecorator>
</body>
</html>

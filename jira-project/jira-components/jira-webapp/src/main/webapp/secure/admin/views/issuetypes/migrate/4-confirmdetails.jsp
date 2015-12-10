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
		<page:param name="title"><ww:text name="'admin.issuemigration.confirmation'"/></page:param>

        <page:param name="instructions">
            <p><ww:text name="'admin.issuemigration.confirmation.instruction'"/></p>
            <ul class="square">
            <ww:iterator value="/multiBulkMoveBean/issuesInContext" status="'status'">
               <li><a href="#<ww:property value="./key/project/string('id')" /><ww:property value="./key/issueTypeObject/id" />"><ww:property value="./key/project/string('name')" /> - <ww:property value="./key/issueTypeObject/nameTranslation" /></a></li>
            </ww:iterator>
            </ul>
        </page:param>
		<page:param name="action">MigrateIssueTypes!perform.jspa</page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ManageIssueTypeSchemes!default.jspa</page:param>
		<page:param name="wizard">true</page:param>

        <tr><td>
            <ww:iterator value="./multiBulkMoveBean/bulkEditBeans" status="'status'">
                <page:applyDecorator name="jirapanel">
                    <page:param name="title">
                        <strong><ww:property value="./key/project/string('name')" /></strong> -
                        <ww:component name="'issuetype'" template="constanticon.jsp">
                          <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                          <ww:param name="'iconurl'" value="./key/issueType/string('iconurl')" />
                          <ww:param name="'alt'"><ww:property value="/nameTranslation(./key/issueType)" /></ww:param>
                        </ww:component>
                        <strong><ww:property value="/nameTranslation(./key/issueType)" /></strong>
                    </page:param>
                    <page:param name="instructions">
                        <p id="<ww:property value="./key/project/string('id')" /><ww:property value="./key/issueTypeObject/id" />">
                            <ww:text name="'admin.issuemigration.confirmation.summary.of.changes'">
                                <ww:param name="'value0'"><strong><ww:property value="./value/selectedIssues/size()" /></strong></ww:param>
                                <ww:param name="'value1'"><strong><ww:property value="./key/project/string('name')" /></strong></ww:param>
                                <ww:param name="'value2'"><strong><ww:property value="/nameTranslation(./key/issueType)" /></strong></ww:param>
                            </ww:text>
                        </p>
                    </page:param>

                    <ww:property value="./value" >
                        <%@include file="/secure/views/bulkedit/confirmationdetails.jsp"%>
                    </ww:property>
                    <a href="#top" class="backToTop"><ww:text name="'admin.issuemigration.confirmation.back.to.top'"/></a>
                </page:applyDecorator>
            </ww:iterator>
        </td></tr>
    </page:applyDecorator>
</body>
</html>

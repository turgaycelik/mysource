<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
     <title>
        <ww:if test="/bulkEditBean/singleMode == false">
            <ww:text name="'bulk.migrate.title'"/>
        </ww:if>
        <ww:else>
            <ww:text name="'moveissue.title'"/>
        </ww:else>
    </title>
</head>
<body>
    <page:applyDecorator name="bulkpanel">
        <page:param name="action">BulkMigrateChooseStatus.jspa</page:param>
        <page:param name="title">
            <ww:if test="/bulkEditBean/singleMode == false">
                <ww:text name="'bulkedit.step3'"/>: 
            </ww:if>
            <ww:else>
            	<ww:text name="'bulkedit.step1.single'"/>: 
            </ww:else>
            <ww:text name="'bulk.migrate.status.title'">
                <ww:param name="'value0'"><ww:property value="/bulkEditBean/targetProjectGV/string('name')" /></ww:param>
                <ww:param name="'value1'"><ww:property value="/bulkEditBean/targetIssueTypeObject/name" /></ww:param>
            </ww:text>
        </page:param>
        <page:param name="instructions">
            <ww:text name="'bulk.migrate.status.instructions'">
                <ww:param name="'value0'">
                    <ww:iterator value="./bulkEditBean/issueTypeObjects" status="'status'">
                        <ww:component name="'issuetype'" template="constanticon.jsp">
                          <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                          <ww:param name="'iconurl'" value="./iconUrl" />
                          <ww:param name="'alt'"><ww:property value="./name" /></ww:param>
                        </ww:component> <strong><ww:property value="./name" /></strong><ww:if test="@status/last == false">, </ww:if>
                    </ww:iterator>
                </ww:param>
                <ww:param name="'value1'">
                    <ww:iterator value="./bulkEditBean/projects" status="'status'">
                        <strong><ww:property value="./string('name')" /></strong><ww:if test="@status/last == false">, </ww:if>
                    </ww:iterator>
                </ww:param>
                <ww:param name="'value2'">
                    <ww:component name="'issuetype'" template="constanticon.jsp">
                      <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                      <ww:param name="'iconurl'" value="/bulkEditBean/targetIssueTypeObject/iconUrl" />
                      <ww:param name="'alt'"><ww:property value="/bulkEditBean/targetIssueTypeObject/name" /></ww:param>
                    </ww:component> <strong><ww:property value="/bulkEditBean/targetIssueTypeObject/name" /></strong>
                </ww:param>
                <ww:param name="'value3'"><strong><ww:property value="/bulkEditBean/targetProjectGV/string('name')" /></strong></ww:param>
            </ww:text>

            <ww:text name="'bulk.move.statusmapping'" />.
        </page:param>
        <%@include file="/secure/views/bulkedit/includes/statusmapping.jsp"%>
    </page:applyDecorator>
</body>
</html>

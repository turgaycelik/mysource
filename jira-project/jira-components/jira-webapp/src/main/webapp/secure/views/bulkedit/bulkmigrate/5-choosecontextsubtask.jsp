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
    <page:applyDecorator name="bulkpanel" >
        <page:param name="title">
        	<ww:if test="/bulkEditBean/singleMode == false">
                <ww:text name="'bulkedit.step3'"/>: 
            </ww:if>
            <ww:else>
            	<ww:text name="'bulkedit.step1.single'"/>: 
            </ww:else>
            <ww:text name="'bulk.migrate.overview.subtask.title'">
                <ww:param name="'value0'"><strong><ww:property value="/rootBulkEditBean/relatedMultiBulkMoveBean/currentBulkEditBean/targetProjectGV/string('name')"/></strong></ww:param>
                <ww:param name="'value1'"><strong><ww:property value="/rootBulkEditBean/relatedMultiBulkMoveBean/currentBulkEditBean/targetIssueTypeGV/string('name')"/></strong></ww:param>
            </ww:text>
        </page:param>
        <page:param name="action">BulkMigrateChooseSubTaskContext.jspa</page:param>
        <page:param name="instructions">
            <p>
            <ww:text name="'bulk.migrate.overview.subtask.instructions'">
                <ww:param name="'value0'"><strong><ww:property value="/currentRootBulkEditBean/selectedIssues/size"/></strong></ww:param>
                <ww:param name="'value1'"><strong><ww:property value="/rootBulkEditBean/relatedMultiBulkMoveBean/currentBulkEditBean/targetIssueTypeGV/string('name')"/></strong></ww:param>
                <ww:param name="'value2'"><strong><ww:property value="/rootBulkEditBean/relatedMultiBulkMoveBean/currentBulkEditBean/targetProjectGV/string('name')"/></strong></ww:param>
                <ww:param name="'value3'"><strong><ww:property value="/multiBulkMoveBean/issuesInContext/size"/></strong></ww:param>
            </ww:text>
            </p>
        </page:param>
        <ui:component name="'subTaskPhase'" template="hidden.jsp"  />
        <ww:iterator value="/rootBulkEditBean/relatedMultiBulkMoveBean/bulkEditBeans" status="'status'">
            <ww:if test="./value/relatedMultiBulkMoveBean/bulkEditBeans != null">
                <ww:iterator value="./value/relatedMultiBulkMoveBean/bulkEditBeans" status="'status'">
                    <%@include file="/secure/views/bulkedit/includes/chooseContext.jsp" %>
                </ww:iterator>
            </ww:if>
        </ww:iterator>
    </page:applyDecorator>
</body>
</html>

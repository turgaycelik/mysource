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
        <page:param name="action">BulkMigratePerform.jspa</page:param>
        <page:param name="title">
             <ww:if test="/bulkEditBean/singleMode == false">
                <ww:text name="'bulkedit.step4'"/>: <ww:text name="'bulk.migrate.confirm.title'"/>
            </ww:if>
            <ww:else>
            	<ww:text name="'bulkedit.step2.single'"/>: <ww:text name="'bulk.migrate.confirm.title'"/>
            </ww:else>
        </page:param>
        <page:param name="instructions">
            <p><ww:text name="'bulk.migrate.confirm.instructions'"/></p>
            <!-- Send Mail confirmation -->
            <ww:if test="/canDisableMailNotifications() == true && /bulkEditBean/hasMailServer == true">
                <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications-confirmation.jsp"/>
            </ww:if>
            <ul class="optionslist">
            <ww:iterator value="/multiBulkMoveBean/bulkEditBeans" status="'status'">
               <li>
                <a href="#<ww:property value="./key/project/string('id')" /><ww:property value="./key/issueTypeObject/id" />"><ww:property value="./key/project/string('name')" /> - <ww:property value="./key/issueTypeObject/name" /></a>
                <ww:property value="./value/relatedMultiBulkMoveBean/bulkEditBeans">
                    <ww:if test=".">
                     <ul>
                       <ww:iterator value="." status="'status'">
                           <li><a href="#<ww:property value="./key/project/string('id')" /><ww:property value="./key/issueTypeObject/id" />"><ww:property value="./key/project/string('name')" /> - <ww:property value="./key/issueTypeObject/name" /></a></li>
                      </ww:iterator>
                       </ul>
                    </ww:if>
                </ww:property>

                </li>
            </ww:iterator>
            </ul>
        </page:param>
        <ui:component name="'subTaskPhase'" template="hidden.jsp"  />
        <ww:iterator value="/multiBulkMoveBean/bulkEditBeans" status="'status'">
            <%@include file="confirm.jsp"%>
            <ww:if test="./value/relatedMultiBulkMoveBean/bulkEditBeans != null">
                <ww:iterator value="./value/relatedMultiBulkMoveBean/bulkEditBeans" status="'status'">
                    <%@include file="confirm.jsp"%>
                </ww:iterator>
            </ww:if>
        </ww:iterator>
    </page:applyDecorator>
</body>
</html>
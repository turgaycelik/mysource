<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
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
        <page:param name="action">BulkMigrateSetFields.jspa</page:param>
        <page:param name="title">
            <ww:if test="/bulkEditBean/singleMode == false">
                <ww:text name="'bulkedit.step3'"/>: 
            </ww:if>
            <ww:else>
            	<ww:text name="'bulkedit.step1.single'"/>: 
            </ww:else>
            <ww:text name="'bulk.migrate.fields.title'">
                <ww:param name="'value0'"><ww:property value="/bulkEditBean/targetProjectGV/string('name')" /></ww:param>
                <ww:param name="'value1'"><ww:property value="/bulkEditBean/targetIssueTypeObject/name" /></ww:param>
            </ww:text>
        </page:param>
        <page:param name="instructions">
            <p>
                <ww:text name="'bulk.migrate.fields.instructions'">
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
                </ww:text>
            </p>

            <%--<%@ include file="/secure/views/bulkedit/updatefieldsinstruction.jsp"%>--%>

            <ww:if test="hasAvailableActions == true">
                <%@ include file="/secure/views/bulkedit/updatefieldsinstruction.jsp"%>
            </ww:if>
            <ww:else>
                <p><ww:text name="'bulk.move.cannotperform'" /></p>
            </ww:else>

            <ww:property value="/bulkEditBean/messagedFieldLayoutItems">
                <ww:if test=". != null && ./empty == false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <table>
                            <tr>
                                <td><b><ww:text name="'bulk.move.fieldname'" /></b></td>
                                <td><b><ww:text name="'bulk.move.message'" /></b></td>
                            </tr>
                            <ww:iterator value="./keySet()">
                                <tr valign="top">
                                    <td>
                                        <i>Comment<ww:text name="."/></i>:
                                    </td>
                                    <ww:if test="/bulkEditBean/messagedFieldLayoutItems/(.)/warning == true">
                                        <td><span id="warning-<ww:text name="."/>" class="warning"><ww:property value="/bulkEditBean/messagedFieldLayoutItems/(.)/message" escape="'false'"/></span></td>
                                    </ww:if>
                                    <ww:elseIf test="/bulkEditBean/messagedFieldLayoutItems/(.)/fatal == true">
                                        <td><span id="error-<ww:text name="."/>" class="errMsg"><ww:property value="/bulkEditBean/messagedFieldLayoutItems/(.)/message" escape="'false'"/></span></td>
                                    </ww:elseIf>
                                </tr>
                            </ww:iterator>
                        </table>
                    </aui:param>
                </aui:component>
                </ww:if>
            </ww:property>
        </page:param>

        <%@include file="/secure/views/bulkedit/fielddetails.jsp"%>

        <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications.jsp"/>
    </page:applyDecorator>
</body>
</html>

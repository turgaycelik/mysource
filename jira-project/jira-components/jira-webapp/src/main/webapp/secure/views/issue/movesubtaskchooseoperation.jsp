<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%--STEP 1 - Move Sub Task: Choose Operation--%>
<html>
<head>
	<title><ww:text name="'movesubtask.title'"/></title>
</head>
<body>
    <page:applyDecorator name="bulkops-general">
        <page:param name="pageTitle"><ww:text name="'moveissue.title'"/></page:param>
        <page:param name="navContentJsp">/secure/views/issue/movetaskpane.jsp</page:param>

            <page:applyDecorator name="jiraform">
                <page:param name="title"><ww:text name="'movesubtask.title'"/>: <ww:text name="'move.chooseoperation.title'"/></page:param>
                <page:param name="description">
                    <ww:text name="'move.subtask.step1.desc'">
                       <ww:param name="'value0'"><ww:property value="/issue/string('key')"/></ww:param>
                   </ww:text>
                </page:param>
                <page:param name="action"><ww:url page="MoveSubTaskChooseOperation.jspa"><ww:param name="'id'" value="/issue/string('id')"/></ww:url></page:param>
                <page:param name="width">100%</page:param>
                <page:param name="name">movesubtask</page:param>
                <page:param name="cancelURI"><ww:url value="/issuePath" atltoken="false" /></page:param>
                <page:param name="submitId">next_submit</page:param>
                <page:param name="submitName"><ww:text name="'common.forms.next'"/> &gt;&gt;</page:param>
                    <tr>
                        <td colspan="2">
                            <table class="aui aui-table-rowhover">
                                <ww:iterator value="moveSubTaskOperations" status="'status'">
                                    <%-- if the operation is available draw the radio button --%>
                                    <ww:if test="/canPerform(.) == true">
                                        <tr id="cell<ww:property value="./nameKey"/>">
                                            <td width="1%">
                                                <input type="radio" name="operation" id="<ww:property value="./nameKey"/>_id" value="<ww:property value="./nameKey"/>">
                                            </td>
                                            <td>
                                                <label for="<ww:property value="./nameKey"/>_id"><ww:text name="./nameKey"/></label>
                                            </td>
                                            <td>
                                                <label for="<ww:property value="./nameKey"/>_id"><ww:text name="./descriptionKey"/></label>
                                            </td>
                                        </tr>
                                    </ww:if>
                                    <ww:else>
                                        <tr>
                                            <td width="1%">
                                                <ww:text name="'bulkedit.constants.na'"/>
                                            </td>
                                            <td>
                                                <ww:text name="./nameKey"/>
                                            </td>
                                            <td>
                                                <ww:text name="/cannotPerformMessageKey(.)">
                                                <ww:param name="'value0'"><span class="status-inactive"></ww:param>
                                                <ww:param name="'value1'"></span></ww:param>
                                                </ww:text>
                                            </td>
                                        </tr>
                                    </ww:else>
                                 </ww:iterator>
                            </table>
                        </td>
                    </tr>
                <ui:component name="'id'" template="hidden.jsp"  theme="'single'" />
            </page:applyDecorator>

    </page:applyDecorator>
</body>
</html>

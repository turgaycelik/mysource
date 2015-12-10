<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
    <script language="javascript">
        function selectCellRadioBox(cell)
        {
            var id = cell.id.substring(4, cell.id.length);
            document.forms['bulkedit_chooseoperation'].elements[id + '_id'].checked = true;
        }
    </script>
</head>
<body>
    <!-- Step 2 - Bulk Operation: Choose Operation -->
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                <ui:param name="'content'">
                    <h1><ww:text name="'bulkedit.title'"/></h1>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'id'" value="'stepped-process'" />
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelNav'">
                <ui:param name="'content'">
                    <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
                </ui:param>
            </ui:soy>
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
                        <ui:param name="'content'">
                            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                                <ui:param name="'content'">
                                    <h2><ww:text name="'bulkedit.step2'"/>: <ww:text name="'bulkedit.chooseoperation.title'"/></h2>
                                </ui:param>
                            </ui:soy>
                        </ui:param>
                    </ui:soy>
                    <ww:if test="/hasAvailableOperations == true">
                        <p>
                            <ww:text name="'bulkedit.chooseoperation.desc'">
                                <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                            </ww:text>
                        </p>
                    </ww:if>
                    <ww:else>
                        <p>
                            <ww:text name="'bulkedit.chooseoperation.unavailable'">
                                <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                            </ww:text>
                        </p>
                    </ww:else>

                    <page:applyDecorator id="bulkedit_chooseoperation" name="auiform">
                        <page:param name="action">BulkChooseOperation.jspa</page:param>
                        <page:param name="useCustomButtons">true</page:param>
                            <table class="aui">
                                <tbody>
                                    <ww:iterator value="bulkOperations" status="'status'">
                                        <tr onclick="selectCellRadioBox(this)" id="cell<ww:property value="./nameKey"/>">
                                            <%-- if the operation is available draw the radio button --%>
                                            <ww:if test="/canPerform(.) == true">
                                                <td class="cell-type-collapsed">
                                                    <input type="radio" name="operation" id="<ww:property value="./nameKey"/>_id" value="<ww:property value="./nameKey"/>">
                                                </td>
                                                <td>
                                                    <label for="<ww:property value="./nameKey"/>_id"><ww:text name="./nameKey"/></label>
                                                </td>
                                                <td>
                                                    <label for="<ww:property value="./nameKey"/>_id"><ww:text name="./descriptionKey"/></label>
                                                </td>
                                            </ww:if>
                                            <ww:else>
                                                <td class="cell-type-collapsed">
                                                    <ww:text name="'bulkedit.constants.na'"/>
                                                </td>
                                                <td>
                                                    <ww:text name="./nameKey"/>
                                                </td>
                                                <td>
                                                    <ww:text name="./cannotPerformMessageKey">
                                                        <ww:param name="'value0'"><strong></ww:param>
                                                        <ww:param name="'value1'"></strong></ww:param>
                                                        <ww:param name="'value2'"><ww:property value="/bulkEditBean/selectedIssues/size"/></ww:param>
                                                    </ww:text>
                                                </td>
                                            </ww:else>
                                        </tr>
                                    </ww:iterator>
                                </tbody>
                            </table>
                            <%@include file="bulkchooseoperation_submit_buttons.jsp"%>
                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>

<%@ page import="com.atlassian.jira.util.BrowserUtils,
                 com.atlassian.jira.web.action.issue.bulkedit.BulkEdit,
                 com.atlassian.jira.web.component.IssueTableLayoutBean"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <!-- Step 4 - Bulk Operation: Confirmation for EDIT -->
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
                                    <h2><ww:text name="'bulkedit.step4'"/>: <ww:text name="'bulkedit.step4.title'"/></h2>
                                </ui:param>
                            </ui:soy>
                        </ui:param>
                    </ui:soy>

                    <h3><ww:text name="'bulkedit.confirm.updatedfields'"/></h3>

                    <page:applyDecorator id="bulkedit_confirmation" name="auiform">
                        <page:param name="action">BulkEditPerform.jspa</page:param>
                        <page:param name="useCustomButtons">true</page:param>

                        <table class="aui aui-table-rowhover" id="updatedfields">
                            <thead>
                                <tr>
                                    <th><ww:text name="'bulk.move.fieldname'" /></th>
                                    <th><ww:text name="'bulk.move.fieldvalue'" /></th>
                                </tr>
                            </thead>
                            <tbody>
                                <ww:iterator value="/bulkEditBean/actions/values">
                                    <tr>
                                        <td style="max-width: 200px; width: 200px; word-wrap: break-word;"><ww:property value="./fieldName"/></td>
                                        <td><ww:property value="/fieldViewHtml(./field)" escape="false" /></td>
                                    </tr>
                                </ww:iterator>
                            </tbody>
                        </table>

                        <!-- Send Mail confirmation -->
                        <ww:if test="/canDisableMailNotifications() == true && /bulkEditBean/hasMailServer == true">
                            <jsp:include page="/includes/bulkedit/bulkedit-sendnotifications-confirmation.jsp"/>
                        </ww:if>

                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">info</aui:param>
                            <aui:param name="'messageHtml'">
                                <p><ww:text name="'bulkedit.confirm.warning.about.blanks'"/></p>
                                <p>
                                    <ww:text name="'bulkedit.confirm.msg'">
                                        <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                                    </ww:text>
                                </p>
                            </aui:param>
                        </aui:component>

                        <p>
                            <input class="aui-button" type="submit" id="confirm" name="<ww:text name="'common.forms.confirm'"/>" value="<ww:text name="'common.forms.confirm'"/>"
                               accessKey="<ww:text name="'common.forms.submit.accesskey'"/>"
                               title="<ww:text name="'common.forms.submit.tooltip'">
                               <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                               <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                               </ww:text>"
                            />
                            <a class="aui-button aui-button-link" id="cancel" href="<ww:url value="'BulkCancelWizard.jspa'" atltoken="false"/>"><ww:text name="'common.forms.cancel'"/></a>
                        </p>
                        <div id="updatedIssueTable">
                            <%-- Set this so that it can be used further down --%>
                            <ww:property value="/" id="bulkEdit" />
                            <%
                                BulkEdit bulkEdit = (BulkEdit) request.getAttribute("bulkEdit");
                            %>
                            <ui:issuetable layoutBean="<%=bulkEdit.getIssueTableLayoutBean()%>"
                                           issues="<%=bulkEdit.getBulkEditBean().getSelectedIssues()%>"/>
                        </div>
                        <p>
                            <input class="aui-button" type="submit" name="<ww:text name="'common.forms.confirm'"/>" value="<ww:text name="'common.forms.confirm'"/>"
                               accessKey="<ww:text name="'common.forms.submit.accesskey'"/>"
                               title="<ww:text name="'common.forms.submit.tooltip'">
                               <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                               <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                               </ww:text>"
                            />
                            <a class="aui-button aui-button-link" id="cancel-bottom" href="<ww:url value="'BulkCancelWizard.jspa'" atltoken="false"/>"><ww:text name="'common.forms.cancel'"/></a>
                        </p>
                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>

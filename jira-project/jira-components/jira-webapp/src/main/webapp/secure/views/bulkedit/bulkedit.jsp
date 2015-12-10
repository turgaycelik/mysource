<%@ page import="com.atlassian.jira.ComponentManager,
                 com.atlassian.jira.util.BrowserUtils,
                 com.atlassian.jira.web.action.issue.bulkedit.BulkEdit1,
                 com.atlassian.jira.web.component.IssueTableLayoutBean"%>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ page import="java.util.List" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
    <script>
        AJS.$(function(){
            AJS.$('#bulkedit-select-all').change(function(){
                var value = AJS.$(this).is(':checked');
                AJS.$(this).closest('form').find(':checkbox').prop("checked", value);
            });
        });
    </script>
</head>
<body>
    <!-- STEP 1 - Bulk Operation: Choose Issues-->
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
                                    <h2><ww:text name="'bulkedit.step1'"/>: <ww:text name="'bulkedit.step1.title'"/></h2>
                                </ui:param>
                            </ui:soy>
                        </ui:param>
                    </ui:soy>
                    <ww:if test="/bulkLimited == true">
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">warning</aui:param>
                            <aui:param name="'messageHtml'">
                                <p><ww:text name="'bulk.edit.limited'"><ww:param name="'value0'" value="/tempMax"/></ww:text></p>
                            </aui:param>
                        </aui:component>
                    </ww:if>

                    <page:applyDecorator id="bulkedit" name="auiform">
                        <page:param name="action">BulkEdit1.jspa</page:param>
                        <page:param name="useCustomButtons">true</page:param>
                        <p>
                            <input class="aui-button" type="submit" name="Next" id="next" value="<ww:text name="'common.forms.next'"/>"
                               title="<ww:text name="'common.forms.submit.tooltip'">
                               <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                               <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                               </ww:text>"
                            />
                            <a href="<ww:url value="'BulkCancelWizard.jspa'" atltoken="false" />" class="aui-button aui-button-link" id="cancel"><ww:text name="'common.forms.cancel'" /></a>
                        </p>

                        <%-- Set this so that it can be used further down --%>
                        <ww:property value="/" id="bulkEdit" />
                        <%
                            BulkEdit1 bulkEdit1 = (BulkEdit1) request.getAttribute("bulkEdit");
                        %>
                        <ui:issuetable layoutBean="<%=bulkEdit1.getIssueTableLayoutBean()%>"
                                       issues="<%=bulkEdit1.getBulkEditBean().getIssuesFromSearchRequest()%>"/>

                        <p>
                            <input class="aui-button" type="submit" name="Next" id="next-bottom" value="<ww:text name="'common.forms.next'"/>"
                               title="<ww:text name="'common.forms.submit.tooltip'">
                               <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                               <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                               </ww:text>"
                            />
                            <a href="<ww:url value="'BulkCancelWizard.jspa'" atltoken="false" />" class="aui-button aui-button-link" id="cancel-bottom"><ww:text name="'common.forms.cancel'" /></a>
                        </p>
                        <input type="hidden" name="tempMax" value="<ww:property value="/tempMax"/>"/>
                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>

<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<decorator:usePage id="p" />
<% if (p.isPropertySet("pageTitle")) { %>
<ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
    <ui:param name="'mainContent'"><h1><decorator:getProperty property="pageTitle"/></h1></ui:param>
</ui:soy>
<% } %>
<ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
    <ui:param name="'id'" value="'stepped-process'" />
    <ui:param name="'content'">
        <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelNav'">
            <ui:param name="'content'">
                <% if (p.isPropertySet("navContentJsp")) { %>
                    <jsp:include page='<%= p.getProperty("navContentJsp")%>' flush="false" />
                <% } %>
            </ui:param>
        </ui:soy>
        <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
            <ui:param name="'content'">
                <decorator:body />
            </ui:param>
        </ui:soy>
    </ui:param>
</ui:soy>

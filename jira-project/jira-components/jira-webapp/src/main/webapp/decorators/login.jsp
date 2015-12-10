<%@ page import="com.atlassian.jira.web.util.ProductVersionDataBeanProvider" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww"%>
<%@ taglib uri="webwork" prefix="ui"%>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%
    WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
    webResourceManager.requireResourcesForContext("atl.general");
    webResourceManager.requireResourcesForContext("jira.general");
%>
<!DOCTYPE html>
<html lang="<%= ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getLocale().getLanguage() %>">
<head>
    <%@ include file="/includes/decorators/aui-layout/head-common.jsp" %>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
    <decorator:head/>
</head>
<body id="jira" class="aui-layout aui-theme-default page-type-login <decorator:getProperty property="body.class" /> <page:capClass />" <%= ComponentAccessor.getComponent(ProductVersionDataBeanProvider.class).get().getBodyHtmlAttributes() %>>
    <div id="page">
        <page:capHide value="IFRAME">
            <header id="header" role="banner">
                <%@ include file="/includes/decorators/aui-layout/notifications-header.jsp" %>
                <%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
                <%@ include file="/includes/decorators/aui-layout/header.jsp" %>
            </header>
        </page:capHide>
        <%@ include file="/includes/decorators/aui-layout/notifications-content.jsp" %>
        <section id="content" role="main">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
                <ui:param name="'content'">
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                        <ui:param name="'content'">
                            <decorator:body />
                        </ui:param>
                    </ui:soy>
                </ui:param>
            </ui:soy>
        </section>
        <page:capHide value="IFRAME">
            <footer id="footer" role="contentinfo">
                <%--<%@ include file="/includes/decorators/aui-layout/notifications-footer.jsp" %>--%>
                <%@ include file="/includes/decorators/aui-layout/footer.jsp" %>
            </footer>
        </page:capHide>
    </div>
</body>
</html>

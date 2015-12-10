<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager" %>
<%@ page import="com.atlassian.jira.web.util.ProductVersionDataBeanProvider" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "navigator"
    final WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
    webResourceManager.requireResourcesForContext("jira.navigator");
    webResourceManager.requireResourcesForContext("atl.general");
    webResourceManager.requireResourcesForContext("jira.general");

    final KeyboardShortcutManager navigatorKeyboardShortcutManager = ComponentAccessor.getComponent(KeyboardShortcutManager.class);
    navigatorKeyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issueaction);
    navigatorKeyboardShortcutManager.requireShortcutsForContext(KeyboardShortcutManager.Context.issuenavigation);
%>
<!DOCTYPE html>
<html lang="<%= ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getLocale().getLanguage() %>">
<head>
    <%@ include file="/includes/decorators/aui-layout/head-common.jsp" %>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
    <decorator:head/>
</head>
<body id="jira" class="aui-layout aui-theme-default page-type-issuenav <decorator:getProperty property="body.class" />" <%= ComponentAccessor.getComponent(ProductVersionDataBeanProvider.class).get().getBodyHtmlAttributes() %>>
<div id="page">
    <header id="header" role="banner">
        <%@ include file="/includes/decorators/aui-layout/notifications-header.jsp" %>
        <%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
        <%@ include file="/includes/decorators/aui-layout/header.jsp" %>
    </header>
    <%@ include file="/includes/decorators/aui-layout/notifications-content.jsp" %>
    <section id="content" role="main">
        <decorator:body />
    </section>
    <footer id="footer" role="contentinfo">
        <%--<%@ include file="/includes/decorators/aui-layout/notifications-footer.jsp" %>--%>
        <%@ include file="/includes/decorators/aui-layout/footer.jsp" %>
    </footer>
</div>
</body>
</html>

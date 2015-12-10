<%@ page import="com.atlassian.jira.plugin.navigation.HeaderFooterRendering" %>
<%
    //
    // IDEA gives you a warning below because it cant resolve JspWriter.  I don't know why but its harmless
    //
    ComponentAccessor.getComponent(HeaderFooterRendering.class).includeTopNavigation(out, request, JspDecoratorUtils.getBody());
%>
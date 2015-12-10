<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%
	ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

    if ("true".equalsIgnoreCase(ap.getString(APKeys.JIRA_SETUP)))
    {
        response.sendRedirect(request.getContextPath() + "/secure/project/ViewProjects.jspa");
        return;
    }
%>

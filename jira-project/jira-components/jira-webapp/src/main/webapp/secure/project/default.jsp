<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties,
                 com.atlassian.jira.config.properties.APKeys,
                 com.atlassian.jira.component.ComponentAccessor"%>
<%
	ApplicationProperties ap = ComponentAccessor.getApplicationProperties();

    if ("true".equalsIgnoreCase(ap.getString(APKeys.JIRA_SETUP)))
    {
        response.sendRedirect(request.getContextPath() + "/secure/project/ViewProjects.jspa");
        return;
    }
%>

<%@ page import="com.atlassian.jira.util.ComponentFactory" %>
<%@ page import="com.atlassian.jira.config.ReindexMessageManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.user.util.UserUtil" %>
<%@ page import="com.atlassian.jira.security.PermissionManager" %>
<%@ page import="com.atlassian.jira.security.Permissions" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%
    ReindexMessageManager reindexMessageManager = ComponentAccessor.getComponentOfType(ReindexMessageManager.class);
    JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
    final boolean isAdmin = ComponentAccessor.getComponentOfType(PermissionManager.class).hasPermission(Permissions.ADMINISTER, authenticationContext.getUser());
    final String message = reindexMessageManager.getMessage(authenticationContext.getLoggedInUser());
    if (isAdmin && !StringUtils.isBlank(message))
    {
%>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">info</aui:param>
    <aui:param name="'messageHtml'"><%= message %></aui:param>
</aui:component>
<%
    }

    UserUtil userUtil = ComponentAccessor.getComponentOfType(UserUtil.class);
    if (isAdmin && userUtil.hasExceededUserLimit())
    {
%>
<aui:component id="'adminMessages2'" template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'messageHtml'">
        <ww:text name="'admin.globalpermissions.user.limit.warning'">
            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/ViewLicense!default.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
    </aui:param>
</aui:component>
<%
    }
%>
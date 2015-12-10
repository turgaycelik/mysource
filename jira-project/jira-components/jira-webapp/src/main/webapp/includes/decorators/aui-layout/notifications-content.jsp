<%@ page import="com.atlassian.jira.admin.AnnouncementBanner" %>
<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%
    AnnouncementBanner banner = ComponentAccessor.getComponentOfType(AnnouncementBanner.class);
    if (banner.isDisplay())
    {
%>
<div id="announcement-banner" class="alertHeader">
    <%= banner.getViewHtml() %>
</div>
<%
    }
%>

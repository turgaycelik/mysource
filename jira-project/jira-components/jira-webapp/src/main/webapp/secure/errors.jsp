<%@ page import="com.atlassian.jira.license.LicenseJohnsonEventRaiser" %>
<%@ page import="com.atlassian.johnson.JohnsonEventContainer" %>
<%@ page import="com.atlassian.johnson.event.Event" %>
<%@ page import="com.atlassian.johnson.event.EventType" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.johnson.event.EventLevel" %>
<%@ page import="com.atlassian.jira.web.util.MetalResourcesManager" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<!DOCTYPE html>
<html>
<head>
    <title><ww:text name="'system.error.access.constraints.title'"/></title>
    <meta http-equiv="refresh" content="30"/>
    <meta name="decorator" content="none"/>
    <%
        MetalResourcesManager.includeMetalResources(out, request.getContextPath());
    %>
</head>
<body class="aui-page-focused aui-page-focused-medium">
<div id="page">
    <section id="content">
        <div class="aui-page-panel">
            <div class="aui-page-panel-inner">
                <section class="aui-page-panel-content">

                    <header class="aui-page-header">
                        <div class="aui-page-header-inner">
                            <div class="aui-page-header-main">
                                <h1><ww:text name="'system.error.access.constraints.title'"/></h1>
                            </div>
                        </div>
                    </header>
                    <%
                        JohnsonEventContainer appEventContainer = JohnsonEventContainer.get(pageContext.getServletContext());

                        // If there are events outstanding, then display them in a table.
                        if (appEventContainer.hasEvents())
                        {
                            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    %>

                    <div class="aui-message warning">
                        <ww:text name="'system.error.access.constraints.desc'"/>
                        <span class="aui-icon icon-warning"></span>
                    </div>

                    <table class="aui">
                        <thead>
                        <tr>
                            <th width="40%">
                                <ww:text name="'common.words.description'"/>
                            </th>
                            <th width="10%">
                                <ww:text name="'common.words.time'"/>
                            </th>
                            <th>
                                <ww:text name="'common.words.level'"/>
                            </th>
                            <th width="40%">
                                <ww:text name="'common.words.exception'"/>
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <%
                            boolean onlyWarnings = true;
                            final Collection events = appEventContainer.getEvents();
                            for (Iterator iterator = events.iterator(); iterator.hasNext(); )
                            {
                                Event event = (Event) iterator.next();
                                onlyWarnings &= EventLevel.WARNING.equals(event.getLevel().getLevel());
                        %>
                        <tr>
                            <td>
                                <% if (EventType.get("export-illegal-xml").equals(event.getKey())) { %>
                                    <ww:component template="help.jsp" name="'autoexport'"><ww:param name="'helpURLFragment'"/></ww:component><br/>
                                <% } %>

                                <%= event.getDesc() %><br/>

                                <% if (event.hasProgress()) { %>
                                    <br/>
                                    <ww:text name="'system.error.progress.completed'">
                                        <ww:param name="value0"><%=event.getProgress()%></ww:param>
                                    </ww:text>
                                <% } %>

                                <% if (EventType.get(LicenseJohnsonEventRaiser.LICENSE_TOO_OLD).equals(event.getKey())) { %>
                                    <br/>
                                    <a href="<%= request.getContextPath() %>/secure/ConfirmNewInstallationWithOldLicense!default.jspa"><ww:text name="'system.error.edit.license.or.evaluate'"/></a>
                                <% } else if (EventType.get(LicenseJohnsonEventRaiser.CLUSTERING_UNLICENSED).equals(event.getKey()) ||
                                        EventType.get(LicenseJohnsonEventRaiser.SUBSCRIPTION_EXPIRED).equals(event.getKey())) { %>
                                    <br/>
                                    <a href="<%= request.getContextPath() %>/secure/ConfirmNewInstallationWithOldLicense!default.jspa"><ww:text name="'system.error.edit.license'"/></a>
                                <% } else if (EventType.get("export-illegal-xml").equals(event.getKey())) { %>
                                    <br/>
                                    <a href="<%= request.getContextPath() %>/secure/CleanData!default.jspa"><ww:text name="'system.error.clean.characters.from.database'"/></a>
                                    <br/>
                                    <ww:text name="'system.error.disable.export.on.upgrade.desc'">
                                        <ww:param name="value0"><b></ww:param>
                                        <ww:param name="value1"></b></ww:param>
                                    </ww:text> &nbsp;
                                <% } else if (EventType.get("index-lock-already-exists").equals(event.getKey())) { %>
                                    <p>
                                        <ww:text name="'system.error.unexpected.index.lock.found.desc1'"/>
                                        <br/>
                                        <br/>
                                        <%
                                            Object lockFiles = event.getAttribute("lockfiles");
                                            if (lockFiles != null)
                                            {
                                                out.println(lockFiles);
                                            }
                                        %>
                                        <br/>
                                        <br/>
                                        <ww:text name="'system.error.unexpected.index.lock.found.desc2'"/>
                                    </p>

                                    <p>
                                        <ww:text name="'system.error.unexpected.index.lock.found.desc3'">
                                            <ww:param name="value0"><strong></ww:param>
                                            <ww:param name="value1"></strong></ww:param>
                                        </ww:text>
                                    </p>
                                <% } else if (EventType.get("upgrade").equals(event.getKey())) {
                                    String exportFilePath = ComponentManager.getInstance().getUpgradeManager().getExportFilePath();
                                    if (TextUtils.stringSet(exportFilePath)) { %>
                                        <br/>
                                        <ww:text name="'system.error.data.before.upgrade.exported.to'">
                                            <ww:param name="value0"><%= exportFilePath %>
                                            </ww:param>
                                        </ww:text>
                                    <% } %>
                                <% } %>

                                <!-- (<ww:text name="'system.error.type'">
                                    <ww:param name="value0"><%= event.getKey().getType() %></ww:param>
                                </ww:text>) -->

                            </td>
                            <td><%=event.getDate()%></td>
                            <td><%=event.getLevel().getLevel()%></td>
                            <td>
                                <pre><%= event.getException() == null ? "" : event.getException() %></pre>
                            </td>
                        </tr>
                        <% }

                        if (onlyWarnings)
                        {
                            response.setHeader("Retry-After", "30");
                        }
                        %>
                        </tbody>
                    </table>
                    <% } else { %>
                    <div class="aui-message generic">
                        <ww:text name="'system.error.no.problems.accessing.jira'"/>
                        <span class="aui-icon icon-generic"></span>
                    </div>

                    <p>
                        <a href="<%=request.getContextPath()%>/secure/MyJiraHome.jspa"><ww:text name="'system.error.go.to.jira.home'"/></a>
                    </p>
                    <% } %>
                    </section>
                </div>
            </div>
        </section>
    </div>
</body>
</html>

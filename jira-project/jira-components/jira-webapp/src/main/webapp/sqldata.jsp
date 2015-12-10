<%@ page import="com.atlassian.jira.ofbiz.PerformanceSQLInterceptor" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="com.atlassian.jira.web.filters.accesslog.AccessLogImprinter" %>
<%@ page import="com.atlassian.jira.config.properties.JiraSystemProperties" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.util.lang.Pair" %>
<html>
<head>
    <style type="text/css">
        tr.odd, li.odd {background-color:#d3d3d3;}
    </style>
</head>
<body>
<%
    if(session != null && JiraSystemProperties.showPerformanceMonitor())
    {
        final Map<String, PerformanceSQLInterceptor.SQLPerfCache> storage =
                (Map<String, PerformanceSQLInterceptor.SQLPerfCache>) session.getAttribute(AccessLogImprinter.REQUEST_SQL_CACHE_STORAGE);
        final PerformanceSQLInterceptor.SQLPerfCache cache = storage.get(request.getParameter("requestId"));

        if(cache != null)
        {
%>
<h3>Invocations</h3>
<table>
    <thead>
    <th>SQL</th><th>#</th><th>Avg (ms)</th><th>Total (ms)</th>
    </thead>
    <%
            final Map<String,List<Long>> statements = cache.getStatements();
            long overallTotal = 0;
            long overallTotalInvocations = 0;
            int count =0;
            for (Map.Entry<String, List<Long>> entry : statements.entrySet())
            {
                final List<Long> value = entry.getValue();
                long totalTime = 0;
                for (Long timing : value)
                {
                    totalTime += timing;
                }
                overallTotal += totalTime;
                overallTotalInvocations += value.size();
                final long avg = totalTime / value.size();
    %>
                <tr <%if(count %2 == 1) {out.print("class=\"odd\"");}%>><td><%=TextUtils.htmlEncode(entry.getKey())%></td><td><%=value.size()%></td><td><%=avg%></td><td><%=totalTime%></td></tr>
        <%
                    count++;
                }

        %>
    <tr><td></td><td><strong><%=overallTotalInvocations%></strong></td><td></td><td><strong><%=overallTotal%></strong></td></tr>
</table>

<h3>Statements in Execution Order</h3>
<ol>
<%
    final List<Pair<String,String>> statementsInCallOrder = cache.getStatementsInCallOrder();
    count = 0;
    for (Pair<String, String> statement : statementsInCallOrder)
    {
        %>
        <li <%if(count %2 == 1) {out.print("class=\"odd\"");}%>>
            <a href="#" class="stack-trigger"><%=TextUtils.htmlEncode(statement.first())%></a>
            <div class="hidden"><%=TextUtils.br(TextUtils.htmlEncode(statement.second()))%></div>
        </li>
    <%
        count++;
    }

%>
</ol>
<% }
} %>

<script>
    AJS.$(function() {
        AJS.$(".stack-trigger").click(function(e) {
            e.preventDefault();
            var $stackDiv = AJS.$(this).siblings("div");
            if($stackDiv.hasClass("hidden")) {
                $stackDiv.removeClass("hidden");
            } else {
                $stackDiv.addClass("hidden");
            }
        })
    });
</script>
</body>
</html>
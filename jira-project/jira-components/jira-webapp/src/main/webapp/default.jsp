<%@page session="false" %>
<%
    if (request != null)
    {
        response.sendRedirect(request.getContextPath() + "/secure/MyJiraHome.jspa");
        return;
    }
%>
<html>
	<head>
		<title>Go to JIRA</title>
	</head>
	<body>
		<b><a href="<%= request.getContextPath() %>/secure/MyJiraHome.jspa">Click here!</a></b> to go to JIRA.
    </body>
</html>


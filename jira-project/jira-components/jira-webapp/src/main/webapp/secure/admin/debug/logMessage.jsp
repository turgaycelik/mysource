<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.opensymphony.util.TextUtils" %>

<%! public static final Logger log = Logger.getLogger("logMessage.jsp"); %>

<html>
<head>
    <title> Logs a Message to the atlassian-jira.log</title>
</head>
<body>
<p>
<%
    String message = request.getParameter("message");
    if (!StringUtils.isBlank(message))
    {
        log.info(message);
%>
    <b>message logged:</b> <%= TextUtils.htmlEncode(message) %>
<%
    }
%>
</p>
<p>
    <b>Log a message:</b>
    <form action="logMessage.jsp" method="POST">
        <input type="text" name="message" size="80" value="<%=TextUtils.htmlEncode(message)%>"/>
        <input type="reset" value="clear"/>
        <input type="submit" value="log"/>
    </form>
</p>
</body>
</html>

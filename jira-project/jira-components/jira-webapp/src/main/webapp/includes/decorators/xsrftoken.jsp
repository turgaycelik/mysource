<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.security.xsrf.XsrfTokenGenerator" %>
<%
    XsrfTokenGenerator xsrfTokenGenerator = (XsrfTokenGenerator) ComponentManager.getComponentInstanceOfType(XsrfTokenGenerator.class);
%>    
<meta id="atlassian-token" name="atlassian-token" content="<%=xsrfTokenGenerator.generateToken(request)%>">


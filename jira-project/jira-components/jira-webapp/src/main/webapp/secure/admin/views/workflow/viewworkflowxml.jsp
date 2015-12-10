<%@ taglib uri="webwork" prefix="ww" %><%@ page import="com.atlassian.jira.ComponentManager"%><%@ page import="webwork.util.ValueStack" %><%@ page import="com.atlassian.jira.issue.views.util.WordViewUtils" %><%
    response.setContentType("text/xml;charset=" + ComponentManager.getInstance().getApplicationProperties().getEncoding());
    // JRA-17446: need to set these headers so that IE can save this when viewed over HTTPS
    response.setHeader("Cache-Control", "private, must-revalidate, max-age=5"); // http 1.1
    response.setHeader("Pragma", ""); // http 1.0
    response.setDateHeader("Expires", System.currentTimeMillis() + 300); // prevent proxy caching
    // JRA-14463: need to set these headers so to prompt for download rather than display in browser
    ValueStack stack = (ValueStack)request.getAttribute("webwork.result");
    response.setHeader("content-disposition", "attachment; filename=" + stack.findValue("name") + ".xml");
%><ww:property value="xml" escape="false" />

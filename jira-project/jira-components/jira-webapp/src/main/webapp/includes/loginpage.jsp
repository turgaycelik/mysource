<%@ page import="com.atlassian.jira.component.ComponentAccessor,com.atlassian.jira.config.properties.APKeys"%>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ taglib prefix="ww" uri="webwork" %>
<h2><ww:text name="'login.welcome.to'"/> <%= TextUtils.htmlEncode(ComponentAccessor.getApplicationProperties().getDefaultBackedString(APKeys.JIRA_TITLE))%></h2>
<%@ include file="/includes/loginform.jsp" %>

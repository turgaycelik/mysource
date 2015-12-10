<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
	<title><ww:text name="'common.words.login.caps'"/></title>
    <meta name="decorator" content="login" />
</head>
<body>
    <page:capHide value="IFRAME">
        <header><h1><ww:text name="'login.welcome.to'"/> <%= TextUtils.htmlEncode(ComponentAccessor.getComponent(ApplicationProperties.class).getDefaultBackedString(APKeys.JIRA_TITLE))%></h1></header>
    </page:capHide>
    <%@ include file="/includes/loginform.jsp" %>
</body>
</html>
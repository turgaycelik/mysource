<%--
 - This JSP aims to replace all the security checks that were previously done in the issueaction.jsp decorator. If
 - callers don't have permission to see an issue the action should return "generic_issue_error" and actions.xml should
 - be configured to show this JSP.
--%>
<%@ page import="com.atlassian.jira.util.JiraUtils" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib prefix="aui" uri="webwork" %>
<html>
<head>
    <meta content="message" name="decorator" />
    <title><ww:text name="'admin.common.words.error'"/></title>
</head>
<body>
<header><h1><ww:text name="'common.words.error'" /></h1></header>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">error</aui:param>
    <aui:param name="'messageHtml'">
        <ww:if test="/issueExists == true">
            <ww:if test="remoteUser == null">
                <p><ww:text name="'generic.notloggedin.msg'"/></p>
                <p>
                <ww:text name="'generic.notloggedin.try.login'">
                    <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
                    <ww:param name="'value1'"></ww:param>
                </ww:text>
                <ww:if test="extUserManagement != true">
                    <% if (JiraUtils.isPublicMode()) { %>
                        <ww:text name="'noprojects.signup'">
                            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    <% } %>
                </ww:if>
                </p>
            </ww:if>
            <ww:else>
                <p><ww:text name="'generic.notloggedin.no.permission'"/></p>
            </ww:else>
        </ww:if>
        <ww:else>
            <p><ww:text name="'issue.wasdeleted'"/></p>
        </ww:else>
    </aui:param>
</aui:component>
</body>
</html>
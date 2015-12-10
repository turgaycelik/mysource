<%@ page import="com.atlassian.jira.util.JiraUtils"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
<head>
    <title><ww:text name="'showconstantshelp.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <div class="form-body">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ww:if test="remoteUser == null">
                    <p><ww:text name="'showconstantshelp.notloggedin'"/></p>
                    <p>
                        <ww:text name="'showconstantshelp.mustfirstlogin'">
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
                    <p><ww:text name="'showconstantshelp.error.permission'"/></p>
                </ww:else>
            </aui:param>
        </aui:component>
    </div>
</body>
</html>
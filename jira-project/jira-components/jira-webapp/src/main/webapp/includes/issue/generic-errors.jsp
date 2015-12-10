<%@ page import="com.atlassian.jira.util.JiraUtils"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:if test="hasErrorMessages == 'true'">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <ww:if test="hasErrorMessages == 'true'">
                <ww:iterator value="flushedErrorMessages">
                    <p><ww:property value="." /></p>
                </ww:iterator>
            </ww:if>
        </aui:param>
    </aui:component>
</ww:if>
<%  //if the issue exists show the permission warning and login link %>
<ww:if test="/issueExists == true">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'perm.violation.desc'"/></p>
            <%-- If we're viewing a valid issue but aren't logged in, show the login warning --%>
            <ww:if test="/remoteUser == null">
                <p>
                    <% if (JiraUtils.isPublicMode()) { %>
                    <ww:text name="'login.required.signup.description'">
                        <ww:param name="'value0'"><jira:loginlink returnUrl="'/browse/' + /key"><ww:text name="'login.required.login'"/></jira:loginlink></ww:param>
                        <ww:param name="'value1'"><a href="<%= request.getContextPath() %>/secure/Signup!default.jspa"></ww:param>
                        <ww:param name="'value2'"></a></ww:param>
                    </ww:text>
                    <% }
                    else
                    { %>
                    <ww:text name="'login.required.description'">
                        <ww:param name="'value0'"><jira:loginlink returnUrl="'/browse/' + /key"><ww:text name="'login.required.login'"/></jira:loginlink></ww:param>
                    </ww:text>
                    <% } %>
                </p>
            </ww:if>
            <p>
                <ww:text name="'contact.admin.for.perm'">
                    <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
</ww:if>
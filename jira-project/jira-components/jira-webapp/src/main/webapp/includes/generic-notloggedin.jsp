<%@ page import="com.atlassian.jira.util.JiraUtils"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:if test="hasErrorMessages == 'true'">
    <ul>
        <ww:iterator value="flushedErrorMessages">
            <li><ww:property /></li>
        </ww:iterator>
    </ul>
</ww:if>
<ww:if test="remoteUser == null">
    <p>
        <ww:text name="'generic.notloggedin.msg'"/>
    </p>
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
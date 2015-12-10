<%@ page import="com.atlassian.jira.util.JiraUtils"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:if test="remoteUser == null">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'createissue.notloggedin'"/></p>
            <p>
                <ww:text name="'createissue.mustfirstlogin'">
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
        </aui:param>
    </aui:component>
</ww:if>
<ww:elseIf test="project != null">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'createissue.projectnopermission'"/></p>
        </aui:param>
    </aui:component>
</ww:elseIf>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">error</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'createissue.invalid.pid'"/></p>
        </aui:param>
    </aui:component>
</ww:else>

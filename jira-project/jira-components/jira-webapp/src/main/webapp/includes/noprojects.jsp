<%@ page import="com.atlassian.jira.util.JiraUtils"%>
<%@ page import="com.atlassian.jira.util.I18nHelper" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:if test="applicationProperties/string('jira.setup') == 'true'">
    <ww:if test="remoteUser == null && projectManager/projects/size > 0">
        <p><ww:text name="'noprojects.notloggedin'"/></p>
        <p>
            <ww:text name="'noprojects.mustfirstlogin'">
                <ww:param name="'value0'"><jira:loginlink><ww:text name="'common.words.login'"/></jira:loginlink></ww:param>
                <ww:param name="'value1'"></a></ww:param>
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
        <ww:if test="projectManager/projects/size > 0">
            <p><ww:text name="'noprojects.nopermissions'"/></p>
            <p>
                <ww:if test="hasPermission('admin') == true">
                    <a href="<%= request.getContextPath() %>/secure/admin/default.jsp"><ww:text name="'noprojects.viewallprojects.link'"/></a> <ww:text name="'noprojects.viewallprojects.end'"/>.
                </ww:if>
                <ww:else>
                    <ww:text name="'noprojects.contactadmin.permissions'">
                        <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                    </ww:text>
                </ww:else>
            </p>
        </ww:if>
        <ww:else>
            <p><ww:text name="'noprojects'"/></p>
            <p>
                <ww:if test="hasPermission('admin') == true">
                    <a class="add-project-trigger" href="<%= request.getContextPath() %>/secure/admin/AddProject!default.jspa?nextAction=browseprojects&src=noprojectsmessage"><ww:text name="'noprojects.createprojectnow.link'"/></a>.
                </ww:if>
                <ww:else>
                    <ww:text name="'noprojects.contactadmin.createproject'">
                        <ww:param name="'value0'"><ww:property value="administratorContactLink" escape="'false'"/></ww:param>
                    </ww:text>
                </ww:else>
            </p>
        </ww:else>
    </ww:else>
</ww:if>
<%-- otherwise JIRA is not setup yet - show links --%>
<ww:else>
    <p><ww:text name="'noprojects.mustsetupfirst'"/></p>
    <p><ww:text name="'noprojects.createadmintocreateotheradmins'"/></p>
    <p><a href="<%= request.getContextPath() %>/secure/Setup!default.jspa"><ww:text name="'noprojects.setupjira.link'"/></a></p>
</ww:else>


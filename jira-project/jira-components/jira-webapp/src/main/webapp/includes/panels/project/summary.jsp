<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>

<ww:bean id="projectDescriptionRenderer" name="'com.atlassian.jira.web.bean.ProjectDescriptionRendererBean'"/>

<%--
expects the project GV on top of the ValueStack
--%>
<ww:if test="./long('avatar') != null">
    <jira:feature-check featureKey="rotp.project.shortcuts">
        <div class="project-shortcut-dialog-trigger" data-key="<ww:property value="string('key')"/>" data-name="<ww:property value="string('name')"/>" data-entity-type="jira.project">
    </jira:feature-check>
            <img id="project-avatar" alt="" class="project-avatar-48" height="48" src="<%= request.getContextPath() %>/secure/projectavatar?size=large&amp;pid=<ww:property value="./long('id')"/>&amp;avatarId=<ww:property value="./long('avatar')"/>" width="48" />
    <jira:feature-check featureKey="rotp.project.shortcuts">
        </div>
    </jira:feature-check>
</ww:if>
<ww:if test="stringSet(., 'description') == true">
	<ww:property value="string('description')" escape="false" /><br>
	<br>
</ww:if>
<b><ww:text name="'common.concepts.key'"/>:</b> <ww:property value="string('key')" /><br>
<b><ww:text name="'common.concepts.url'"/>:</b>
<ww:if test="string('url') != null && string('url') != ''">
	<a href="<ww:property value="string('url')" />"><ww:property value="string('url')" /></a><br>
</ww:if>
<ww:else><ww:text name="'browse.projects.no.url'"/><br></ww:else>

<b><ww:text name="'admin.projects.project.team'"/>:</b>
<br/>
<span style="padding-left:30px">
    <ww:text name="'common.concepts.projectlead'"/>:
    <ww:if test="/userExistsByKey(./string('lead')) == true">
        <jira:formatuser userKey="string('lead')" type="'profileLink'" id="'project_summary'"/>
    </ww:if>
    <ww:else>
        <span class="errLabel"><ww:property value="string('lead')" /></span>
    </ww:else>
</span>
<br/>
<span style="padding-left:30px">
    <ww:text name="'admin.projects.default.assignee'"/>: <ww:text name="/prettyAssigneeType(long('assigneetype'))" /> <ww:if test="/defaultAssigneeAssignable == false">(<span class="warning"><ww:text name="'admin.projects.warning.user.not.assignable'"/></span>)</ww:if>
</span>
<br/>
<ww:if test="/hasAssociateRolesPermission() == true">
    <span style="padding-left:30px">
        <ww:text name="'admin.projects.project.roles'"/>:
        <a href="<%=request.getContextPath()%>/plugins/servlet/project-config/<ww:property value="project/string('key')"/>/roles"><ww:text name="'admin.projects.project.roles.view.members'"/></a>
    </span>
    <br/>
</ww:if>

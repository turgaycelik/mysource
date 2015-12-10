<%@ taglib uri="jiratags" prefix="jira" %>
<% final String avatarSize = "small"; %>
<div class="p-list">
    <table class="aui">
        <thead>
            <tr>
                <th></th>
                <th class="project-list-name"><ww:text name="'common.concepts.project'" /></th>
                <th class="project-list-key"><ww:text name="'common.concepts.key'" /></th>
                <th class="project-list-lead"><ww:text name="'common.concepts.projectlead'" /></th>
                <th class="project-list-url"><ww:text name="'common.concepts.url'" /></th>
            </tr>
        </thead>
        <tbody class="projects-list">
        <ww:iterator>
            <tr>
                <td class="cell-type-icon" data-cell-type="avatar">
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.avatar.avatar'">
                        <ui:param name="'isProject'" value="true" />
                        <ui:param name="'size'"><%= avatarSize %></ui:param>
                        <ww:if test="./hasDefaultAvatar() == true">
                            <ui:param name="'avatarImageUrl'">
                                <ww:url value="'/secure/projectavatar'" atltoken="false" >
                                    <ww:param name="'size'"><%= avatarSize %></ww:param>
                                </ww:url>
                                <ui:param name="'extraClasses'" value="'jira-system-avatar'"/>
                            </ui:param>
                        </ww:if>
                        <ww:else>
                            <ww:if test="./avatar != null">
                                <ui:param name="'avatarImageUrl'">
                                    <ww:url value="'/secure/projectavatar'" atltoken="false">
                                        <ww:param name="'pid'" value="./id"/>
                                        <ww:param name="'size'"><%= avatarSize %></ww:param>
                                    </ww:url>
                                </ui:param>
                                <ww:if test="./avatar/systemAvatar == true">
                                    <ui:param name="'extraClasses'" value="'jira-system-avatar'"/>
                                </ww:if>
                            </ww:if>
                        </ww:else>
                    </ui:soy>
                </td>
                <td data-cell-type="name">
                    <a href="<%= request.getContextPath() %>/browse/<ww:property value="./key" />"><ww:property value="./name" /></a>
                </td>
                <td>
                    <ww:property value="./key" />
                </td>
                <td class="cell-type-user">
                    <ww:if test="./lead != null">
                        <jira:formatuser userKey="./projectLead/key" type="'profileLink'" id="'project_' + ./key + '_table'"/>
                    </ww:if>
                    <ww:else>
                        <i><ww:text name="'browse.projects.no.lead'" /></i>
                    </ww:else>
                </td>
                <td class="cell-type-url">
                    <ww:if test="./url != null && ./url != ''">
                        <a href="<ww:property value="./url" />"><ww:property value="./url" /></a>
                    </ww:if>
                    <ww:else>
                        <i><ww:text name="'browse.projects.no.url'" /></i>
                    </ww:else>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</div>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.schemes.issuesecurity.edit.issue.security.levels'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="security_schemes"/>
</head>
<body>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.edit.issue.security.levels'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">security_levels</page:param>
        <page:param name="postTitle">
            <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
                <ui:param name="'projects'" value="/usedIn"/>
            </ui:component>
        </page:param>
        <p>
        <ww:text name="'admin.schemes.issuesecurity.on.this.page'">
            <ww:param name="'value0'"><ww:property value="scheme/string('name')"/></ww:param>
        </ww:text>
        </p>
        <ww:text name="'admin.schemes.issuesecurity.each.security.level'"/>
        <p>
        <ww:text name="'admin.schemes.issuesecurity.issue.can.be.assigned.security.level'"/>
        </p>
        <p>
        <ww:text name="'admin.schemes.issuesecurity.be.sure.to.set.issue.security'"/>
        </p>
        <ul class="square">
            <li><ww:text name="'admin.schemes.issuesecurity.view.all.issue.security.schemes'">
                <ww:param name="'value0'"><b><a href="ViewIssueSecuritySchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></b></ww:param>
            </ww:text></li>
            <ww:if test="/scheme/long('defaultlevel') != null">
                <li><a href="<ww:url page="EditIssueSecurities!makeDefaultLevel.jspa"><ww:param name="'schemeId'" value="scheme/long('id')"/><ww:param name="'levelId'" value="'-1'"/></ww:url>"><ww:text name="'admin.schemes.issuesecurity.change.default.security.to.none'"/></a></li>
            </ww:if>
        </ul>
    </page:applyDecorator>

    <table class="aui aui-table-rowhover" id="issue-security-table">
        <thead>
            <tr>
                <th>
                    <ww:text name="'admin.schemes.issuesecurity.security.level'"/>
                </th>
                <th>
                    <ww:text name="'admin.common.words.users.groups.roles'"/>
                </th>
                <th width="10%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="securityLevels" status="'status'">
            <tr>
                <td>
                    <b><ww:property value="./name"/></b><ww:if test="../default(./id) == true"> (<ww:text name="'admin.common.words.default'"/>)</ww:if>
                    <div class="description"><ww:property value="./description" /></div>
                </td>
                <td>
                    <ww:if test="securities(.)/empty == false">
                        <ul>
                        <ww:iterator value="securities(.)">
                            <li>
	                        <ww:if test="/type(type) != null">
                                <ww:property value="/type(type)/displayName" />
                                <ww:property value="/formatSecurityTypeParameter(type, ../../type(type)/argumentDisplay(parameter))" />
			                </ww:if>
			                <ww:else>
			                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
			                    	<ww:param name="'value0'"><ww:property value="type"/></ww:param>
			                    </ww:text>
			                </ww:else>
                                    (<a id="delGroup_<ww:property value="parameter"/>_<ww:property value="../name"/>" href="<ww:url page="DeleteIssueSecurity!default.jspa"><ww:param name="'id'" value="id"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"title="<ww:text name="'admin.schemes.issuesecurity.delete.this.user.group'"/>"><ww:text name="'common.words.delete'"/></a>)
                            </li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                    <ww:else>
                        &nbsp;
                    </ww:else>
                </td>
                <td>
                    <ul class="operations-list">
                        <li><a id="add_<ww:property value="./name"/>" href="<ww:url page="AddIssueSecurity!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'security'" value="./id"/></ww:url>"title="<ww:text name="'admin.schemes.issuesecurity.add.a.user.group'"/>"><ww:text name="'common.forms.add'"/></a></li>
                    <ww:if test="../default(./id) != true">
                        <li><a id="default_<ww:property value="./name"/>" href="<ww:url page="EditIssueSecurities!makeDefaultLevel.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'levelId'" value="./id"/></ww:url>"title="<ww:text name="'admin.schemes.issuesecurity.make.default'"/>"><ww:text name="'admin.common.words.default'"/></a></li>
                    </ww:if>
                        <li><a id="delLevel_<ww:property value="./name"/>" href="<ww:url page="DeleteIssueSecurityLevel!default.jspa">
                    <ww:param name="'levelId'" value="./id"/><ww:param name="'schemeId'" value="scheme/long('id')"/></ww:url>"title="<ww:text name="'admin.schemes.issuesecurity.delete.security.level'"/>"><ww:text name="'common.words.delete'"/></a></li>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>

    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'contentHtml'">
            <page:applyDecorator name="jiraform">
                <page:param name="action">EditIssueSecurities!addLevel.jspa</page:param>
                <page:param name="submitId">level_submit</page:param>
                <page:param name="submitName"><ww:text name="'admin.schemes.issuesecurity.add.security.level'"/></page:param>
                <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.add.security.level'"/></page:param>
                <page:param name="description"><ww:text name="'admin.schemes.issuesecurity.add.security.level.instruction'"/></page:param>
                <page:param name="helpURL">security_levels</page:param>
                <page:param name="helpURLFragment">#Creating+a+Security+Level</page:param>
                <ui:textfield label="text('common.words.name')" name="'name'" size="'30'"/>
                <ui:textfield label="text('common.words.description')" name="'description'" size="'60'"/>
                <ui:component name="'schemeId'" template="hidden.jsp"/>
            </page:applyDecorator>
        </aui:param>
    </aui:component>

    <ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
        <ui:param name="'projects'" value="/usedIn"/>
        <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
    </ui:component>

</body>
</html>

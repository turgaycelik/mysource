<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="issue_type_screen_scheme"/>
	<title>
        <ww:text name="'admin.itss.configure.issue.type.screen.scheme'">
            <ww:param name="'value0'"><ww:property value="/issueTypeScreenScheme/name" /></ww:param>
        </ww:text>
    </title>
</head>
<body>
<header class="aui-page-header">
    <div class="aui-page-header-inner">
        <div class="aui-page-header-main">
            <h2>
                <ww:text name="'admin.itss.configure.issue.type.screen.scheme'">
                    <ww:param name="'value0'">
                        <span data-scheme-field="name" data-id="<ww:property value="/issueTypeScreenScheme/id"/>"><ww:property value="/issueTypeScreenScheme/name" /></span>
                    </ww:param>
                </ww:text>
            </h2>
            <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
                <ui:param name="'projects'" value="/usedIn"/>
            </ui:component>
        </div>
        <div class="aui-page-header-actions">
            <div class="aui-buttons">
                <ww:if test="/addableIssueTypes/empty == false">
                    <a id="add-issue-type-screen-scheme-configuration-association" class="aui-button trigger-dialog"
                       href="AddIssueTypeScreenScreenSchemeAssociation!default.jspa?id=<ww:property value="./id"/>">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issue.type.to.screen.scheme.association.button.label'"/>
                    </a>
                </ww:if>
                <ww:else>
                    <a id="add-issue-type-screen-scheme-configuration-association"
                          title="<ww:text name="'admin.issuefields.screens.schemes.issuetype.all.issue.types.have.associations'"/>"
                          class="aui-button" aria-disabled="true">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issue.type.to.screen.scheme.association.button.label'"/>
                    </a>
                </ww:else>
            </div>
            <aui:component name="'issuetype_screenschemes'" template="help.jsp" theme="'aui'" />
        </div>
    </div>
</header>

<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">info</aui:param>
    <aui:param name="'messageHtml'">
        <p>
            <ww:text name="'admin.itss.configure.definition'">
                <ww:param name="'value0'"><a href="ViewFieldScreenSchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
        <p>
            <ww:text name="'admin.itss.configure.default.entry.definition'"/>
        </p>
        <p>
            <ww:text name="'admin.itss.configure.view.all.itss'">
                <ww:param name="'value0'"><a id="view_issuetypescreenschemes" href="ViewIssueTypeScreenSchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
    </aui:param>
</aui:component>

<ww:if test="/issueTypeScreenSchemeEntities/empty == false">
    <table id="issue-type-table" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.concepts.issuetype'"/>
                </th>
                <th>
                    <ww:text name="'admin.projects.screen.scheme'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/issueTypeScreenSchemeEntities" status="'status'">
            <ww:if test="/shouldDisplay(.) == true">
            <tr data-default-association="<ww:property value="/default(.)"/>">
                <td>
                    <ww:if test="./issueType">
                        <ww:property value="./issueTypeObject">
                            <ww:component name="'issuetype'" template="constanticon.jsp">
                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                <ww:param name="'iconurl'" value="./iconUrl" />
                                <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                                <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                            </ww:component>
                            <strong data-scheme-field=issue-type><ww:property value="./nameTranslation" /></strong>
                        </ww:property>
                    </ww:if>
                    <ww:else>
                        <strong data-scheme-field=issue-type><ww:text name="'admin.common.words.default'"/></strong>
                        <div class="description secondary-text"><ww:text name="'admin.itss.configure.used.for.all.unmapped.issue.types'"/></div>
                    </ww:else>
                </td>
                <td>
                    <ww:property value="./fieldScreenScheme">
                        <a data-scheme-field="screen-scheme" id="configure_fieldscreenscheme" href="ConfigureFieldScreenScheme.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a>
                    </ww:property>
                </td>
                <td>
                    <ul class="operations-list">
                    <ww:if test="./issueType == null">
                        <li><a id="edit_issuetypescreenschemeentity_default" href="ViewEditIssueTypeScreenSchemeEntity.jspa?id=<ww:property value="/id" />" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.default.mapping'"/>"><ww:text name="'common.words.edit'"/></a></li>
                    </ww:if>
                    <ww:else>
                        <li><a id="edit_issuetypescreenschemeentity_<ww:property value="./issueType/string('name')" />" href="ViewEditIssueTypeScreenSchemeEntity.jspa?id=<ww:property value="/id" />&issueTypeId=<ww:property value="./issueType/string('id')" />" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.entry'"/>"><ww:text name="'common.words.edit'"/></a></li>
                        <li><a id="delete_issuetypescreenschemeentity_<ww:property value="./issueType/string('name')" />" href="<ww:url page="DeleteIssueTypeScreenSchemeEntity.jspa"><ww:param name="'id'" value="/id" /><ww:param name="'issueTypeId'" value="./issueType/string('id')" /></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.delete.entry'"/>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:else>
                    </ul>
                </td>
            </tr>
            </ww:if>
        </ww:iterator>
        </tbody>
    </table>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.no.issue.mappings.alert'"/></aui:param>
    </aui:component>
</ww:else>
<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
</ui:component>
</body>
</html>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title>
        <ww:text name="'admin.issuefields.fieldconfigschemes.configure.field.configuration.scheme'">
            <ww:param name="'value0'"><ww:property value="/fieldLayoutScheme/name"/></ww:param>
        </ww:text>
    </title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>
<header class="aui-page-header">
    <div class="aui-page-header-inner">
        <div class="aui-page-header-main">
            <h2>
                <ww:text name="'admin.issuefields.fieldconfigschemes.configure.field.configuration.scheme'">
                    <ww:param name="'value0'">
                        <span data-scheme-field="name" data-id="<ww:property value="/fieldLayoutScheme/id"/>"><ww:property value="/fieldLayoutScheme/name"/></span>
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
                    <a id="add-issue-type-field-configuration-association" class="aui-button trigger-dialog"
                       href="AddIssueTypeToFieldConfigurationAssociation!default.jspa?id=<ww:property value="./id"/>">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issue.type.to.field.configuration.association.button.label'"/>
                    </a>
                </ww:if>
                <ww:else>
                    <a id="add-issue-type-field-configuration-association"
                          title="<ww:text name="'admin.issuefields.fieldconfigschemes.all.issue.types.have.associations'"/>"
                          class="aui-button" aria-disabled="true">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issue.type.to.field.configuration.association.button.label'"/>
                    </a>
                </ww:else>
            </div>
            <aui:component name="'fieldscreenschemes'" template="help.jsp" theme="'aui'" />
        </div>
    </div>
</header>

<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">info</aui:param>
    <aui:param name="'messageHtml'">
        <p><ww:text name="'admin.issuefields.fieldconfigschemes.scheme.association'"/></p>
        <p><ww:text name="'admin.issuefields.fieldconfigschemes.configure.instructions'"/></p>
        <p>
            <ww:text name="'admin.issuefields.fieldconfigschemes.view.all.field.layout.schemes'">
                <ww:param name="'value0'"><a id="view_fieldlayoutschemes" href="ViewFieldLayoutSchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
    </aui:param>
</aui:component>

<ww:if test="/fieldLayoutSchemeItems/empty == false">
    <table id="scheme_entries" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="20%">
                    <ww:text name="'common.concepts.issuetype'"/>
                </th>
                <th width="65%">
                    <ww:text name="'admin.issuefields.fieldconfigschemes.field.configuration'"/>
                </th>
                <th width="15%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/fieldLayoutSchemeItems" status="'status'">
            <ww:if test="/shouldDisplay(.) == true">
            <tr>
                <td>
                    <ww:if test="./issueType">
                        <ww:property value="./issueTypeObject">
                            <ww:component name="'issuetype'" template="constanticon.jsp">
                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                <ww:param name="'iconurl'" value="./iconUrl" />
                                <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                                <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                            </ww:component>
                             <strong data-scheme-field="issue-type"><ww:property value="./nameTranslation" /></strong>
                        </ww:property>
                    </ww:if>
                    <ww:else>
                        <strong data-scheme-field="issue-type"><ww:text name="'admin.common.words.default'"/></strong>
                        <div class="description secondary-text">
                            <ww:text name="'admin.issuefields.fieldconfigschemes.used.for.all.unmapped.issue.types'"/>
                        </div>
                    </ww:else>
                </td>
                <td>
                    <ww:property value="/fieldLayout(./fieldLayoutId)">
                        <ww:if test="./type">
                            <a data-scheme-field="field-configuration" id="configure_fieldlayout" href="ViewIssueFields.jspa"><ww:property value="./name" /></a>
                        </ww:if>
                        <ww:else>
                            <a data-scheme-field="field-configuration" id="configure_fieldlayout" href="ConfigureFieldLayout.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a>
                        </ww:else>
                    </ww:property>
                </td>
                <td>
                    <ul class="operations-list">
                    <ww:if test="./issueType == null">
                        <li><a id="edit_fieldlayoutschemeentity" href="<ww:url page="/secure/admin/ViewEditFieldLayoutSchemeEntity.jspa"><ww:param name="'id'" value="/id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.default.mapping'"/>"><ww:text name="'common.words.edit'"/></a></li>
                    </ww:if>
                    <ww:else>
                        <li><a id="edit_fieldlayoutschemeentity_<ww:property value="./issueType/string('id')"/>" href="<ww:url page="/secure/admin/ViewEditFieldLayoutSchemeEntity.jspa"><ww:param name="'id'" value="/id"/><ww:param name="'issueTypeId'" value="./issueType/string('id')" /></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.entry'"/>"><ww:text name="'common.words.edit'"/></a></li>
                        <li><a id="delete_fieldlayoutschemeentity_<ww:property value="./issueType/string('id')" />" href="<ww:url page="/secure/admin/DeleteFieldLayoutSchemeEntity.jspa"><ww:param name="'id'" value="/id"/><ww:param name="'issueTypeId'" value="./issueType/string('id')" /></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.delete.entry'"/>"><ww:text name="'common.words.delete'"/></a></li>
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
        <aui:param name="'messageType'">warning</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.no.issue.mappings.alert'"/></aui:param>
    </aui:component>
</ww:else>
<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
</ui:component>
</body>
</html>
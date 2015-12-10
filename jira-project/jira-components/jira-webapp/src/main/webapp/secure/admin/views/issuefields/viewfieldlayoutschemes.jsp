<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigschemes.view.field.configuration.schemes'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.issuefields.fieldconfigschemes.view.field.configuration.schemes'"/></h2>
            </div>
            <div class="aui-page-header-actions">
                <div class="aui-buttons">
                    <a id="add-field-configuration-scheme" class="aui-button trigger-dialog" href="AddFieldConfigurationScheme!default.jspa">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issuefields.fieldconfigschemes.add.field.configuration.scheme'"/>
                    </a>
                </div>
                <aui:component name="'issuefieldschemes'" template="help.jsp" theme="'aui'" />
            </div>
        </div>
    </header>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.issuefields.fieldconfigschemes.the.table.below'"/>
            </p>
            <p>
                <ww:text name="'admin.issuefields.fieldconfigschemes.description'">
                    <ww:param name="'value0'"><a href="ViewFieldLayouts.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

<ww:if test="fieldLayoutScheme/size == 0">
    <p><ww:text name="'admin.issuefields.fieldconfigschemes.no.field.configuration.schemes.configured'"/></p>
</ww:if>
<ww:else>
    <table id="field-configuration-schemes-table" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'common.concepts.projects'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/fieldLayoutScheme" status="'status'">
            <tr data-field-configuration-scheme-name="<ww:property value="./name"/>">
                <td>
                    <a href="<ww:url page="/secure/admin/ConfigureFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.change.field.layouts'"/>">
                        <strong data-scheme-field="name"><ww:property value="./name"/></strong>
                    </a>
                    <div class="description secondary-text">
                        <ww:property value="./description"/>
                    </div>
                </td>
                <td>
                <ww:if test="/schemeProjects(.)/empty == false">
                    <ul>
                    <ww:iterator value="/schemeProjects(.)">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
                </td>
                <td>
                    <ul class="operations-list">
                        <li><a id="configure_<ww:property value="./id"/>" data-operation="configure" href="<ww:url page="/secure/admin/ConfigureFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.change.field.layouts'"/>"><ww:text name="'admin.common.words.configure'"/></a></li>
                        <li><a id="copy_<ww:property value="./id"/>" data-operation="copy" href="<ww:url page="/secure/admin/CopyFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.create.a.copy'"/>"><ww:text name="'common.words.copy'"/></a></li>
                        <li><a id="edit_<ww:property value="./id"/>" data-operation="edit" href="<ww:url page="/secure/admin/EditFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.edit.name.and.description'"/>"><ww:text name="'common.words.edit'"/></a></li>
                    <!-- Scheme can only be deleted if it is not associated with an issue type/project -->
                    <ww:if test="/schemeProjects(.)/empty == true">
                        <li><a id="del_<ww:property value="./name"/>" data-operation="delete" href="<ww:url page="/secure/admin/DeleteFieldLayoutScheme!default.jspa"><ww:param name="'id'" value="./id"/></ww:url>" title="<ww:text name="'admin.issuefields.fieldconfigschemes.delete.this.scheme'"/>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:if>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</ww:else>
</body>
</html>

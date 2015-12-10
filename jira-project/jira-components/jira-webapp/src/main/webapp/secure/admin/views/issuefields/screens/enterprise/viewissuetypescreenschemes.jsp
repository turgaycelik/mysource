<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="issue_type_screen_scheme"/>
	<title><ww:text name="'admin.issuefields.issuetypescreenschemes.view.issue.type.screen.schemes'"/></title>
</head>
<body>
<header class="aui-page-header">
    <div class="aui-page-header-inner">
        <div class="aui-page-header-main">
            <h2><ww:text name="'admin.issuefields.issuetypescreenschemes.view.issue.type.screen.schemes'"/></h2>
        </div>
        <div class="aui-page-header-actions">
            <div class="aui-buttons">
                <a id="add-issue-type-screen-scheme" class="aui-button trigger-dialog" href="AddIssueTypeScreenScheme!default.jspa">
                    <span class="icon jira-icon-add"></span>
                    <ww:text name="'admin.issuefields.issuetypescreenschemes.add'"/>
                </a>
            </div>
            <aui:component name="'issuetype_screenschemes'" template="help.jsp" theme="'aui'" />
        </div>
    </div>
</header>

<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">info</aui:param>
    <aui:param name="'messageHtml'">
        <p>
            <ww:text name="'admin.issuefields.issuetypescreenschemes.definition'">
                <ww:param name="'value0'"><a href="ViewFieldScreenSchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
        <p>
            <ww:text name="'admin.issuefields.issuetypescreenschemes.project.association.note'">
                <ww:param name="'value0'"><a href="ViewFieldScreens.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
        <p>
            <ww:text name="'admin.issuefields.issuetypescreenschemes.note'">
                <ww:param name="'value0'"><em></ww:param>
                <ww:param name="'value1'"></em></ww:param>
            </ww:text>

        </p>
    </aui:param>
</aui:component>
<ww:if test="/issueTypeScreenSchemes/empty == false">
<table id="issue-type-screen-schemes-table" class="aui aui-table-rowhover">
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
    <ww:iterator value="/issueTypeScreenSchemes" status="'status'">
        <tr data-issue-type-screen-scheme-name="<ww:property value="./name" />">
            <td class="cell-type-key">
                <a  href="ConfigureIssueTypeScreenScheme.jspa?id=<ww:property value="./id" />"
                    title="<ww:text name="'admin.issuefields.issuetypescreenschemes.configure.the.scheme'">
                                <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                           </ww:text>">
                    <strong data-scheme-field="name"><ww:property value="./name" /></strong>
                </a>
                <div class="description secondary-text"><ww:property value="./description"/></div>
            </td>
            <td>
                <ww:if test="/projects(.)/empty == false">
                    <ul>
                    <ww:iterator value="/projects(.)">
                        <li><a id="view_project" href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td class="cell-type-collapsed">
                <ul class="operations-list">
                    <li>
                        <a data-operation="configure" id="configure_issuetypescreenscheme_<ww:property value="./id" />" href="ConfigureIssueTypeScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.configure.the.scheme'">
                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                        </ww:text>"><ww:text name="'admin.common.words.configure'"/></a>
                    </li>
                    <li>
                        <a data-operation="edit" id="edit_issuetypescreenscheme_<ww:property value="./id" />" href="EditIssueTypeScreenScheme!default.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.edit'">
                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                        </ww:text>"><ww:text name="'common.words.edit'"/></a>
                    </li>
                    <li>
                        <a data-operation="copy" id="copy_issuetypescreenscheme_<ww:property value="./id" />" href="ViewCopyIssueTypeScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.copy.entry'">
                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                        </ww:text>"><ww:text name="'common.words.copy'"/></a>
                    </li>
                <ww:if test="/projects(.)/empty == true && ./default == false">
                    <li>
                        <a data-operation="delete" id="delete_issuetypescreenscheme_<ww:property value="./id" />" href="<ww:url page="ViewDeleteIssueTypeScreenScheme.jspa"><ww:param name="'id'" value="./id" /></ww:url>" title="<ww:text name="'admin.issuefields.issuetypescreenschemes.delete.entry'">
                        <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                        </ww:text>"><ww:text name="'common.words.delete'"/></a>
                    </li>
                </ww:if>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.issuetypescreenschemes.no.screen.schemes.configured'"/></aui:param>
    </aui:component>
</ww:else>
</body>
</html>

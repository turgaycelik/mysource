<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.issuefields.screenschemes.view.screen.schemes'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screen_scheme"/>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.issuefields.screenschemes.view.screen.schemes'"/></h2>
            </div>
            <div class="aui-page-header-actions">
                <div class="aui-buttons">
                    <a id="add-field-screen-scheme" class="aui-button trigger-dialog" href="AddNewFieldScreenScheme.jspa">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issuefields.screenschemes.add.screen.scheme'"/>
                    </a>
                </div>
                <aui:component name="'fieldscreenschemes'" template="help.jsp" theme="'aui'" />
            </div>
        </div>
    </header>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'admin.issuefields.screenschemes.the.table.below'"/>
                <ww:text name="'admin.issuefields.screenschemes.screens.schemes.are.mapped'">
                    <ww:param name="'value0'"><a href="ViewIssueTypeScreenSchemes.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </p>
            <p>
                <ww:text name="'admin.issuefields.screenschemes.note1'">
                    <ww:param name="'value0'"><span class="note"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

<ww:if test="/fieldScreenSchemes/empty == false">
    <table id="field-screen-schemes-table" class="aui">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'admin.menu.issuefields.issue.type.screen.schemes'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/fieldScreenSchemes" status="'status'">
            <tr data-field-screen-scheme-id="<ww:property value="./id" />">
                <td>
                    <strong class="field-screen-scheme-name"><ww:property value="./name" /></strong>
                    <ww:if test="./description && ./description/empty == false">
                        <div class="field-screen-scheme-description description secondary-text"><ww:property value="./description" /></div>
                    </ww:if>
                </td>
                <td>
                    <ww:if test="/issueTypeScreenSchemes(.)/empty == false">
                        <ul>
                        <ww:iterator value="/issueTypeScreenSchemes(.)">
                            <li><a id="configure_issuetypescreenscheme" href="ConfigureIssueTypeScreenScheme.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a></li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                </td>
                <td>
                    <ul class="operations-list">
                        <li>
                            <a id="configure_fieldscreenscheme_<ww:property value="./name" />" href="ConfigureFieldScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.configure.screens.for'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'admin.common.words.configure'"/>
                            </a>
                        </li>
                        <li>
                            <a id="edit_fieldscreenscheme_<ww:property value="./name" />" href="EditFieldScreenScheme!default.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.edit.value'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'common.words.edit'"/>
                            </a>
                        </li>
                        <li>
                            <a id="copy_fieldscreenscheme_<ww:property value="./name" />" href="ViewCopyFieldScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.copy.value'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'common.words.copy'"/>
                            </a>
                        </li>
                    <ww:if test="/issueTypeScreenSchemes(.)/empty == true">
                        <li>
                            <a id="delete_fieldscreenscheme_<ww:property value="./name" />" href="ViewDeleteFieldScreenScheme.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screenschemes.delete.value'"><ww:param name="'value0'"><ww:property value="./name" /></ww:param></ww:text>">
                                <ww:text name="'common.words.delete'"/>
                            </a>
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
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.screenschemes.no.screen.schemes.configured'"/></aui:param>
    </aui:component>
</ww:else>

</body>
</html>

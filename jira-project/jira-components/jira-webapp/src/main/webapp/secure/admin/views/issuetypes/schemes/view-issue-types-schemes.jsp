<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title>
        <ww:text name="'admin.issue.types.schemes.view.title'"/>
    </title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_type_schemes"/>
</head>

<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.issue.types.schemes.view.title'"/></h2>
            </div>
            <div class="aui-page-header-actions">
                <div class="aui-buttons">
                    <a id="issuetype-scheme-add" class="aui-button" href="<ww:url page="ConfigureOptionSchemes!default.jspa" atltoken="false"><ww:param name="'fieldId'" value="fieldId" /></ww:url>">
                        <ww:text name="'admin.issue.types.schemes.add.button.label'"/>
                    </a>
                </div>
                <aui:component name="'manageIssueTypes'" template="help.jsp" theme="'aui'" />
            </div>
        </div>
    </header>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p>
               <ww:text name="'admin.issuesettings.issuetypes.issue.type.schemes.determines'" />
            </p>
        </aui:param>
    </aui:component>
    <table id="issuetypeschemes" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'admin.common.words.options'"/>
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
        <ww:iterator value="/schemes" status="'status'">
            <tr data-id="<ww:property value="./id"/>" <ww:if test="$actionedSchemeId == ./id">class="rowHighlighted"</ww:if><ww:elseIf test="@status/modulus(2) == 0">class="rowAlternate"</ww:elseIf>>
                <td class="cell-type-key">
                    <strong data-scheme-field="name"><ww:property value="name" /></strong>
                    <div class="description secondary-text"><ww:property value="description"/></div>
                </td>
                <td data-scheme-field="options">
                <ww:property value="/options(.)" >
                    <ww:if test=". && ./size() > 0">
                        <ul class="imagebacked">
                        <ww:iterator value="." status="'status2'">
                            <li>
                                <img class="icon jira-icon-image" src="<ww:url value="imagePath" atltoken="false" />" alt="" />
                                <span class="issue-type-name"><ww:property value="./name" /></span><ww:if test="/default(./id, ../..) == true"> <span class="issue-type-default">(<ww:text name="'admin.common.words.default'"/>)</span></ww:if>
                            </li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                    <ww:else>
                        <span class="errorText"><ww:text name="'admin.issuesettings.no.issue.types.associated'"/></span>
                    </ww:else>
                </ww:property>
                </td>

                <td data-scheme-field="projects">
                <ww:if test="./global == true">
                    <span class="issue-type-scheme-global"><ww:text name="'admin.issuesettings.global'"/></span>
                </ww:if>
                <ww:elseIf test="./associatedProjects && ./associatedProjects/size() > 0">
                    <ul>
                    <ww:iterator value="./associatedProjects" status="'status2'">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="./string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:elseIf>
                <ww:else>
                    <span class="errorText"><ww:text name="'admin.issuesettings.no.project'"/></span>
                </ww:else>
                </td>

                <td class="cell-type-collapsed">
                    <ul class="operations-list">
                        <li><a id="edit_<ww:property value="id"/>" href="ConfigureOptionSchemes!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'common.words.edit'"/></a></li>
                    <ww:if test="/default(.) != true">
                        <li><a id="associate_<ww:property value="id"/>" href="AssociateIssueTypeSchemes!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'admin.projects.schemes.associate'"/></a></li>
                    </ww:if>
                    <ww:else>
                        <li><a id="associate_<ww:property value="id"/>" href="AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'admin.projects.schemes.associate'"/></a></li>
                    </ww:else>
                        <li><a id="copy_<ww:property value="id"/>" href="ConfigureOptionSchemes!copy.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'common.words.copy'"/></a></li>
                    <ww:if test="/default(.) != true">
                        <li><a id="delete_<ww:property value="id"/>" href="DeleteOptionScheme!default.jspa?fieldId=<ww:property value="fieldId" />&schemeId=<ww:property value="id"/>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:if>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
        </tbody>
    </table>
</body>
</html>

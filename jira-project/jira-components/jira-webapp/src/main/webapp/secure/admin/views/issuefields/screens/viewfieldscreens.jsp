<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.issuefields.screens.view.screens'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screens"/>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.issuefields.screens.view.screens'"/></h2>
            </div>
            <div class="aui-page-header-actions">
                <div class="aui-buttons">
                    <a id="add-field-screen" class="aui-button trigger-dialog" href="AddNewFieldScreen.jspa">
                        <span class="icon jira-icon-add"></span>
                        <ww:text name="'admin.issuefields.screens.add.screen'"/>
                    </a>
                </div>
                <aui:component name="'fieldscreens'" template="help.jsp" theme="'aui'" />
            </div>
        </div>
    </header>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.issuefields.screens.description'"/></p>
            <ul>
                <li>
                    <ww:text name="'admin.issuefields.screens.to.choose.screens'">
                        <ww:param name="'value0'"><b></ww:param>
                        <ww:param name="'value1'"></b></ww:param>
                        <ww:param name="'value2'"><b></ww:param>
                        <ww:param name="'value3'"></b></ww:param>
                        <ww:param name="'value4'"><a href="ViewFieldScreenSchemes.jspa"></ww:param>
                        <ww:param name="'value5'"></a></ww:param>
                    </ww:text>
                </li>
                <li>
                    <ww:text name="'admin.issuefields.screens.to.select.screens'">
                        <ww:param name="'value0'"><b></ww:param>
                        <ww:param name="'value1'"></b></ww:param>
                        <ww:param name="'value2'"><a href="ListWorkflows.jspa"></ww:param>
                        <ww:param name="'value3'"></a></ww:param>
                    </ww:text>
                </li>
            </ul>
            <p>
                <ww:text name="'admin.issuefields.screens.note1'">
                    <ww:param name="'value0'"><span class="note"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>
    <ww:if test="/fieldScreens/empty == false">
    <table id="field-screens-table" class="aui">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'admin.menu.issuefields.screen.schemes'"/>
                </th>
                <th>
                    <ww:text name="'admin.menu.globalsettings.workflows'"/>
                </th>
                <th>
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="/fieldScreens" status="'status'">
            <tr data-field-screen-id="<ww:property value="./id" />">
                <td>
                    <strong class="field-screen-name"><ww:property value="./name" /></strong>
                    <ww:if test="./description && ./description/empty == false">
                        <div class="field-screen-description description secondary-text"><ww:property value="./description" /></div>
                    </ww:if>
                </td>
                <td>
                    <ww:if test="/fieldScreenSchemes(.) != null && /fieldScreenSchemes(.)/empty != true ">
                        <ul>
                        <ww:iterator value="/fieldScreenSchemes(.)" status="'schemeStatus'">
                            <li><a href="<%= request.getContextPath() %>/secure/admin/ConfigureFieldScreenScheme.jspa?id=<ww:property value="./id" />"><ww:property value="./name" /></a></li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                </td>
                <td>
                    <ww:property value="/workflowTransitionViews(.)">
                        <ww:if test=". && ./empty == false">
                            <ul>
                            <ww:iterator value="." status="'workflowTransitionStatus'">
                                <li>
                                <ww:if test="./hasSteps == true || ./globalAction == true">
                                    <%-- If the action is global then no need to pass the step id in teh link --%>
                                    <ww:if test="./globalAction == true">
                                        <a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="./workflowMode" /><ww:param name="'workflowName'" value="./workflowName" /><ww:param name="'workflowTransition'" value="./transitionId" /></ww:url>"><ww:property value="./workflowName" /></a>
                                    </ww:if>
                                    <ww:else>
                                        <ww:property value="./firstStep" id="step" />
                                        <a href="<ww:url page="ViewWorkflowTransition.jspa"><ww:param name="'workflowMode'" value="./workflowMode" /><ww:param name="'workflowName'" value="./workflowName" /><ww:param name="'workflowStep'" value="@step/id" /><ww:param name="'workflowTransition'" value="./transitionId" /></ww:url>"><ww:property value="./workflowName" /></a>
                                    </ww:else>
                                    (<ww:property value="./transitionName" />)
                                </ww:if>
                                </li>
                            </ww:iterator>
                            </ul>
                        </ww:if>
                    </ww:property>
                </td>
                <td>
                    <ul class="operations-list">
                        <li>
                            <a id="configure_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="configure-fieldscreen" href="ConfigureFieldScreen.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.configure.tabs.and.fields'">
                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                            </ww:text>"><ww:text name="'admin.common.words.configure'"/></a>
                        </li>
                        <li>
                            <a id="edit_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="edit-fieldscreen" href="EditFieldScreen!default.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.edit.value'">
                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                            </ww:text>"><ww:text name="'common.words.edit'"/></a>
                        </li>
                        <li>
                            <a id="copy_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="copy-fieldscreen" href="ViewCopyFieldScreen.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.copy.value'">
                            <ww:param name="'value0'"><ww:property value="./name" /></ww:param>
                            </ww:text>"><ww:text name="'common.words.copy'"/></a>
                        </li>
                        <ww:if test="/deletable(.) == true">
                        <li>
                            <a id="delete_fieldscreen_<ww:property value="./name" />" rel="<ww:property value="./id" />" class="delete-fieldscreen" href="ViewDeleteFieldScreen.jspa?id=<ww:property value="./id" />" title="<ww:text name="'admin.issuefields.screens.delete.value'">
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
        <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.screens.no.screens.configured'"/></aui:param>
    </aui:component>
</ww:else>
</body>
</html>

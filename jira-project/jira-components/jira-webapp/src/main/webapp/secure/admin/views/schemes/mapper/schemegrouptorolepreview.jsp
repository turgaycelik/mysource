<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.group.role.preview.title'"/></title>
</head>

<body>

<script type="text/javascript">
function toggle(mode, elementId)
{
    var hideElement;
    var showElement;

    if (mode == "hide")
    {
        hideElement = document.getElementById('long_' + elementId);
        showElement = document.getElementById('short_' + elementId);
    }
    else
    {
        hideElement = document.getElementById('short_' + elementId);
        showElement = document.getElementById('long_' + elementId);
    }

    if (hideElement && showElement)
    {
        hideElement.style.display = 'none';
        showElement.style.display = '';
    }
}
</script>

<p>
    <page:applyDecorator name="jiraform">
        <page:param name="action">SchemeGroupToRoleTransformer.jspa</page:param>
        <page:param name="columns">1</page:param>
        <ww:if test="/hasSelectedSchemeIds == true && /groupToRoleMappings != null && /groupToRoleMappings/size != 0">
            <page:param name="submitId">save_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.words.save'"/></page:param>
        </ww:if>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI">SchemePicker!default.jspa</page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.group.role.preview.title'"/></page:param>
        <page:param name="autoSelectFirst">false</page:param>

        <ww:if test="/hasSelectedSchemeIds == true ">

        <tr>
            <td>
                <ww:text name="'admin.scheme.group.role.preview.instructions.1'">
                   <ww:param name="'value0'"><strong></ww:param>
                   <ww:param name="'value1'"><ww:property value="/schemeTransformResults/all/size"/></ww:param>
                   <ww:param name="'value2'"></strong></ww:param>
                </ww:text>
            </td>
        </tr>
        </ww:if>

<ww:if test="/hasSelectedSchemeIds == true && /groupToRoleMappings != null && /groupToRoleMappings/size != 0">
    <tr>
        <td>
            <table class="centered" id="group_to_role_mappings">
                <ww:if test="/mappingsWithoutGlobalUsePermission/size != 0" >
                    <ww:iterator value="/mappingsWithoutGlobalUsePermission">
                        <tr >
                            <td valign="middle" align="right" width="45%"><ww:property value="./groupName"/></td>
                            <td valign="bottom" align="center" width="10%"><img src="<%=request.getContextPath()%>/images/icons/arrow_right_small.gif" alt="to --->" /></td>
                            <td valign="middle" align="left" width="45%"><ww:property value="./projectRole/name"/></td>
                        </tr>
                    </ww:iterator>
                </ww:if>
                <ww:if test="/anyGroupGrantedGlobalUsePermission == true">
                    <tr>
                        <td colspan="3">
                            <aui:component template="auimessage.jsp" theme="'aui'">
                                <aui:param name="'messageType'">warning</aui:param>
                                <aui:param name="'messageHtml'">
                                    <p><ww:text name="'admin.scheme.group.role.preview.warning.use.permission'"/></p>
                                </aui:param>
                            </aui:component>
                        </td>
                    </tr>
                    <ww:iterator value="/mappingsWithGlobalUsePermission">
                        <tr >
                            <td valign="middle" align="right"><ww:property value="./groupName"/></td>
                            <td valign="bottom" align="center"><img src="<%=request.getContextPath()%>/images/icons/arrow_right_yellow.gif" alt="to --->" /></td>
                            <td valign="middle" align="left"><ww:property value="./projectRole/name"/></td>
                        </tr>
                    </ww:iterator>
                </ww:if>
            </table>
            <br/>
        </td>
    </tr>
    <tr>
        <ww:if test="/schemeTransformResults/associatedProjectsCount != 0">
        <td>
            <ww:text name="'admin.scheme.group.role.preview.instructions.2'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"><ww:property value="/schemeTransformResults/associatedProjectsCount"/></ww:param>
                <ww:param name="'value2'"></strong></ww:param>
            </ww:text>
        </td>
        </ww:if>
        <ww:else>
            <td><ww:text name="'admin.scheme.group.role.preview.no.project.roles'"/></td>
        </ww:else>
    </tr>

    <tr>
        <td>
            <table class="defaultwidth centered">
                <ww:iterator value="/schemeTransformResults/associatedTransformedResults">
                    <ww:iterator value="./associatedProjects">
                        <tr>
                            <td><ww:text name="'common.words.project'"/> <strong><ww:property value="./name"/></strong> <span class="smallgrey">(<ww:text name="'common.concepts.scheme'"/>:
                                <ww:property value="../originalScheme/name"/>)</span></td>
                        </tr>
                        <tr>
                            <td>
                                <table id="<ww:property value="./name"/>_summary" class="grid defaultwidth centered">
                                    <tr>
                                        <th width="20%"><ww:text name="'common.words.role'"/></th>
                                        <th width="80%"><ww:text name="'admin.scheme.group.role.preview.users.being.added'"/></th>
                                    </tr>
                                    <ww:iterator value="../roleToGroupsMappings">
                                        <tr>
                                            <td><ww:property value="./projectRole/name"/></td>
                                            <td>
                                <span id="short_<ww:property value="../name"/>_<ww:property value="./projectRole/name"/>"
                                        <ww:if test="./unpackedUsersLimited(21)/size == 21">onclick="toggle('expand', '<ww:property value="../name"/>_<ww:property value="./projectRole/name"/>');"</ww:if>>
                                    <ww:iterator value="./unpackedUsersLimited(20)" status="'status'"><ww:property value="/fullNameForUser(.)"/><ww:if test="@status/last == false">, </ww:if></ww:iterator>
                                    <ww:if test="./unpackedUsersLimited(21)/size == 21"><span style="cursor:pointer;" class="smallgrey" >[<ww:text name="'common.concepts.more'" />]</span></ww:if>
                                </span>
                                <span style="display:none; cursor:pointer;"
                                      id="long_<ww:property value="../name"/>_<ww:property value="./projectRole/name"/>"
                                      onclick="toggle('hide', '<ww:property value="../name"/>_<ww:property value="./projectRole/name"/>');">
                                    <ww:iterator value="./unpackedUsers" status="'status'"><ww:property value="/fullNameForUser(.)"/><ww:if test="@status/last == false">, </ww:if></ww:iterator>

                                    <span class="smallgrey">[<ww:text name="'admin.deleteuser.hide'"/>]</span>
                                </span>
                                            </td>
                                        </tr>
                                    </ww:iterator>
                                </table>
                            </td>
                        </tr>
                    </ww:iterator>
                </ww:iterator>
            </table>
        </td>
    </tr>

</ww:if>
<ww:elseIf test="/hasSelectedSchemeIds == false && hasAnyErrors == false">
    <tr>
        <td>
            <ww:text name="'admin.scheme.group.role.preview.no.schemes.selected'"/>
        </td>
    </tr>
</ww:elseIf>
<ww:elseIf test="hasAnyErrors == false">
    <tr>
        <td>
            <ww:text name="'admin.scheme.group.role.preview.no.groups.selected'"/>
        </td>
    </tr>
</ww:elseIf>

</page:applyDecorator>
</p>
</body>
</html>

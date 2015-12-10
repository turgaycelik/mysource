<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.group.role.mapper.title'"/></title>
</head>

<body>

<script type="text/javascript">
    function submitForm(typeOfSchemesToDisplay)
    {
        document.forms['jiraform'].action = "SchemePicker.jspa?typeOfSchemesToDisplay=" + typeOfSchemesToDisplay + "&selectedSchemeType=" + document.getElementById("selectedSchemeType").value;
        document.forms['jiraform'].submit();
        return false;
    }
</script>

<p>
    <page:applyDecorator name="jiraform">
    <page:param name="action">SchemeGroupToRoleMapper.jspa</page:param>
    <page:param name="columns">1</page:param>
    <page:param name="helpURL">scheme_tools</page:param>
    <ww:if test="/hasSelectedSchemeIds == true && /uniqueGroupsForSelectedSchemes/size != 0">
        <page:param name="submitId">mapper_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.scheme.group.role.mapper.submit'"/></page:param>
    </ww:if>
    <page:param name="cancelURI">SchemePicker!default.jspa</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.scheme.group.role.mapper.title'"/></page:param>
        <page:param name="description"><ww:text name="'admin.scheme.group.role.mapper.desc'">
            <ww:param name="'value0'"><p/></ww:param>
            <ww:param name="'value1'"><a href="<%=request.getContextPath()%>/secure/project/ViewProjectRoles.jspa"></ww:param>
            <ww:param name="'value2'"></a></ww:param>
        </ww:text>
    </page:param>
        <!-- error cases first -->
        <ww:if test="/hasSelectedSchemeIds == false && hasAnyErrors == false">
            <tr>
                <td><ww:text name="'admin.scheme.group.role.mapper.no.selected.schemes'"/></td>
            </tr>
        </ww:if>
        <ww:elseIf test="/uniqueGroupsForSelectedSchemes/size == 0 && hasAnyErrors == false">
            <tr>
                <td><ww:text name="'admin.scheme.group.role.mapper.no.groups'"/></td>
            </tr>
        </ww:elseIf>
        <!-- happy path -->
        <ww:elseIf test="/hasSelectedSchemeIds == true">
        <tr>
            <td>
                <ww:if test="/groupsWithoutGlobalUsePermission/size != 0">
                <table class="grid centered" id="group_to_role_mappings_no_use_permission" style="width: 40%">
                    <tr>
                        <th>
                            <ww:text name="'common.words.groups'"/>
                        </th>
                        <th>
                            <ww:text name="'common.words.project.roles'"/>
                        </th>
                    </tr>
                    <ww:iterator value="/groupsWithoutGlobalUsePermission">
                        <tr>
                            <td width="50%">
                                <ww:property value="."/>
                            </td>
                            <td valign="middle">
                                <select id="<ww:property value="."/>_project_role" name="<ww:property value="."/>_project_role">
                                    <option value="-1"><ww:text name="'admin.scheme.group.role.mapper.do.not.map.group'"/></option>
                                    <ww:iterator value="/availableRoles">
                                        <option value="<ww:property value="./id"/>" id="<ww:property value="./id"/>"><ww:property value="./name"/></option>
                                    </ww:iterator>
                                </select>
                            </td>
                        </tr>
                    </ww:iterator>
                </table>
                </ww:if>

                <ww:if test="/existsGroupsWithGlobalUsePermission == true">
                <p>
                <ww:text name="'admin.scheme.group.role.mapper.warning.use.permission'"/>
                </p>
                <table class="grid centered" id="group_to_role_mappings_use_permission" style="width: 40%">
                    <tr>
                        <th>
                            <ww:text name="'common.words.groups'"/><br />
                        <th>
                            <ww:text name="'common.words.project.roles'"/>
                        </th>
                    </tr>
                    <ww:iterator value="/groupsWithGlobalUsePermission">
                        <tr>
                            <td width="50%">
                                <ww:property value="."/>
                            </td>
                            <td valign="middle">
                                <select id="<ww:property value="."/>_project_role" name="<ww:property value="."/>_project_role">
                                    <option value="-1"><ww:text name="'admin.scheme.group.role.mapper.do.not.map.group'"/></option>
                                    <ww:iterator value="/availableRoles">
                                        <option value="<ww:property value="./id"/>" id="<ww:property value="./id"/>"><ww:property value="./name"/></option>
                                    </ww:iterator>
                                </select>
                            </td>
                        </tr>
                    </ww:iterator>
                </table>
                </ww:if>
                <ww:iterator value="/selectedSchemeIds">
                    <input type="hidden" name="selectedSchemeIds" id="selectedSchemeIds" value="<ww:property value='.'/>"/>
                </ww:iterator>
                <input type="hidden" name="selectedSchemeType" id="selectedSchemeType" value="<ww:property value="/selectedSchemeType"/>"/>
            </td>
        </tr>
        </ww:elseIf>
    </page:applyDecorator>
</p>


</body>
</html>

<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.group.role.result.title'"/></title>
</head>

<body>
<p>
    <page:applyDecorator name="jirapanel">
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.group.role.result.title'"/></page:param>

        <tr>
            <td>
                <ww:text name="'admin.scheme.group.role.result.instruction.1'">
                   <ww:param name="'value0'"><strong></ww:param>
                   <ww:param name="'value1'"><ww:property value="/schemeTransformResults/allSchemeTransformResults/size"/></ww:param>
                   <ww:param name="'value2'"></strong></ww:param>
                </ww:text>
                <ul class="square_blue">
                    <ww:iterator value="/schemeTransformResults/allSchemeTransformResults">
                        <li><ww:property value="./transformedScheme/name"/></li>
                    </ww:iterator>
                </ul>
            </td>
        </tr>
        <tr>
            <td>
                <ww:text name="'admin.scheme.group.role.result.instruction.2'"/>
                <ul class="square_blue">
                    <ww:iterator value="/schemeTransformResults/allSchemeTransformResults">
                        <li><ww:property value="./originalScheme/name"/></li>
                    </ww:iterator>
                </ul>
            </td>
        </tr>
        <tr>
            <td>
                <ww:text name="'admin.scheme.group.role.result.instruction.3'">
                   <ww:param name="'value0'"><a id="merge_tool" href="<%=request.getContextPath()%>/secure/admin/SchemeTypePicker!default.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                   <ww:param name="'value2'"><a id="delete_tool" href="<%=request.getContextPath()%>/secure/admin/SchemePurgeTypePicker!default.jspa"></ww:param>
                </ww:text>
            </td>
        </tr>

</page:applyDecorator>
</p>
</body>
</html>

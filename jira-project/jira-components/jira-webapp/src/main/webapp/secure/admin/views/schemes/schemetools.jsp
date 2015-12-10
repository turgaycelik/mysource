<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'admin.scheme.tools.title'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
</head>

<body>

<p>
    <page:applyDecorator name="jirapanel">
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.tools.title'"/></page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="description">
            <ww:if test="/systemAdministrator == true">
                <ww:text name="'admin.scheme.tools.desc'">
                    <ww:param name="'value0'"><p/></ww:param>
                    <ww:param name="'value1'"><a href="<%=request.getContextPath()%>/secure/admin/XmlBackup!default.jspa"></ww:param>
                    <ww:param name="'value2'"></a></ww:param>
                    <ww:param name="'value3'"><span class="redText"></ww:param>
                    <ww:param name="'value4'"></span></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.scheme.tools.desc.admin'">
                    <ww:param name="'value0'"><p/></ww:param>
                    <ww:param name="'value1'"> </ww:param>
                    <ww:param name="'value2'"> </ww:param>
                    <ww:param name="'value3'"><span class="redText"></ww:param>
                    <ww:param name="'value4'"></span></ww:param>
                </ww:text>
            </ww:else>
        </page:param>
        <table id="scheme-tools-list" class="grid  centered">
            <tr>
                <td valign="top" nowrap="true">
                    <a id="compare_tool" href="<%=request.getContextPath()%>/secure/admin/SchemeComparisonPicker!default.jspa"><ww:text name="'admin.scheme.tools.compare.tool.title'"/></a></td>
                <td valign="top">
                    <ww:text name="'admin.scheme.tools.compare.tool.desc'">
                       <ww:param name="'value0'"><p/></ww:param>
                       <ww:param name="'value1'"><a href="<%=request.getContextPath()%>/secure/admin/SchemeTypePicker!default.jspa"></ww:param>
                       <ww:param name="'value2'"></a></ww:param>
                    </ww:text>
                </td>
            </tr>
            <tr>
                <td valign="top" nowrap="true">
                    <a id="mapping_tool" href="<%=request.getContextPath()%>/secure/admin/SchemePicker!default.jspa"><ww:text name="'admin.scheme.tools.mapping.tool.title'"/></a>
                </td>
                <td valign="top">
                    <ww:text name="'admin.scheme.tools.mapping.tool.desc'"/>
                </td>
            </tr>
            <tr>
                <td valign="top" nowrap="true">
                    <a id="merge_tool" href="<%=request.getContextPath()%>/secure/admin/SchemeTypePicker!default.jspa"><ww:text name="'admin.scheme.tools.merge.tool.title'"/></a>
                </td>
                <td valign="top">
                    <ww:text name="'admin.scheme.tools.merge.tool.desc'">
                       <ww:param name="'value0'"><p/></ww:param>
                       <ww:param name="'value1'"><a href="<%=request.getContextPath()%>/secure/admin/SchemePicker!default.jspa"></ww:param>
                       <ww:param name="'value2'"></a></ww:param>
                       <ww:param name="'value3'"><a href="<%=request.getContextPath()%>/secure/admin/SchemeComparisonPicker!default.jspa"></ww:param>
                    </ww:text>
                </td>
            </tr>
            <tr>
                <td valign="top" nowrap="true">
                    <a id="delete_tool" href="<%=request.getContextPath()%>/secure/admin/SchemePurgeTypePicker!default.jspa"><ww:text name="'admin.scheme.tools.delete.tool.title'"/></a>
                </td>
                <td valign="top">
                    <ww:text name="'admin.scheme.tools.delete.tool.desc'">
                       <ww:param name="'value0'"><p/></ww:param>
                       <ww:param name="'value1'"><a href="<%=request.getContextPath()%>/secure/admin/SchemeTypePicker!default.jspa"></ww:param>
                       <ww:param name="'value2'"></a></ww:param>
                       <ww:param name="'value3'"><a href="<%=request.getContextPath()%>/secure/admin/SchemePicker!default.jspa"></ww:param>
                    </ww:text>
                </td>
            </tr>
        </table>
    </page:applyDecorator>
</p>


</body>
</html>

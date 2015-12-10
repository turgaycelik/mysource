<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.purge.preview.title'"/></title>
</head>

<body>


<p>
    <page:applyDecorator name="jiraform">
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.purge.preview.title'"/></page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="description">
            <ww:if test="/systemAdministrator == true">
                <ww:text name="'admin.scheme.purge.preview.desc'">
                    <ww:param name="'value0'"><p/></ww:param>
                    <ww:param name="'value1'"><span class="redText"></ww:param>
                    <ww:param name="'value2'"></span></ww:param>
                    <ww:param name="'value3'"><a href="<%=request.getContextPath()%>secure/admin/XmlBackup!default.jspa"></ww:param>
                    <ww:param name="'value4'"></a></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.scheme.purge.preview.desc.admin'">
                    <ww:param name="'value0'"><p/></ww:param>
                    <ww:param name="'value1'"><span class="redText"></ww:param>
                    <ww:param name="'value2'"></span></ww:param>
                    <ww:param name="'value3'"> </ww:param>
                    <ww:param name="'value4'"> </ww:param>
                </ww:text>
            </ww:else>
        </page:param>
        <page:param name="action">SchemePurgeToolResults.jspa</page:param>
        <page:param name="columns">1</page:param>
        <page:param name="submitId">deleteschemes_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.scheme.purge.preview.delete.schemes'"/></page:param>
        <page:param name="cancelURI"><ww:url page="SchemePurgeTypePicker!return.jspa"><ww:param name="'selectedSchemeType'" value="/selectedSchemeType"/></ww:url></page:param>
        <page:param name="autoSelectFirst">false</page:param>


        <tr>
            <td>
                <ww:text name="'admin.scheme.purge.preview.chosen.delete.schemes'"/>:
                <ul class="square_blue" style="padding-left:20px">
                    <ww:iterator value="/selectedSchemes" status="'status'">
                        <li><ww:property value="./name"/></li>
                    </ww:iterator>
                </ul>
            </td>
        </tr>
        <ui:component name="'selectedSchemeType'" value="/selectedSchemeType" template="hidden.jsp" theme="'single'"/>
    </page:applyDecorator>
</p>
</body>
</html>

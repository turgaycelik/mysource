<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.purge.result.title'"/></title>
</head>

<body>


<p>
    <page:applyDecorator name="jiraform">
        <page:param name="columns">1</page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.purge.result.title'"/></page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="description">
            <ww:text name="'admin.scheme.purge.result.desc'">
               <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/SchemePurgeTypePicker!default.jspa"></ww:param>
               <ww:param name="'value1'"></a></ww:param>
               <ww:param name="'value2'"><a href="<%=request.getContextPath()%>/secure/admin/SchemeTools.jspa"></ww:param>
            </ww:text>
        </page:param>
        <page:param name="autoSelectFirst">false</page:param>

        <ww:iterator value="/deletionErrors/errorMessages">
            <tr>
                <td>
                    <table class="defaultWidth centered">
                        <tr><td>
                            <div class="warningBox">
                                <ww:property value="." escape="false"/>
                            </div>
                        </td></tr>
                    </table>
                </td>
            </tr>
        </ww:iterator>

        <tr>
            <td>
                <ww:text name="'admin.scheme.purge.result.succes'"/>:
                <ul class="square_blue" style="padding-left:20px">
                    <ww:iterator value="/successfullyDeletedSchemes">
                        <li><ww:property value="./name"/></li>
                    </ww:iterator>
                </ul>

            </td>
        </tr>

    </page:applyDecorator>
</p>
</body>
</html>

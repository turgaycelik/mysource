<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.merge.result.title'"/></title>
</head>

<body>

<p>
    <page:applyDecorator name="jirapanel">
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.merge.result.title'"/></page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="description">
            <ww:text name="'admin.scheme.merge.result.desc'">
               <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/SchemePurgeTypePicker!default.jspa"></ww:param>
               <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </page:param>
        <page:param name="autoSelectFirst">false</page:param>

        <ww:iterator value="/persistErrors/errors">
            <tr>
                <td>
                    <table class="defaultWidth centered">
                        <tr><td>
                            <div class="warningBox">
                                <ww:text name="'admin.scheme.merge.result.could.not.save'"/> <strong><ww:property value="./key"/></strong>:
                                <ww:property value="./value" escape="false"/>
                            </div>
                        </td></tr>
                    </table>
                </td>
            </tr>
        </ww:iterator>


        <ww:if test="/persistedDistilledSchemeResults/size > 0">
            <tr>
                <td>
                    <ww:text name="'admin.scheme.merge.result.saved.merged.scheme'"/>:
                    <table class="defaultWidth centered">
                        <tr>
                            <td>
                                <ul class="square_blue">
                                    <ww:iterator value="/distilledSchemeResults/distilledSchemeResults" status="'status'">
                                        <ww:if test="./selected == true && /persistErrors/errors/containsKey(./resultingSchemeTempName) == false">
                                            <li>
                                                <strong><ww:property value="./resultingScheme/name"/></strong>
                                                <ww:if test="./allAssociatedProjects/size > 0"> <ww:text name="'admin.scheme.merge.result.ass.projects'"/>:<strong>
                                                    <ww:iterator value="./allAssociatedProjects" status="'status'">
                                                        <ww:property value="./name"/>
                                                        <ww:if test="@status/last == false">, </ww:if>
                                                    </ww:iterator>
                                                        </strong>
                                                </ww:if>
                                            </li>
                                        </ww:if>
                                    </ww:iterator>
                                </ul>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </ww:if>
    </page:applyDecorator>
</p>

</body>
</html>

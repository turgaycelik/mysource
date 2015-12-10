<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="scheme_tools"/>
    <title><ww:text name="'admin.scheme.merge.preview.title'"/></title>
</head>

<body>

<p>
    <page:applyDecorator name="jiraform">
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.scheme.merge.preview.title'"/></page:param>
        <page:param name="helpURL">scheme_tools</page:param>
        <page:param name="description">
            <ww:if test="/numberOfSelectedSchemes == /distilledSchemeResults/distilledSchemeResults/size">
                <ww:text name="'admin.scheme.merge.preview.desc.1'">
                   <ww:param name="'value0'"><strong></ww:param>
                   <ww:param name="'value1'"></strong></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.scheme.merge.preview.desc.2'">
                   <ww:param name="'value0'"><strong></ww:param>
                   <ww:param name="'value1'"><ww:property value="/numberOfSelectedSchemes"/></ww:param>
                   <ww:param name="'value2'"></strong></ww:param>
                   <ww:param name="'value3'"><ww:property value="/distilledSchemeResults/distilledSchemeResults/size"/></ww:param>
                </ww:text>
            </ww:else>

        </page:param>
        <page:param name="action">SchemeMergeResult.jspa</page:param>
        <page:param name="columns">1</page:param>
        <page:param name="submitId">preview_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.scheme.merge.preview.submit'"/></page:param>
        <page:param name="cancelURI"><ww:url page="SchemeMerge!default.jspa"><ww:param name="'selectedSchemeType'" value="/selectedSchemeType"/><ww:param name="'typeOfSchemesToDisplay'" value="/typeOfSchemesToDisplay"/></ww:url></page:param>
        <page:param name="autoSelectFirst">false</page:param>

        <tr>
            <td>
                    <ww:iterator value="/distilledSchemeResults/distilledSchemeResults" status="'status'">
                        <ww:if test="./selected == true">
                            <table class="defaultWidth centered">
                                <tr>
                                    <td><ww:text name="'admin.scheme.merge.preview.adding.scheme'"/>: <strong><ww:property value="./resultingSchemeTempName"/></strong></td>
                                </tr>
                                <tr>
                                    <td>
                                        <table class="grid defaultWidth centered" id="<ww:property value="./resultingSchemeTempName"/>_table">
                                            <tr>
                                                <th width="50%">
                                                    <ww:text name="'admin.scheme.merge.preview.merged.from.schemes'"/>
                                                </th>
                                                <th width="50%">
                                                    <ww:text name="'admin.scheme.merge.preview.project.associations.to.be.migrated'"/>
                                                </th>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <ww:iterator value="./originalSchemes" status="'status'">
                                                        <ww:property value="./name"/>
                                                        <ww:if test="@status/last == false">, </ww:if>
                                                    </ww:iterator>
                                                </td>
                                                <td>
                                                    <ww:iterator value="./allAssociatedProjects" status="'status'">
                                                        <ww:property value="./name"/>
                                                        <ww:if test="@status/last == false">, </ww:if>
                                                    </ww:iterator>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </ww:if>
                    </ww:iterator>
            </td>
        </tr>
    </page:applyDecorator>
</p>


</body>
</html>
